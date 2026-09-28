package com.example.ui.components

import android.graphics.Bitmap
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.model.Client
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PaidGreenLight
import com.example.ui.viewmodel.CarnetViewModel
import java.io.File

@Composable
fun NewClientDialog(
    viewModel: CarnetViewModel,
    currency: String = "Ar",
    onDismiss: () -> Unit,
    onClientCreated: (Client) -> Unit = {}
) {
    val context = LocalContext.current

    // Indispensable fields
    var nom by remember { mutableStateOf("") }
    var prenom by remember { mutableStateOf("") }
    var telephone by remember { mutableStateOf("") }
    var residence by remember { mutableStateOf("") }

    // Validation files (Photo & Empreinte)
    var photoUri by remember { mutableStateOf<String?>(null) }
    var empreinteUri by remember { mutableStateOf<String?>(null) }

    // Cas particulier (CIN - Optionnel)
    var cin by remember { mutableStateOf("") }
    var showCinField by remember { mutableStateOf(false) }

    // Settings
    var plafondInput by remember { mutableStateOf("50000") }
    var note by remember { mutableStateOf("") }

    // Camera launcher for profile photo
    val photoCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedPath = ImageHelper.saveBitmapToFile(context, bitmap, "client_photo")
            photoUri = savedPath
        }
    }

    // Photo picker for profile photo
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            val savedPath = ImageHelper.saveUriToFile(context, uri, "client_photo")
            photoUri = savedPath
        }
    }

    // Camera launcher for fingerprint (photo de l'empreinte papier)
    val fingerprintCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            val savedPath = ImageHelper.saveBitmapToFile(context, bitmap, "client_empreinte")
            empreinteUri = savedPath
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Nouveau Client (Carnet A)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Enregistrement au registre de crédit",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Section 1: Informations Indispensables
                Text(
                    text = "INFORMATIONS INDISPENSABLES *",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = nom,
                        onValueChange = { nom = it },
                        label = { Text("Nom de famille *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )

                    OutlinedTextField(
                        value = prenom,
                        onValueChange = { prenom = it },
                        label = { Text("Prénoms *") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    )
                }

                OutlinedTextField(
                    value = telephone,
                    onValueChange = { telephone = it },
                    label = { Text("Téléphone (WhatsApp / Appels) *") },
                    placeholder = { Text("ex: +221 77 123 45 67") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = residence,
                    onValueChange = { residence = it },
                    label = { Text("Lieu de résidence / Quartier *") },
                    placeholder = { Text("ex: Médina Rue 22, Cocody...") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Section 2: Validation Visuelle (Photo & Empreinte)
                Text(
                    text = "PREUVES & VALIDATION (RECOMMANDÉ)",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Profile Photo capture card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (photoUri != null && File(photoUri!!).exists()) {
                                AsyncImage(
                                    model = File(photoUri!!),
                                    contentDescription = "Photo client",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Photo ajoutée ✅", style = MaterialTheme.typography.labelSmall, color = PaidGreen, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Photo du client", style = MaterialTheme.typography.labelSmall)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                OutlinedButton(
                                    onClick = { photoCameraLauncher.launch() },
                                    modifier = Modifier.height(30.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text("Caméra", fontSize = 10.sp)
                                }
                                OutlinedButton(
                                    onClick = {
                                        photoPickerLauncher.launch(
                                            androidx.activity.result.PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    },
                                    modifier = Modifier.height(30.dp),
                                    shape = RoundedCornerShape(6.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                                ) {
                                    Text("Galerie", fontSize = 10.sp)
                                }
                            }
                        }
                    }

                    // Fingerprint capture card
                    Card(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            if (empreinteUri != null && File(empreinteUri!!).exists()) {
                                AsyncImage(
                                    model = File(empreinteUri!!),
                                    contentDescription = "Empreinte",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(RoundedCornerShape(6.dp))
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Empreinte ✅", style = MaterialTheme.typography.labelSmall, color = PaidGreen, fontWeight = FontWeight.Bold)
                            } else {
                                Icon(Icons.Default.Fingerprint, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Photo empreinte", style = MaterialTheme.typography.labelSmall)
                            }

                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedButton(
                                onClick = { fingerprintCameraLauncher.launch() },
                                modifier = Modifier.height(30.dp),
                                shape = RoundedCornerShape(6.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 6.dp)
                            ) {
                                Text("Photographier", fontSize = 10.sp)
                            }
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                // Section 3: Cas Particulier - CIN (Facultatif / Optionnel)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (showCinField || cin.isNotBlank()) MaterialTheme.colorScheme.surfaceVariant else Color.Transparent
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCinField = !showCinField },
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Badge,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Column {
                                    Text(
                                        text = "N° CIN / Pièce d'identité (Cas Particulier)",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "Facultatif pour les habitués de confiance",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = if (showCinField) "Masquer" else "+ Ajouter",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (showCinField || cin.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedTextField(
                                value = cin,
                                onValueChange = { cin = it },
                                label = { Text("Numéro de CIN (Optionnel)") },
                                placeholder = { Text("ex: 1 234 567 890 12") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            )
                        }
                    }
                }

                // Section 4: Plafond & Note
                OutlinedTextField(
                    value = plafondInput,
                    onValueChange = { plafondInput = it },
                    label = { Text("Plafond de crédit maximum autorisé") },
                    trailingIcon = { Text(currency, modifier = Modifier.padding(end = 12.dp)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note facultative (ex: métier, relation)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nom.isBlank() || residence.isBlank()) {
                        Toast.makeText(context, "Le nom et le quartier de résidence sont obligatoires", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (telephone.isBlank()) {
                        Toast.makeText(context, "Le numéro de téléphone est indispensable", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val plafond = plafondInput.toLongOrNull() ?: 0L
                    viewModel.createClient(
                        nom = nom,
                        prenom = prenom,
                        telephone = telephone,
                        residence = residence,
                        cin = cin.ifBlank { null },
                        plafondCredit = plafond,
                        note = note,
                        photoUri = photoUri,
                        empreinteUri = empreinteUri,
                        onSuccess = { created ->
                            Toast.makeText(context, "Client ${created.nomComplet} enregistré !", Toast.LENGTH_SHORT).show()
                            onClientCreated(created)
                            onDismiss()
                        }
                    )
                },
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Enregistrer le Client")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    )
}
