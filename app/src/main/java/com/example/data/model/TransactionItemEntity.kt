package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "transaction_items",
    foreignKeys = [
        ForeignKey(
            entity = TransactionEntity::class,
            parentColumns = ["id"],
            childColumns = ["transaction_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("transaction_id")
    ]
)
data class TransactionItemEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "transaction_id")
    val transactionId: String,
    val designation: String,
    @ColumnInfo(name = "prix_unitaire")
    val prixUnitaire: Long,
    val quantite: Double,
    @ColumnInfo(name = "total_ligne")
    val totalLigne: Long
)
