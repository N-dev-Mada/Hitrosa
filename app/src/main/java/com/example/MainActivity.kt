package com.example

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.NewClientDialog
import com.example.ui.components.ReceiptSuccessDialog
import com.example.ui.screens.AuditScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ClientDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NewCreditScreen
import com.example.ui.screens.PaymentScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CarnetViewModel
import com.example.ui.viewmodel.Screen

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                CarnetApp()
            }
        }
    }
}

@Composable
fun CarnetApp(
    viewModel: CarnetViewModel = viewModel()
) {
    val isAppUnlocked by viewModel.isAppUnlocked.collectAsStateWithLifecycle()
    val isSecurityPinEnabled by viewModel.isSecurityPinEnabled.collectAsStateWithLifecycle()
    val securityPin by viewModel.securityPin.collectAsStateWithLifecycle()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsStateWithLifecycle()

    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val selectedClientId by viewModel.selectedClientId.collectAsStateWithLifecycle()
    val lastResult by viewModel.lastTransactionResult.collectAsStateWithLifecycle()
    val currency by viewModel.currency.collectAsStateWithLifecycle()
    val allClients by viewModel.allClientsWithBalances.collectAsStateWithLifecycle()

    var showNewClientDialog by remember { mutableStateOf(false) }

    // Initial starter demo data if fresh empty database
    var hasSeeded by remember { mutableStateOf(false) }
    LaunchedEffect(allClients.size) {
        if (!hasSeeded && allClients.isEmpty()) {
            hasSeeded = true
            viewModel.createClient(
                nom = "Diallo",
                prenom = "Amadou",
                telephone = "+221 77 543 21 00",
                residence = "Médina, Rue 22 x 15",
                cin = null,
                plafondCredit = 100000L,
                note = "Client habitué de confiance",
                onSuccess = { client ->
                    viewModel.recordCredit(
                        clientId = client.id,
                        dateCredit = System.currentTimeMillis() - (12L * 24 * 60 * 60 * 1000),
                        dateRemboursementPrevue = System.currentTimeMillis() + (3L * 24 * 60 * 60 * 1000),
                        grandTotal = 45000L,
                        acompteVerse = 15000L,
                        raison = "Sac de Riz 50kg + Bidon Huile 5L",
                        signatureUri = null,
                        items = listOf(
                            "Sac Riz Parfumé 50kg" to (22500L to 1.0),
                            "Bidon Huile 5L" to (6500L to 1.0),
                            "Carton Lait Concentré" to (16000L to 1.0)
                        ),
                        isEmissaire = false,
                        onSuccess = {}
                    )
                }
            )

            viewModel.createClient(
                nom = "Sow",
                prenom = "Fatou Bintou",
                telephone = "+221 70 888 99 11",
                residence = "Grand Yoff, près Mosquée",
                cin = null,
                plafondCredit = 75000L,
                note = "Vendeuse de beignets du quartier",
                onSuccess = { client ->
                    viewModel.recordCredit(
                        clientId = client.id,
                        dateCredit = System.currentTimeMillis() - (2L * 24 * 60 * 60 * 1000),
                        dateRemboursementPrevue = System.currentTimeMillis() + (10L * 24 * 60 * 60 * 1000),
                        grandTotal = 28000L,
                        acompteVerse = 5000L,
                        raison = "Farine et Sucre pour beignets",
                        signatureUri = null,
                        items = listOf(
                            "Sac Farine 25kg" to (14000L to 1.0),
                            "Sucre en Poudre 10kg" to (8000L to 1.0),
                            "Levure et Arômes" to (6000L to 1.0)
                        ),
                        isEmissaire = true,
                        emissaireNom = "Ibrahima Sow",
                        emissaireLien = "Frère",
                        emissaireTelephone = "+221 77 111 22 33",
                        emissaireConfirmation = "Appel téléphonique reçu de Fatou",
                        onSuccess = {}
                    )
                }
            )

            viewModel.createClient(
                nom = "Konaté",
                prenom = "Moussa",
                telephone = "+221 76 333 44 22",
                residence = "Parcelles Assainies, U. 14",
                cin = "1 992 1985 09876",
                plafondCredit = 50000L,
                note = "Client avec CIN enregistrée pour gros crédit"
            )
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        // Security Lock Screen check
        if (isSecurityPinEnabled && !isAppUnlocked) {
            AuthScreen(
                savedPin = securityPin,
                isPinConfigured = securityPin.length == 4,
                isBiometricEnabled = isBiometricEnabled,
                onAuthenticated = { viewModel.unlockApp() },
                onSetNewPin = { newPin ->
                    viewModel.setSecurityPin(newPin)
                    viewModel.unlockApp()
                }
            )
        } else {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    Screen.HOME -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToNewCredit = { viewModel.navigateTo(Screen.NEW_CREDIT) },
                        onNavigateToAudit = { viewModel.navigateTo(Screen.AUDIT) },
                        onSelectClient = { clientId -> viewModel.navigateTo(Screen.CLIENT_DETAIL, clientId) },
                        onOpenNewClientDialog = { showNewClientDialog = true }
                    )

                    Screen.CLIENT_DETAIL -> ClientDetailScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(Screen.HOME) },
                        onAddCredit = { clientId -> viewModel.navigateTo(Screen.NEW_CREDIT, clientId) },
                        onAddPayment = { clientId -> viewModel.navigateTo(Screen.NEW_PAYMENT, clientId) }
                    )

                    Screen.NEW_CREDIT -> NewCreditScreen(
                        viewModel = viewModel,
                        initialClientId = selectedClientId,
                        onBack = {
                            if (selectedClientId != null) viewModel.navigateTo(Screen.CLIENT_DETAIL, selectedClientId)
                            else viewModel.navigateTo(Screen.HOME)
                        },
                        onOpenNewClientDialog = { showNewClientDialog = true }
                    )

                    Screen.NEW_PAYMENT -> PaymentScreen(
                        viewModel = viewModel,
                        initialClientId = selectedClientId,
                        onBack = {
                            if (selectedClientId != null) viewModel.navigateTo(Screen.CLIENT_DETAIL, selectedClientId)
                            else viewModel.navigateTo(Screen.HOME)
                        }
                    )

                    Screen.AUDIT -> AuditScreen(
                        viewModel = viewModel,
                        onBack = { viewModel.navigateTo(Screen.HOME) }
                    )
                }
            }
        }
    }

    // Modal dialog for creating new client
    if (showNewClientDialog) {
        NewClientDialog(
            viewModel = viewModel,
            currency = currency,
            onDismiss = { showNewClientDialog = false },
            onClientCreated = { newClient ->
                viewModel.selectClient(newClient.id)
            }
        )
    }

    // Modal receipt dialog after saving transaction
    lastResult?.let { result ->
        ReceiptSuccessDialog(
            client = result.client,
            transaction = result.transaction,
            receiptText = result.receiptText,
            currency = currency,
            onDismiss = { viewModel.clearLastTransactionResult() }
        )
    }
}
