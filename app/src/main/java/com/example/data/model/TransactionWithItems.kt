package com.example.data.model

import androidx.room.Embedded
import androidx.room.Relation

data class TransactionWithItems(
    @Embedded
    val transaction: TransactionEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "transaction_id"
    )
    val items: List<TransactionItemEntity> = emptyList()
)
