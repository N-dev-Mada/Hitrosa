package com.example.data.crypto

import com.example.data.model.TransactionEntity
import java.security.MessageDigest

object CryptoSecurity {

    const val GENESIS_HASH = "0000000000000000000000000000000000000000000000000000000000000000"

    /**
     * Calcule le hash SHA-256 infalsifiable selon la formule :
     * SHA256(previous_hash + client_id + date_credit + grand_total + signature_uri + emissaire_nom)
     */
    fun calculateTransactionHash(
        previousHash: String,
        clientId: String,
        dateCredit: Long,
        grandTotal: Long,
        signatureUri: String?,
        emissaireNom: String? = null
    ): String {
        val payload = buildString {
            append(previousHash)
            append(clientId)
            append(dateCredit)
            append(grandTotal)
            append(signatureUri.orEmpty())
            append(emissaireNom.orEmpty())
        }
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = digest.digest(payload.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }

    /**
     * Génère un code de reçu unique et lisible, ex: CR-9A4B1F
     */
    fun generateReferenceCode(transactionId: String): String {
        val cleaned = transactionId.replace("-", "").uppercase()
        val suffix = if (cleaned.length >= 6) cleaned.takeLast(6) else cleaned.padStart(6, '0')
        return "CR-$suffix"
    }

    data class AuditBlockResult(
        val index: Int,
        val transaction: TransactionEntity,
        val expectedPreviousHash: String,
        val isPreviousHashValid: Boolean,
        val recomputedHash: String,
        val isCurrentHashValid: Boolean,
        val isValid: Boolean
    )

    data class AuditReport(
        val totalBlocks: Int,
        val isChainValid: Boolean,
        val invalidBlockIndex: Int? = null,
        val auditedAt: Long = System.currentTimeMillis(),
        val blocks: List<AuditBlockResult> = emptyList()
    )

    /**
     * Vérifie de bout en bout l'intégrité de la chaîne chronologique des transactions.
     * Détecte toute altération directe dans la base de données SQLite.
     */
    fun verifyChain(transactions: List<TransactionEntity>): AuditReport {
        if (transactions.isEmpty()) {
            return AuditReport(
                totalBlocks = 0,
                isChainValid = true,
                blocks = emptyList()
            )
        }

        val results = mutableListOf<AuditBlockResult>()
        var previousHash = GENESIS_HASH
        var firstInvalidIndex: Int? = null

        transactions.forEachIndexed { index, tx ->
            val isPrevValid = tx.previousHash == previousHash
            val recomputed = calculateTransactionHash(
                previousHash = tx.previousHash,
                clientId = tx.clientId,
                dateCredit = tx.dateCredit,
                grandTotal = tx.grandTotal,
                signatureUri = tx.signatureUri,
                emissaireNom = tx.emissaireNom
            )
            val isCurrValid = tx.currentHash.equals(recomputed, ignoreCase = true)
            val isValid = isPrevValid && isCurrValid

            if (!isValid && firstInvalidIndex == null) {
                firstInvalidIndex = index
            }

            results.add(
                AuditBlockResult(
                    index = index,
                    transaction = tx,
                    expectedPreviousHash = previousHash,
                    isPreviousHashValid = isPrevValid,
                    recomputedHash = recomputed,
                    isCurrentHashValid = isCurrValid,
                    isValid = isValid
                )
            )

            previousHash = tx.currentHash
        }

        return AuditReport(
            totalBlocks = transactions.size,
            isChainValid = firstInvalidIndex == null,
            invalidBlockIndex = firstInvalidIndex,
            blocks = results
        )
    }
}
