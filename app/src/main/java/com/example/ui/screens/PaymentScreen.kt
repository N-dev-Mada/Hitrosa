package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.theme.CreditRed
import com.example.ui.theme.CreditRedContainer
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.PaidGreenLight
import com.example.ui.viewmodel.CarnetViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    viewModel: CarnetViewModel,
    initialClientId: String?,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onBack)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    val allClients by viewModel.allClientsWithBalances.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()

    var selectedClientId by remember { mutableStateOf(initialClientId ?: allClients.firstOrNull()?.client?.id) }
    var clientDropdownExpanded by remember { mutableStateOf(false) }

    val selectedClient = allClients.find { it.client.id == selectedClientId }
    val soldeActuel = selectedClient?.soldeDu ?: 0L

    var montantInput by remember { mutableStateOf("") }
    var noteInput by remember { mutableStateOf("Règlement en espèces") }
    var isSubmitting by remember { mutableStateOf(false) }

    val montantPaye = montantInput.toLongOrNull() ?: 0L
    val soldeRestantApres = maxOf(0L, soldeActuel - montantPaye)
    val isFullyCleared = soldeActuel > 0 && montantPaye >= soldeActuel
    val numberFormatter = NumberFormat.getNumberInstance(Locale.FRANCE)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Enregistrer un Règlement",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Retour"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. Client selection
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "CLIENT CONCERNÉ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        ExposedDropdownMenuBox(
                            expanded = clientDropdownExpanded,
                            onExpandedChange = { clientDropdownExpanded = it }
                        ) {
                            OutlinedTextField(
                                value = selectedClient?.client?.nomComplet ?: "Sélectionnez un client...",
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = clientDropdownExpanded) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                )
                            )

                            ExposedDropdownMenu(
                                expanded = clientDropdownExpanded,
                                onDismissRequest = { clientDropdownExpanded = false }
                            ) {
                                allClients.forEach { cwb ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Text(cwb.client.nomComplet, fontWeight = FontWeight.Bold)
                                                    Text(
                                                        cwb.client.residence,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                                    )
                                                }
                                                Text(
                                                    text = "${numberFormatter.format(cwb.soldeDu)} $currency",
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = if (cwb.soldeDu > 0) CreditRed else PaidGreen
                                                )
                                            }
                                        },
                                        onClick = {
                                            selectedClientId = cwb.client.id
                                            clientDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        // Current Debt Banner & Quick Solder Action
                        Spacer(modifier = Modifier.height(12.dp))
                        Surface(
                            color = if (soldeActuel > 0) CreditRedContainer else PaidGreenContainer,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = "Dette actuelle inscrite au carnet :",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "${numberFormatter.format(soldeActuel)} $currency",
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Black,
                                        color = if (soldeActuel > 0) CreditRed else PaidGreen
                                    )
                                }

                                if (soldeActuel > 0) {
                                    Button(
                                        onClick = {
                                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                            montantInput = soldeActuel.toString()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ),
                                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text("Tout solder", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Amount Input & Quick Chips
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "MONTANT VERSÉ",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = montantInput,
                            onValueChange = { montantInput = it },
                            label = { Text("Montant reçu en paiement ($currency) *") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = MaterialTheme.colorScheme.surface,
                                unfocusedContainerColor = MaterialTheme.colorScheme.surface
                            )
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick amount suggestions
                        val suggestions = listOf(1000L, 2000L, 5000L, 10000L, 25000L, 50000L)
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(suggestions.size) { i ->
                                val amt = suggestions[i]
                                Surface(
                                    color = if (montantPaye == amt) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        montantInput = amt.toString()
                                    }
                                ) {
                                    Text(
                                        text = "+${numberFormatter.format(amt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (montantPaye == amt) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Payment mode
                        Text(
                            text = "MODE DE RÈGLEMENT",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        val modes = listOf("Espèces", "Wave", "Orange Money", "MTN Money", "Virement")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(modes.size) { i ->
                                val mode = modes[i]
                                val selected = noteInput.contains(mode)
                                FilterChip(
                                    selected = selected,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        noteInput = "Règlement par $mode"
                                    },
                                    label = { Text(mode) },
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = noteInput,
                            onValueChange = { noteInput = it },
                            label = { Text("Justificatif / Note") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 3. Calculation Preview Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = if (isFullyCleared) PaidGreenContainer else MaterialTheme.colorScheme.surface
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SIMULATION DU NOUVEAU SOLDE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (isFullyCleared) PaidGreen else MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Dette initiale :", style = MaterialTheme.typography.bodyMedium)
                            Text("${numberFormatter.format(soldeActuel)} $currency", style = MaterialTheme.typography.bodyMedium)
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Règlement appliqué :", style = MaterialTheme.typography.bodyMedium, color = PaidGreen)
                            Text("-${numberFormatter.format(montantPaye)} $currency", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = PaidGreen)
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = MaterialTheme.colorScheme.outlineVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Nouveau solde restant :",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (soldeRestantApres == 0L) "COMPTE SOLDÉ (0 $currency)" else "${numberFormatter.format(soldeRestantApres)} $currency",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = if (soldeRestantApres == 0L) PaidGreen else CreditRed
                            )
                        }
                    }
                }
            }

            // 4. Submit Button
            item {
                Button(
                    onClick = {
                        if (selectedClientId == null) {
                            Toast.makeText(context, "Veuillez sélectionner un client", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (montantPaye <= 0L) {
                            Toast.makeText(context, "Veuillez saisir un montant supérieur à zéro", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isSubmitting = true
                        viewModel.recordPayment(
                            clientId = selectedClientId!!,
                            montant = montantPaye,
                            note = noteInput.ifBlank { null },
                            onSuccess = {
                                isSubmitting = false
                                Toast.makeText(context, "Règlement scellé et enregistré !", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PaidGreen),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Enregistrement scellé...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Valider le Règlement (Sceller SHA-256)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
