package com.example

import com.example.data.crypto.CryptoSecurity
import com.example.data.model.TransactionEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class ExampleUnitTest {

    @Test
    fun `sha256 calculation produces consistent hash`() {
        val hash1 = CryptoSecurity.calculateTransactionHash(
            previousHash = CryptoSecurity.GENESIS_HASH,
            clientId = "client-123",
            dateCredit = 1700000000000L,
            grandTotal = 25000L,
            signatureUri = "/path/sig.png",
            emissaireNom = null
        )
        val hash2 = CryptoSecurity.calculateTransactionHash(
            previousHash = CryptoSecurity.GENESIS_HASH,
            clientId = "client-123",
            dateCredit = 1700000000000L,
            grandTotal = 25000L,
            signatureUri = "/path/sig.png",
            emissaireNom = null
        )

        assertEquals(64, hash1.length)
        assertEquals(hash1, hash2)
    }

    @Test
    fun `verifyChain detects valid blockchain sequence`() {
        val clientId = UUID.randomUUID().toString()

        val tx1Hash = CryptoSecurity.calculateTransactionHash(
            previousHash = CryptoSecurity.GENESIS_HASH,
            clientId = clientId,
            dateCredit = 1000L,
            grandTotal = 10000L,
            signatureUri = null
        )
        val tx1 = TransactionEntity(
            id = "tx1",
            clientId = clientId,
            type = "CREDIT",
            dateCredit = 1000L,
            grandTotal = 10000L,
            resteAPayer = 10000L,
            previousHash = CryptoSecurity.GENESIS_HASH,
            currentHash = tx1Hash
        )

        val tx2Hash = CryptoSecurity.calculateTransactionHash(
            previousHash = tx1Hash,
            clientId = clientId,
            dateCredit = 2000L,
            grandTotal = 5000L,
            signatureUri = null
        )
        val tx2 = TransactionEntity(
            id = "tx2",
            clientId = clientId,
            type = "REMBOURSEMENT",
            dateCredit = 2000L,
            grandTotal = 5000L,
            resteAPayer = 5000L,
            previousHash = tx1Hash,
            currentHash = tx2Hash
        )

        val report = CryptoSecurity.verifyChain(listOf(tx1, tx2))
        assertTrue(report.isChainValid)
        assertEquals(2, report.totalBlocks)
    }

    @Test
    fun `verifyChain detects tampering in previous hash or amount`() {
        val clientId = UUID.randomUUID().toString()

        val tx1Hash = CryptoSecurity.calculateTransactionHash(
            previousHash = CryptoSecurity.GENESIS_HASH,
            clientId = clientId,
            dateCredit = 1000L,
            grandTotal = 10000L,
            signatureUri = null
        )
        val tx1 = TransactionEntity(
            id = "tx1",
            clientId = clientId,
            type = "CREDIT",
            dateCredit = 1000L,
            grandTotal = 10000L,
            resteAPayer = 10000L,
            previousHash = CryptoSecurity.GENESIS_HASH,
            currentHash = tx1Hash
        )

        // Tampered grandTotal without recomputing currentHash
        val tx2 = TransactionEntity(
            id = "tx2",
            clientId = clientId,
            type = "CREDIT",
            dateCredit = 2000L,
            grandTotal = 999999L, // Altered
            resteAPayer = 999999L,
            previousHash = tx1Hash,
            currentHash = "fakehash123456"
        )

        val report = CryptoSecurity.verifyChain(listOf(tx1, tx2))
        assertFalse(report.isChainValid)
        assertEquals(1, report.invalidBlockIndex)
    }
}
