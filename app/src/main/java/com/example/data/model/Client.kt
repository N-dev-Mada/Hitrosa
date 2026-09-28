package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "clients"
)
data class Client(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val nom: String,
    val prenom: String? = null,
    val telephone: String = "",
    val residence: String,
    val cin: String? = null, // Optionnel / Cas particulier
    @ColumnInfo(name = "photo_uri")
    val photoUri: String? = null,
    @ColumnInfo(name = "photo_cin_uri")
    val photoCinUri: String? = null,
    @ColumnInfo(name = "empreinte_uri")
    val empreinteUri: String? = null,
    @ColumnInfo(name = "plafond_credit")
    val plafondCredit: Long = 0L,
    val note: String? = null,
    val statut: String = "ACTIF", // 'ACTIF' or 'ARCHIVE'
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "updated_at")
    val updatedAt: Long = System.currentTimeMillis()
) {
    val nomComplet: String
        get() = if (!prenom.isNullOrBlank()) "$nom $prenom" else nom
}
