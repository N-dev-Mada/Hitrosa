package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.WhatsAppHelper
import com.example.ui.theme.CreditRed
import com.example.ui.theme.CreditRedLight
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenLight
import com.example.ui.theme.PrimarySlate
import com.example.ui.viewmodel.CarnetViewModel
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditScreen(
    viewModel: CarnetViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val auditReport by viewModel.auditReport.collectAsStateWithLifecycle()
    val isAuditing by viewModel.isAuditing.collectAsStateWithLifecycle()
    val shopName by viewModel.shopName.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val shopPhone by viewModel.shopPhone.collectAsStateWithLifecycle()

    var editShopName by remember(shopName) { mutableStateOf(shopName) }
    var editCurrency by remember(currency) { mutableStateOf(currency) }
    var editShopPhone by remember(shopPhone) { mutableStateOf(shopPhone) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Sécurité & Registre SHA-256") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshAudit() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Ré-auditer",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = PrimarySlate,
                    titleContentColor = Color.White
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Integrity Status Banner
            item {
                val report = auditReport
                val isValid = report?.isChainValid ?: true
                val totalBlocks = report?.totalBlocks ?: 0

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isValid) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(CircleShape)
                                    .background(Color.White.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (isValid) Icons.Default.Security else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(28.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = if (isValid) "REGISTRE 100% INTÈGRE" else "ALERTE : ALTÉRATION DÉTECTÉE",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Text(
                                    text = "$totalBlocks bloc(s) audité(s) par chaînage SHA-256",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = if (isValid) {
                                "Tous les crédits et remboursements sont liés mathématiquement. Aucune modification ultérieure ou suppression directe n'est possible."
                            } else {
                                "Attention : Le bloc #${report?.invalidBlockIndex} a un hash incompatible avec la chaîne précédente. Une tentative de modification non autorisée a été détectée."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White.copy(alpha = 0.9f)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { viewModel.refreshAudit() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = if (isValid) Color(0xFF064E3B) else Color(0xFF7F1D1D)
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isAuditing) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Vérification en cours...")
                            } else {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Relancer l'Audit Cryptographique", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Append-Only Security Triggers Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "GARANTIES D'INFALSIFIABILITÉ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val guarantees = listOf(
                            "🔒 Triggers SQLite Append-Only : Interdiction stricte de UPDATE ou DELETE sur transactions et articles.",
                            "⛓️ Chaînage Cryptographique : Chaque opération intègre le SHA-256 de la précédente.",
                            "✍️ Preuve par Signature Tactile : Tracé manuscrit scellé dès la validation du crédit.",
                            "📱 Reçu avec Code Réf : Code unique 'CR-XXXX' vérifiable avec le client."
                        )

                        guarantees.forEach { g ->
                            Text(
                                text = g,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.padding(vertical = 3.dp)
                            )
                        }
                    }
                }
            }

            // Export & Backup
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SAUVEGARDE & EXPORT DU REGISTRE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Exportez l'intégralité du carnet et de la chaîne de preuves dans un fichier JSON pour sauvegarde sécurisée ou transmission juridique.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = {
                                scope.launch {
                                    val json = viewModel.exportLedgerJson()
                                    WhatsAppHelper.shareGeneral(context, json)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Exporter & Partager le Registre (JSON)")
                        }
                    }
                }
            }

            // Shop Configuration
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Store,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "PARAMÈTRES DU COMMERCE",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedTextField(
                            value = editShopName,
                            onValueChange = { editShopName = it },
                            label = { Text("Nom de la boutique / Commerce") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = editCurrency,
                                onValueChange = { editCurrency = it },
                                label = { Text("Devise (ex: Ar, EUR, USD)") },
                                singleLine = true,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp)
                            )

                            OutlinedTextField(
                                value = editShopPhone,
                                onValueChange = { editShopPhone = it },
                                label = { Text("Téléphone boutique") },
                                singleLine = true,
                                modifier = Modifier.weight(1.3f),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                viewModel.updateSettings(editShopName, editCurrency, editShopPhone)
                                Toast.makeText(context, "Paramètres enregistrés !", Toast.LENGTH_SHORT).show()
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Enregistrer les Paramètres")
                        }
                    }
                }
            }

            // Security PIN & Biometrics Management Card
            item {
                val isPinEnabled by viewModel.isSecurityPinEnabled.collectAsStateWithLifecycle()
                val currentPin by viewModel.securityPin.collectAsStateWithLifecycle()
                val isBioEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()
                var newPinInput by remember { mutableStateOf("") }
                var showPinEdit by remember { mutableStateOf(false) }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "VERROUILLAGE & ACCÈS SÉCURISÉ",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Switch PIN protection
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Protection par Code PIN (4 chiffres)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isPinEnabled) "Actif • Requis à l'ouverture" else "Désactivé • Accès direct",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isPinEnabled) PaidGreen else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            androidx.compose.material3.Switch(
                                checked = isPinEnabled,
                                onCheckedChange = { checked ->
                                    if (checked && currentPin.length != 4) {
                                        showPinEdit = true
                                    } else {
                                        viewModel.toggleSecurityPinEnabled(checked)
                                    }
                                }
                            )
                        }

                        // Switch Biometric
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Déverrouillage par Empreinte / Biométrie",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    text = if (isBioEnabled) "Activé" else "Désactivé",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            androidx.compose.material3.Switch(
                                checked = isBioEnabled,
                                onCheckedChange = { checked ->
                                    viewModel.toggleBiometricEnabled(checked)
                                }
                            )
                        }

                        // Change PIN section
                        Spacer(modifier = Modifier.height(12.dp))
                        if (!showPinEdit) {
                            OutlinedButton(
                                onClick = { showPinEdit = true },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(if (currentPin.isBlank()) "Définir un Code PIN" else "Modifier le Code PIN")
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "Nouveau Code PIN (4 chiffres) :",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                OutlinedTextField(
                                    value = newPinInput,
                                    onValueChange = { if (it.length <= 4 && it.all { c -> c.isDigit() }) newPinInput = it },
                                    label = { Text("Code à 4 chiffres") },
                                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                                        keyboardType = androidx.compose.ui.text.input.KeyboardType.NumberPassword
                                    ),
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    androidx.compose.material3.TextButton(onClick = { showPinEdit = false }) {
                                        Text("Annuler")
                                    }
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Button(
                                        onClick = {
                                            if (newPinInput.length == 4) {
                                                viewModel.setSecurityPin(newPinInput)
                                                newPinInput = ""
                                                showPinEdit = false
                                                Toast.makeText(context, "Code PIN mis à jour !", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Le code PIN doit comporter 4 chiffres", Toast.LENGTH_SHORT).show()
                                            }
                                        },
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Text("Valider")
                                    }
                                }
                            }
                        }

                        // Immediate lock button
                        if (isPinEnabled) {
                            Spacer(modifier = Modifier.height(10.dp))
                            OutlinedButton(
                                onClick = {
                                    viewModel.lockApp()
                                    Toast.makeText(context, "Application verrouillée", Toast.LENGTH_SHORT).show()
                                },
                                colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                                    contentColor = CreditRed
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Verrouiller l'Application Maintenant")
                            }
                        }
                    }
                }
            }

            // Blockchain Explorer (Blocks visual breakdown)
            val blocks = auditReport?.blocks ?: emptyList()
            item {
                Text(
                    text = "EXPLORATEUR DE LA CHAÎNE (${blocks.size} BLOCS)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (blocks.isEmpty()) {
                item {
                    Text(
                        text = "Aucune transaction enregistrée dans le registre.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(blocks) { block ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (block.isValid) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (block.isValid) PaidGreen else CreditRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Bloc #${block.index + 1} • [${block.transaction.type}]",
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                }

                                Text(
                                    text = if (block.isValid) "Certifié" else "ALTÉRÉ",
                                    color = if (block.isValid) PaidGreen else CreditRed,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }

                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Montant : ${block.transaction.grandTotal} $currency",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Text(
                                text = "Hash Actuel : ${block.transaction.currentHash}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "Hash Précédent : ${block.transaction.previousHash}",
                                style = MaterialTheme.typography.labelSmall,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 9.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}
