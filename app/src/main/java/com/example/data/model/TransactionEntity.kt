package com.example.data.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = Client::class,
            parentColumns = ["id"],
            childColumns = ["client_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("client_id")
    ]
)
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    @ColumnInfo(name = "client_id")
    val clientId: String,
    val type: String, // 'CREDIT', 'REMBOURSEMENT', 'CORRECTION'
    @ColumnInfo(name = "date_credit")
    val dateCredit: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "date_remboursement_prevue")
    val dateRemboursementPrevue: Long? = null,
    @ColumnInfo(name = "grand_total")
    val grandTotal: Long = 0L,
    @ColumnInfo(name = "acompte_verse")
    val acompteVerse: Long = 0L,
    @ColumnInfo(name = "reste_a_payer")
    val resteAPayer: Long = 0L,
    val raison: String? = null,
    @ColumnInfo(name = "signature_uri")
    val signatureUri: String? = null,
    @ColumnInfo(name = "previous_hash")
    val previousHash: String,
    @ColumnInfo(name = "current_hash")
    val currentHash: String,
    @ColumnInfo(name = "statut_paiement")
    val statutPaiement: String = "NON_PAYE", // 'NON_PAYE', 'PARTIEL', 'SOLDE'
    @ColumnInfo(name = "is_emissaire")
    val isEmissaire: Boolean = false,
    @ColumnInfo(name = "emissaire_nom")
    val emissaireNom: String? = null,
    @ColumnInfo(name = "emissaire_lien")
    val emissaireLien: String? = null,
    @ColumnInfo(name = "emissaire_telephone")
    val emissaireTelephone: String? = null,
    @ColumnInfo(name = "emissaire_confirmation")
    val emissaireConfirmation: String? = null,
    @ColumnInfo(name = "emissaire_photo_uri")
    val emissairePhotoUri: String? = null,
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)
