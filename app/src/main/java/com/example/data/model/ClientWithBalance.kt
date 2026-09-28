package com.example.data.model

data class ClientWithBalance(
    val client: Client,
    val soldeDu: Long,
    val derniereOperation: Long? = null,
    val totalCredits: Long = 0L,
    val totalRemboursements: Long = 0L,
    val transactionCount: Int = 0,
    val isOverdue: Boolean = false,
    val isOverLimit: Boolean = false
)
