package com.example.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.CreditRed
import com.example.ui.theme.PaidGreen
import com.example.ui.theme.PrimarySlate
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AuthScreen(
    savedPin: String,
    isPinConfigured: Boolean,
    isBiometricEnabled: Boolean,
    onAuthenticated: () -> Unit,
    onSetNewPin: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val scope = rememberCoroutineScope()

    var enteredPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var isSettingUpNewPin by remember { mutableStateOf(!isPinConfigured) }
    var isConfirmingNewPin by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        scope.launch {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -20f at 50
                    20f at 100
                    -15f at 150
                    15f at 200
                    -10f at 250
                    10f at 300
                    0f at 400
                }
            )
        }
    }

    fun launchBiometrics() {
        if (activity == null || !isBiometricEnabled) return
        try {
            val biometricManager = BiometricManager.from(context)
            val authenticators = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.R) {
                BiometricManager.Authenticators.BIOMETRIC_STRONG or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            } else {
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            }

            if (biometricManager.canAuthenticate(authenticators) == BiometricManager.BIOMETRIC_SUCCESS) {
                val executor = ContextCompat.getMainExecutor(context)
                val prompt = BiometricPrompt(
                    activity,
                    executor,
                    object : BiometricPrompt.AuthenticationCallback() {
                        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                            super.onAuthenticationSucceeded(result)
                            onAuthenticated()
                        }

                        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                            super.onAuthenticationError(errorCode, errString)
                            // Error or user cancelled, stays on PIN screen
                        }
                    }
                )

                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Authentification Hitrosa")
                    .setSubtitle("Déverrouillez pour accéder au carnet financier")
                    .setAllowedAuthenticators(authenticators)
                    .build()

                prompt.authenticate(promptInfo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Auto-launch biometric on start if configured
    LaunchedEffect(isPinConfigured, isBiometricEnabled) {
        if (isPinConfigured && isBiometricEnabled) {
            launchBiometrics()
        }
    }

    fun onDigitPress(digit: String) {
        errorMessage = null
        if (enteredPin.length < 4) {
            val newPin = enteredPin + digit
            enteredPin = newPin

            if (newPin.length == 4) {
                if (isSettingUpNewPin) {
                    if (!isConfirmingNewPin) {
                        // Move to confirm stage
                        confirmPin = newPin
                        enteredPin = ""
                        isConfirmingNewPin = true
                    } else {
                        // Verify confirmation
                        if (newPin == confirmPin) {
                            onSetNewPin(newPin)
                            Toast.makeText(context, "Code PIN de sécurité configuré !", Toast.LENGTH_SHORT).show()
                            onAuthenticated()
                        } else {
                            errorMessage = "Les codes ne correspondent pas"
                            triggerShake()
                            enteredPin = ""
                        }
                    }
                } else {
                    // Check against saved PIN
                    if (newPin == savedPin) {
                        onAuthenticated()
                    } else {
                        errorMessage = "Code PIN incorrect"
                        triggerShake()
                        enteredPin = ""
                    }
                }
            }
        }
    }

    fun onBackspacePress() {
        if (enteredPin.isNotEmpty()) {
            enteredPin = enteredPin.dropLast(1)
            errorMessage = null
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = PrimarySlate
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Header Section
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(top = 40.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = PaidGreen,
                        modifier = Modifier.size(40.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Hitrosa Sécurité",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(6.dp))

                val subtitle = when {
                    isSettingUpNewPin && !isConfirmingNewPin -> "Créez votre code PIN secret à 4 chiffres"
                    isSettingUpNewPin && isConfirmingNewPin -> "Confirmez votre code PIN à 4 chiffres"
                    else -> "Saisissez votre code PIN pour déverrouiller"
                }

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )
            }

            // PIN Dots Indicator Section with Shake animation
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(4) { index ->
                        val isFilled = index < enteredPin.length
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(if (isFilled) PaidGreen else Color.Transparent)
                                .border(
                                    width = 2.dp,
                                    color = if (isFilled) PaidGreen else Color(0xFF64748B),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage.orEmpty(),
                        style = MaterialTheme.typography.bodySmall,
                        color = CreditRed,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }
            }

            // Numeric Keypad & Biometric
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val keys = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("BIO", "0", "DEL")
                )

                keys.forEach { row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        row.forEach { key ->
                            when (key) {
                                "BIO" -> {
                                    if (isBiometricEnabled && !isSettingUpNewPin) {
                                        IconButton(
                                            onClick = { launchBiometrics() },
                                            modifier = Modifier.size(68.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Fingerprint,
                                                contentDescription = "Biométrie",
                                                tint = PaidGreen,
                                                modifier = Modifier.size(34.dp)
                                            )
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.size(68.dp))
                                    }
                                }

                                "DEL" -> {
                                    IconButton(
                                        onClick = { onBackspacePress() },
                                        modifier = Modifier.size(68.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.Backspace,
                                            contentDescription = "Effacer",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                }

                                else -> {
                                    Box(
                                        modifier = Modifier
                                            .size(68.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF1E293B))
                                            .clickable { onDigitPress(key) },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = key,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // First time skip option if user wants to set it up later
                if (isSettingUpNewPin) {
                    Spacer(modifier = Modifier.height(10.dp))
                    TextButton(onClick = { onAuthenticated() }) {
                        Text(
                            text = "Configurer plus tard (Passer)",
                            color = Color(0xFF94A3B8),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}
