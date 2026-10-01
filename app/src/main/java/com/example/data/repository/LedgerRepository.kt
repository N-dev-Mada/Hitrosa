package com.example.data.repository

import androidx.room.withTransaction
import com.example.data.crypto.CryptoSecurity
import com.example.data.db.AppDatabase
import com.example.data.model.Client
import com.example.data.model.ClientWithBalance
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionWithItems
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class LedgerRepository(private val database: AppDatabase) {

    private val clientDao = database.clientDao()
    private val transactionDao = database.transactionDao()

    val activeClients: Flow<List<Client>> = clientDao.getAllActiveClients()
    val allTransactions: Flow<List<TransactionWithItems>> = transactionDao.getAllTransactionsWithItems()
    val totalTransactionsCount: Flow<Int> = transactionDao.getTotalTransactionsCount()

    /**
     * Flux réactif combinant clients et transactions pour un calcul en temps réel des soldes
     */
    val clientsWithBalances: Flow<List<ClientWithBalance>> = combine(
        clientDao.getAllActiveClients(),
        transactionDao.getAllTransactionsWithItems()
    ) { clients, allTxs ->
        val now = System.currentTimeMillis()
        val thirtyDaysMs = 30L * 24 * 60 * 60 * 1000

        clients.map { client ->
            val clientTxs = allTxs.filter { it.transaction.clientId == client.id }
            var totalCredits = 0L
            var totalRemboursements = 0L
            var derniereOp: Long? = null
            var hasOverdueCredit = false

            clientTxs.forEach { txWithItems ->
                val tx = txWithItems.transaction
                if (derniereOp == null || tx.dateCredit > derniereOp!!) {
                    derniereOp = tx.dateCredit
                }

                when (tx.type) {
                    "CREDIT" -> {
                        totalCredits += tx.resteAPayer
                        val isPastDueDate = tx.dateRemboursementPrevue != null && tx.dateRemboursementPrevue < now
                        val isOlderThan30Days = (now - tx.dateCredit) > thirtyDaysMs
                        if (isPastDueDate || isOlderThan30Days) {
                            hasOverdueCredit = true
                        }
                    }
                    "REMBOURSEMENT" -> {
                        totalRemboursements += tx.grandTotal
                    }
                    "CORRECTION" -> {
                        totalCredits += tx.resteAPayer
                    }
                }
            }

            val soldeDu = maxOf(0L, totalCredits - totalRemboursements)
            val isOverdue = soldeDu > 0L && hasOverdueCredit
            val isOverLimit = client.plafondCredit > 0L && soldeDu > client.plafondCredit

            ClientWithBalance(
                client = client,
                soldeDu = soldeDu,
                derniereOperation = derniereOp,
                totalCredits = totalCredits,
                totalRemboursements = totalRemboursements,
                transactionCount = clientTxs.size,
                isOverdue = isOverdue,
                isOverLimit = isOverLimit
            )
        }
    }

    fun getTransactionsForClient(clientId: String): Flow<List<TransactionWithItems>> {
        return transactionDao.getTransactionsWithItemsForClient(clientId)
    }

    suspend fun getClientById(id: String): Client? = clientDao.getClientById(id)

    suspend fun getClientByCin(cin: String): Client? = clientDao.getClientByCin(cin)

    suspend fun insertClient(client: Client) = clientDao.insertClient(client)

    suspend fun updateClient(client: Client) = clientDao.updateClient(client)

    suspend fun archiveClient(id: String) = clientDao.archiveClient(id)

    /**
     * Enregistre un nouveau crédit multi-articles de manière atomique
     * avec support pour mandataire / émissaire et chaînage SHA-256.
     */
    suspend fun recordCreditTransaction(
        clientId: String,
        dateCredit: Long,
        dateRemboursementPrevue: Long?,
        grandTotal: Long,
        acompteVerse: Long,
        raison: String?,
        signatureUri: String?,
        items: List<Pair<String, Pair<Long, Double>>>,
        isEmissaire: Boolean = false,
        emissaireNom: String? = null,
        emissaireLien: String? = null,
        emissaireTelephone: String? = null,
        emissaireConfirmation: String? = null,
        emissairePhotoUri: String? = null
    ): TransactionEntity {
        return database.withTransaction {
            val lastTx = transactionDao.getLastTransaction()
            val previousHash = lastTx?.currentHash ?: CryptoSecurity.GENESIS_HASH

            val transactionId = UUID.randomUUID().toString()
            val currentHash = CryptoSecurity.calculateTransactionHash(
                previousHash = previousHash,
                clientId = clientId,
                dateCredit = dateCredit,
                grandTotal = grandTotal,
                signatureUri = signatureUri,
                emissaireNom = if (isEmissaire) emissaireNom else null,
                emissairePhotoUri = if (isEmissaire) emissairePhotoUri else null
            )

            val resteAPayer = maxOf(0L, grandTotal - acompteVerse)
            val statutPaiement = when {
                resteAPayer == 0L -> "SOLDE"
                acompteVerse > 0L -> "PARTIEL"
                else -> "NON_PAYE"
            }

            val txEntity = TransactionEntity(
                id = transactionId,
                clientId = clientId,
                type = "CREDIT",
                dateCredit = dateCredit,
                dateRemboursementPrevue = dateRemboursementPrevue,
                grandTotal = grandTotal,
                acompteVerse = acompteVerse,
                resteAPayer = resteAPayer,
                raison = raison,
                signatureUri = signatureUri,
                previousHash = previousHash,
                currentHash = currentHash,
                statutPaiement = statutPaiement,
                isEmissaire = isEmissaire,
                emissaireNom = emissaireNom,
                emissaireLien = emissaireLien,
                emissaireTelephone = emissaireTelephone,
                emissaireConfirmation = emissaireConfirmation,
                emissairePhotoUri = emissairePhotoUri,
                createdAt = System.currentTimeMillis()
            )

            transactionDao.insertTransaction(txEntity)

            val itemEntities = items.map { (designation, priceQty) ->
                val (pu, qty) = priceQty
                val totalLigne = (pu * qty).toLong()
                TransactionItemEntity(
                    id = UUID.randomUUID().toString(),
                    transactionId = transactionId,
                    designation = designation,
                    prixUnitaire = pu,
                    quantite = qty,
                    totalLigne = totalLigne
                )
            }

            if (itemEntities.isNotEmpty()) {
                transactionDao.insertTransactionItems(itemEntities)
            }

            txEntity
        }
    }

    /**
     * Enregistre un règlement / remboursement avec chaînage cryptographique
     * Calcule précisément le solde antérieur et le nouveau restant dû.
     */
    suspend fun recordPaymentTransaction(
        clientId: String,
        montantPaye: Long,
        datePaiement: Long = System.currentTimeMillis(),
        note: String? = null
    ): TransactionEntity {
        return database.withTransaction {
            val lastTx = transactionDao.getLastTransaction()
            val previousHash = lastTx?.currentHash ?: CryptoSecurity.GENESIS_HASH

            // Calcul du solde antérieur du client
            val clientTxs = transactionDao.getTransactionsForClientDirect(clientId)
            var totalCredits = 0L
            var totalRemboursements = 0L
            clientTxs.forEach { tx ->
                when (tx.type) {
                    "CREDIT", "CORRECTION" -> totalCredits += tx.resteAPayer
                    "REMBOURSEMENT" -> totalRemboursements += tx.grandTotal
                }
            }
            val soldeAvant = maxOf(0L, totalCredits - totalRemboursements)
            // Impossible de payer plus que la dette
            val montantEffectif = if (soldeAvant > 0L) minOf(montantPaye, soldeAvant) else montantPaye
            val soldeApres = maxOf(0L, soldeAvant - montantEffectif)

            val transactionId = UUID.randomUUID().toString()
            val currentHash = CryptoSecurity.calculateTransactionHash(
                previousHash = previousHash,
                clientId = clientId,
                dateCredit = datePaiement,
                grandTotal = montantEffectif,
                signatureUri = null
            )

            val txEntity = TransactionEntity(
                id = transactionId,
                clientId = clientId,
                type = "REMBOURSEMENT",
                dateCredit = datePaiement,
                dateRemboursementPrevue = null,
                grandTotal = montantEffectif,
                acompteVerse = soldeAvant, // Solde avant ce règlement
                resteAPayer = soldeApres,   // Nouveau solde restant dû
                raison = note ?: "Règlement par Espèces",
                signatureUri = null,
                previousHash = previousHash,
                currentHash = currentHash,
                statutPaiement = if (soldeApres == 0L) "SOLDE" else "PARTIEL",
                isEmissaire = false,
                emissaireNom = null,
                emissaireLien = null,
                emissaireTelephone = null,
                emissaireConfirmation = null,
                createdAt = System.currentTimeMillis()
            )

            transactionDao.insertTransaction(txEntity)
            txEntity
        }
    }

    /**
     * Exécute un audit cryptographique complet du registre
     */
    suspend fun runIntegrityAudit(): CryptoSecurity.AuditReport {
        val allChronological = transactionDao.getAllTransactionsChronological()
        return CryptoSecurity.verifyChain(allChronological)
    }

    /**
     * Génère le texte structuré pour envoi de reçu
     * Présentation en tableau avec prix unitaire pour crédit,
     * et décompte dette antérieure/réglé/restant dû pour règlement.
     */
    fun buildWhatsAppReceipt(
        shopName: String,
        client: Client,
        transaction: TransactionEntity,
        items: List<TransactionItemEntity>,
        currency: String = "Ar"
    ): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateStr = dateFormat.format(Date(transaction.dateCredit))
        val refCode = CryptoSecurity.generateReferenceCode(transaction.id, transaction.type)
        val shortHash = transaction.currentHash.take(12)
        val cinSection = if (!client.cin.isNullOrBlank()) "CIN : ${client.cin}\n" else ""
        val numberFormatter = java.text.NumberFormat.getNumberInstance(Locale.FRANCE)

        if (transaction.type == "REMBOURSEMENT") {
            val soldeAvant = transaction.acompteVerse
            val montantRegle = transaction.grandTotal
            val restantDu = transaction.resteAPayer
            val modeReglement = transaction.raison?.ifBlank { "Règlement par Espèces" } ?: "Règlement par Espèces"

            return """
═══════════════════════════
🧾 REÇU DE RÈGLEMENT - HITROSA
Boutique : $shopName
Réf : $refCode
Date : $dateStr
═══════════════════════════
Client Titulaire : ${client.nomComplet}
Tél : ${client.telephone}
${cinSection}Résidence : ${client.residence}
───────────────────────────
DÉTAIL DE L'OPÉRATION :
• $modeReglement
───────────────────────────
Dette Antérieure  : ${numberFormatter.format(soldeAvant)} $currency
Montant Réglé     : ${numberFormatter.format(montantRegle)} $currency
RESTANT DÛ        : ${numberFormatter.format(restantDu)} $currency
${if (restantDu == 0L) "✅ COMPTE CLIENT TOTALEMENT SOLDÉ\n" else ""}───────────────────────────
🔒 Sceau SHA-256 : $shortHash...
Reçu de paiement numérique certifié
═══════════════════════════
Ce carnet numérique est infalsifiable et fait foi.
""".trimIndent()
        } else {
            val itemsSection = if (items.isNotEmpty()) {
                val header = "Désignation          Qté x P.U.        Total\n───────────────────────────"
                val rows = items.joinToString("\n") { item ->
                    val qtyStr = if (item.quantite % 1.0 == 0.0) item.quantite.toInt().toString() else item.quantite.toString()
                    val puStr = "${numberFormatter.format(item.prixUnitaire)} $currency"
                    val totStr = "${numberFormatter.format(item.totalLigne)} $currency"
                    "• ${item.designation}\n  └─ $qtyStr x $puStr = $totStr"
                }
                "$header\n$rows"
            } else {
                "• ${transaction.raison ?: "Opération de crédit"}"
            }

            val echeanceSection = transaction.dateRemboursementPrevue?.let {
                val dueStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
                "📅 Échéance Convenue : $dueStr\n"
            } ?: ""

            val emissaireSection = if (transaction.isEmissaire && !transaction.emissaireNom.isNullOrBlank()) {
                """
🚶 RETRAIT PAR MANDATAIRE / ÉMISSAIRE :
• Nom du porteur : ${transaction.emissaireNom}
• Lien avec le client : ${transaction.emissaireLien ?: "Proche / Envoyé"}
${if (!transaction.emissaireTelephone.isNullOrBlank()) "• Tél. porteur : ${transaction.emissaireTelephone}\n" else ""}• Autorisation : ${transaction.emissaireConfirmation ?: "Accord confirmé"}
───────────────────────────
"""
            } else ""

            return """
═══════════════════════════
🧾 REÇU DE CRÉDIT - HITROSA
Boutique : $shopName
Réf : $refCode
Date : $dateStr
═══════════════════════════
Client Titulaire : ${client.nomComplet}
Tél : ${client.telephone}
${cinSection}Résidence : ${client.residence}
───────────────────────────
$emissaireSection DÉTAIL DES ARTICLES :
$itemsSection
───────────────────────────
Total Facture   : ${numberFormatter.format(transaction.grandTotal)} $currency
Acompte Versé   : ${numberFormatter.format(transaction.acompteVerse)} $currency
RESTANT DÛ      : ${numberFormatter.format(transaction.resteAPayer)} $currency
$echeanceSection───────────────────────────
🔒 Sceau SHA-256 : $shortHash...
${if (transaction.isEmissaire) "Signature du Porteur Enregistrée" else "Signature Numérique Client Enregistrée"}
═══════════════════════════
Ce carnet numérique est infalsifiable et fait foi.
""".trimIndent()
        }
    }

    /**
     * Génère un relevé de compte complet pour le client
     */
    fun buildWhatsAppStatement(
        shopName: String,
        client: Client,
        soldeDu: Long,
        transactions: List<TransactionWithItems>,
        currency: String = "Ar"
    ): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val dateToday = dateFormat.format(Date())
        val numberFormatter = java.text.NumberFormat.getNumberInstance(Locale.FRANCE)

        val txLines = transactions.take(10).joinToString("\n") { txWithItems ->
            val tx = txWithItems.transaction
            val dt = dateFormat.format(Date(tx.dateCredit))
            val emissaireTag = if (tx.isEmissaire && !tx.emissaireNom.isNullOrBlank()) " [via ${tx.emissaireNom}]" else ""
            if (tx.type == "CREDIT") {
                "• $dt [CRÉDIT] +${numberFormatter.format(tx.resteAPayer)} $currency$emissaireTag"
            } else {
                "• $dt [RÈGLEMENT] -${numberFormatter.format(tx.grandTotal)} $currency"
            }
        }

        return """
═══════════════════════════
📊 RELEVÉ DE COMPTE CLIENT
Boutique : $shopName
Date : $dateToday
═══════════════════════════
Client : ${client.nomComplet}
Tél : ${client.telephone}
Résidence : ${client.residence}
───────────────────────────
SOLDE ACTUEL DÛ : ${numberFormatter.format(soldeDu)} $currency
Statut : ${if (soldeDu > 0) "⚠️ EN DETTE" else "✅ COMPTE SOLDÉ"}
───────────────────────────
DERNIÈRES OPÉRATIONS :
$txLines
═══════════════════════════
Registre numérique sécurisé par chaînage SHA-256.
Merci de votre confiance !
""".trimIndent()
    }

    /**
     * Purge intégrale du registre et réinitialisation usine (pour démarrage à blanc).
     * Supprime toutes les données ajoutées par l'utilisateur et fichiers associés.
     * Notifie Room InvalidationTracker pour que l'interface se mette immédiatement à jour.
     */
    suspend fun clearAllData(filesDir: java.io.File? = null) = withContext(Dispatchers.IO) {
        try {
            val db = database.openHelper.writableDatabase
            db.execSQL("PRAGMA foreign_keys = OFF;")
            db.execSQL("DROP TRIGGER IF EXISTS prevent_transaction_delete;")
            db.execSQL("DROP TRIGGER IF EXISTS prevent_transaction_update;")
            db.execSQL("DROP TRIGGER IF EXISTS prevent_items_update;")
            database.clearAllTables()
            db.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS prevent_transaction_update
                BEFORE UPDATE ON transactions
                BEGIN
                    SELECT RAISE(FAIL, 'SÉCURITÉ : Un crédit enregistré est immuable et ne peut pas être modifié !');
                END;
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS prevent_transaction_delete
                BEFORE DELETE ON transactions
                BEGIN
                    SELECT RAISE(FAIL, 'SÉCURITÉ : Un crédit enregistré ne peut pas être supprimé !');
                END;
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS prevent_items_update
                BEFORE UPDATE ON transaction_items
                BEGIN
                    SELECT RAISE(FAIL, 'SÉCURITÉ : Les lignes de facture sont immuables !');
                END;
                """.trimIndent()
            )
            db.execSQL("PRAGMA foreign_keys = ON;")

            // Supprime les fichiers images et signatures créés localement
            filesDir?.listFiles()?.forEach { file ->
                if (file.isFile && (file.name.endsWith(".png") || file.name.endsWith(".jpg") || file.name.endsWith(".jpeg") || file.name.endsWith(".json"))) {
                    file.delete()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
