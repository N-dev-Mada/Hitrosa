package com.example.ui.screens

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
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
import com.example.ui.components.TouchSignaturePad
import com.example.ui.theme.CreditRed
import com.example.ui.theme.CreditRedContainer
import com.example.ui.theme.CreditRedLight
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenContainer
import com.example.ui.theme.WarningAmber
import com.example.ui.theme.WarningAmberLight
import com.example.ui.viewmodel.CarnetViewModel
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InvoiceItemDraft(
    var designation: String = "",
    var prixUnitaire: String = "",
    var quantite: String = "1"
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun NewCreditScreen(
    viewModel: CarnetViewModel,
    initialClientId: String?,
    onBack: () -> Unit,
    onOpenNewClientDialog: () -> Unit,
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

    // Emissaire / Mandataire state
    var isEmissaire by remember { mutableStateOf(false) }
    var emissaireNom by remember { mutableStateOf("") }
    var emissaireLien by remember { mutableStateOf("Enfant") }
    var emissaireTelephone by remember { mutableStateOf("") }
    var emissaireConfirmation by remember { mutableStateOf("Appel téléphonique reçu du client") }

    // Due date offset
    var dueDaysOffset by remember { mutableStateOf(15) }
    val now = remember { System.currentTimeMillis() }
    val dueDate = remember(dueDaysOffset) { now + (dueDaysOffset * 24L * 60 * 60 * 1000) }

    // Invoice items draft
    val items = remember {
        mutableStateListOf(
            InvoiceItemDraft(designation = "Marchandises diverses", prixUnitaire = "", quantite = "1")
        )
    }

    var acompteInput by remember { mutableStateOf("") }
    var raisonInput by remember { mutableStateOf("") }
    var signatureUri by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    // Computations
    val grandTotal = items.sumOf { item ->
        val pu = item.prixUnitaire.toLongOrNull() ?: 0L
        val qty = item.quantite.toDoubleOrNull() ?: 0.0
        (pu * qty).toLong()
    }
    val acompte = acompteInput.toLongOrNull() ?: 0L
    val resteAPayer = maxOf(0L, grandTotal - acompte)

    val currentSolde = selectedClient?.soldeDu ?: 0L
    val newTotalDebt = currentSolde + resteAPayer
    val plafond = selectedClient?.client?.plafondCredit ?: 0L
    val willExceedPlafond = plafond > 0 && newTotalDebt > plafond

    val numberFormatter = NumberFormat.getNumberInstance(Locale.FRANCE)

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Accorder un Crédit",
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
            // 1. Client Selection Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "1. CLIENT TITULAIRE DU CARNET",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onOpenNewClientDialog()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PersonAdd,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(13.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Nouveau",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

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
                                            Column {
                                                Text(cwb.client.nomComplet, fontWeight = FontWeight.Bold)
                                                Text(
                                                    "${cwb.client.residence} • Dû: ${numberFormatter.format(cwb.soldeDu)} $currency",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
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

                        // Client Balance Summary Strip
                        if (selectedClient != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Solde dû actuel : ${numberFormatter.format(selectedClient.soldeDu)} $currency",
                                            style = MaterialTheme.typography.bodySmall,
                                            fontWeight = FontWeight.Bold,
                                            color = if (selectedClient.soldeDu > 0) CreditRed else PaidGreen
                                        )
                                        Text(
                                            text = "Tél: ${selectedClient.client.telephone.orEmpty().ifBlank { "Non renseigné" }}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }

                                    if (plafond > 0) {
                                        Text(
                                            text = "Plafond: ${numberFormatter.format(plafond)} $currency",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // 2. Emissaire / Mandataire Toggle Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(
                                            if (isEmissaire) Color(0xFFFEF3C7) else MaterialTheme.colorScheme.surfaceVariant
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.TwoWheeler,
                                        contentDescription = null,
                                        tint = if (isEmissaire) WarningAmber else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Retiré par un mandataire ?",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Un proche envoyé par le client habitué",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Switch(
                                checked = isEmissaire,
                                onCheckedChange = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    isEmissaire = it
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = WarningAmber
                                )
                            )
                        }

                        AnimatedVisibility(
                            visible = isEmissaire,
                            enter = expandVertically() + fadeIn(),
                            exit = shrinkVertically() + fadeOut()
                        ) {
                            Column(modifier = Modifier.padding(top = 12.dp)) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = emissaireNom,
                                    onValueChange = { emissaireNom = it },
                                    label = { Text("Nom complet du mandataire *") },
                                    placeholder = { Text("Ex: Ousmane Sow") },
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                Text(
                                    text = "Lien avec le client titulaire :",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                val liens = listOf("Enfant", "Frère/Sœur", "Conjoint(e)", "Employé(e)", "Voisin(e)", "Ami(e)")
                                LazyRow(
                                    modifier = Modifier.padding(vertical = 6.dp),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    items(liens.size) { index ->
                                        val lien = liens[index]
                                        FilterChip(
                                            selected = emissaireLien == lien,
                                            onClick = { emissaireLien = lien },
                                            label = { Text(lien) },
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                OutlinedTextField(
                                    value = emissaireTelephone,
                                    onValueChange = { emissaireTelephone = it },
                                    label = { Text("Téléphone du mandataire (facultatif)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                    singleLine = true,
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }
            }

            // 3. Articles & Lignes de Facture Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "2. ARTICLES & MARCHANDISES (${items.size})",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )

                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    items.add(InvoiceItemDraft("", "", "1"))
                                },
                                shape = RoundedCornerShape(10.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Ajouter article", style = MaterialTheme.typography.labelSmall)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        items.forEachIndexed { index, draft ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .padding(10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    OutlinedTextField(
                                        value = draft.designation,
                                        onValueChange = { draft.designation = it },
                                        placeholder = { Text("Article (ex: Sac de riz, Sucre...)") },
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )

                                    if (items.size > 1) {
                                        IconButton(
                                            onClick = {
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                items.removeAt(index)
                                            },
                                            modifier = Modifier.size(40.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.DeleteOutline,
                                                contentDescription = "Supprimer la ligne",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    OutlinedTextField(
                                        value = draft.prixUnitaire,
                                        onValueChange = { draft.prixUnitaire = it },
                                        placeholder = { Text("Prix unit. ($currency)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1.3f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )

                                    OutlinedTextField(
                                        value = draft.quantite,
                                        onValueChange = { draft.quantite = it },
                                        placeholder = { Text("Qté") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        singleLine = true,
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(0.7f),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                                            unfocusedContainerColor = MaterialTheme.colorScheme.surface
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 4. Acompte & Échéance Card (with Quick Amount Chips)
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "3. ACOMPTE & ÉCHÉANCE CONVENUE",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = acompteInput,
                            onValueChange = { acompteInput = it },
                            label = { Text("Acompte versé immédiatement ($currency)") },
                            placeholder = { Text("0") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Quick Acompte Chips
                        Spacer(modifier = Modifier.height(6.dp))
                        val quickAcomptes = listOf(0L, 2000L, 5000L, 10000L, 25000L)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(quickAcomptes.size) { i ->
                                val amt = quickAcomptes[i]
                                Surface(
                                    color = if (acompte == amt) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainer,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        acompteInput = if (amt > 0) amt.toString() else ""
                                    }
                                ) {
                                    Text(
                                        text = if (amt == 0L) "Aucun acompte" else "${numberFormatter.format(amt)} $currency",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (acompte == amt) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Échéance selector
                        Text(
                            text = "Délai de remboursement promis :",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))

                        val daysOptions = listOf(7, 15, 30, 45)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            daysOptions.forEach { d ->
                                FilterChip(
                                    selected = dueDaysOffset == d,
                                    onClick = {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        dueDaysOffset = d
                                    },
                                    label = { Text("+$d j") },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        val dueFormatted = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dueDate))
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Date limite : $dueFormatted",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = raisonInput,
                            onValueChange = { raisonInput = it },
                            label = { Text("Note facultative / Référence") },
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // 5. Financial Summary Card
            item {
                ElevatedCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.elevatedCardColors(
                        containerColor = CreditRedContainer
                    ),
                    elevation = CardDefaults.elevatedCardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total des articles :",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${numberFormatter.format(grandTotal)} $currency",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (acompte > 0L) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Acompte versé :",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = PaidGreen
                                )
                                Text(
                                    text = "-${numberFormatter.format(acompte)} $currency",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = PaidGreen
                                )
                            }
                        }

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 8.dp),
                            color = CreditRed.copy(alpha = 0.2f)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NET À PAYER (Dette) :",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.ExtraBold,
                                color = CreditRed
                            )
                            Text(
                                text = "${numberFormatter.format(resteAPayer)} $currency",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Black,
                                color = CreditRed
                            )
                        }

                        // Overlimit Warning
                        if (willExceedPlafond) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Surface(
                                color = WarningAmberLight,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = WarningAmber,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Avertissement : Nouveau solde prévu (${numberFormatter.format(newTotalDebt)} $currency) supérieur au plafond fixé (${numberFormatter.format(plafond)} $currency).",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = WarningAmber,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 6. Touch Signature Pad
            item {
                TouchSignaturePad(
                    onSignatureCaptured = { path ->
                        signatureUri = path
                    },
                    hasSignature = signatureUri != null
                )
            }

            // 7. Validation / Submit Button
            item {
                Button(
                    onClick = {
                        if (selectedClientId == null) {
                            Toast.makeText(context, "Veuillez choisir un client", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (isEmissaire && emissaireNom.isBlank()) {
                            Toast.makeText(context, "Veuillez renseigner le nom du mandataire", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (grandTotal <= 0L) {
                            Toast.makeText(context, "Veuillez saisir au moins un article avec son prix", Toast.LENGTH_SHORT).show()
                            return@Button
                        }

                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isSubmitting = true
                        val validItems = items.mapNotNull { item ->
                            val pu = item.prixUnitaire.toLongOrNull() ?: 0L
                            val qty = item.quantite.toDoubleOrNull() ?: 0.0
                            if (item.designation.isNotBlank() && pu > 0) {
                                item.designation.trim() to (pu to qty)
                            } else null
                        }

                        viewModel.recordCredit(
                            clientId = selectedClientId!!,
                            dateCredit = now,
                            dateRemboursementPrevue = dueDate,
                            grandTotal = grandTotal,
                            acompteVerse = acompte,
                            raison = raisonInput.ifBlank { null },
                            signatureUri = signatureUri,
                            items = validItems,
                            isEmissaire = isEmissaire,
                            emissaireNom = if (isEmissaire) emissaireNom.trim() else null,
                            emissaireLien = if (isEmissaire) emissaireLien else null,
                            emissaireTelephone = if (isEmissaire) emissaireTelephone.ifBlank { null } else null,
                            emissaireConfirmation = if (isEmissaire) emissaireConfirmation else null,
                            onSuccess = {
                                isSubmitting = false
                                Toast.makeText(context, "Crédit scellé avec succès !", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                        )
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CreditRed),
                    shape = RoundedCornerShape(16.dp),
                    enabled = !isSubmitting,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Scellement en cours...", fontWeight = FontWeight.Bold)
                    } else {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isEmissaire) "Sceller avec Signature Mandataire" else "Valider & Sceller le Crédit (SHA-256)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }
        }
    }
}
