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
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ClientDetailScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NewCreditScreen
import com.example.ui.screens.PaymentScreen
import com.example.ui.screens.SettingsScreen
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
                        onNavigateToSettings = { viewModel.navigateTo(Screen.SETTINGS) },
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

                    Screen.SETTINGS -> SettingsScreen(
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
