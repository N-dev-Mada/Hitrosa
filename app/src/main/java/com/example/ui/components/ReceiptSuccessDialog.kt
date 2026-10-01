package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.crypto.CryptoSecurity
import com.example.data.model.Client
import com.example.data.model.TransactionEntity
import com.example.ui.theme.CreditRed
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenLight
import java.text.NumberFormat
import java.util.Locale

@Composable
fun ReceiptSuccessDialog(
    client: Client,
    transaction: TransactionEntity,
    receiptText: String,
    currency: String = "Ar",
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val refCode = CryptoSecurity.generateReferenceCode(transaction.id, transaction.type)
    val numberFormatter = NumberFormat.getNumberInstance(Locale.FRANCE)
    val isCredit = transaction.type == "CREDIT"

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    WhatsAppHelper.shareGeneral(context, receiptText, "Partager le reçu")
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCredit) MaterialTheme.colorScheme.primary else PaidGreen
                ),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Share,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Partager le reçu", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Fermer")
            }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (isCredit) MaterialTheme.colorScheme.primaryContainer else PaidGreenLight),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (isCredit) MaterialTheme.colorScheme.primary else PaidGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = if (isCredit) "Crédit Scellé !" else "Règlement Enregistré !",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Réf. $refCode",
                        style = MaterialTheme.typography.labelSmall,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "L'opération a été enregistrée de manière immuable avec son empreinte cryptographique SHA-256.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Client :", style = MaterialTheme.typography.bodySmall)
                            Text(
                                client.nomComplet,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (isCredit) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Total Facture :", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${numberFormatter.format(transaction.grandTotal)} $currency",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Acompte payé :", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${numberFormatter.format(transaction.acompteVerse)} $currency",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("RESTANT DÛ :", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = CreditRed)
                                Text(
                                    "${numberFormatter.format(transaction.resteAPayer)} $currency",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = CreditRed
                                )
                            }
                        } else {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Mode :", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    transaction.raison ?: "Espèces",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Dette Antérieure :", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "${numberFormatter.format(transaction.acompteVerse)} $currency",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("Montant Réglé :", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = PaidGreen)
                                Text(
                                    "-${numberFormatter.format(transaction.grandTotal)} $currency",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PaidGreen
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    "RESTANT DÛ :",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (transaction.resteAPayer == 0L) PaidGreen else CreditRed
                                )
                                Text(
                                    if (transaction.resteAPayer == 0L) "0 $currency (SOLDÉ ✅)" else "${numberFormatter.format(transaction.resteAPayer)} $currency",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (transaction.resteAPayer == 0L) PaidGreen else CreditRed
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = PaidGreen,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Sceau SHA-256 : ${transaction.currentHash.take(16)}...",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = PaidGreen,
                                modifier = Modifier.padding(start = 4.dp)
                            )
                        }
                    }
                }
            }
        }
    )
}
