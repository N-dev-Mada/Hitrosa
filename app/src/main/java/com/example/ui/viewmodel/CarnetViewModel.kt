package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.crypto.CryptoSecurity
import com.example.data.db.AppDatabase
import com.example.data.model.Client
import com.example.data.model.ClientWithBalance
import com.example.data.model.TransactionEntity
import com.example.data.model.TransactionItemEntity
import com.example.data.model.TransactionWithItems
import com.example.data.repository.LedgerRepository
import com.example.data.repository.UserPreferencesRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

enum class Screen {
    HOME,
    CLIENT_DETAIL,
    NEW_CREDIT,
    NEW_PAYMENT,
    AUDIT
}

enum class ClientFilter {
    ALL,
    DEBTORS_ONLY,
    OVERDUE_ONLY,
    OVER_LIMIT_ONLY
}

data class PostTransactionResult(
    val client: Client,
    val transaction: TransactionEntity,
    val receiptText: String
)

class CarnetViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = LedgerRepository(AppDatabase.getInstance(application))
    private val preferencesRepository = UserPreferencesRepository(application)

    // Security & App Lock State
    private val _isAppUnlocked = MutableStateFlow(false)
    val isAppUnlocked: StateFlow<Boolean> = _isAppUnlocked.asStateFlow()

    val isSecurityPinEnabled: StateFlow<Boolean> = preferencesRepository.isSecurityPinEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = false
        )

    val securityPin: StateFlow<String> = preferencesRepository.securityPin
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = ""
        )

    val isBiometricEnabled: StateFlow<Boolean> = preferencesRepository.isBiometricEnabled
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = true
        )

    // Navigation state
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _selectedClientId = MutableStateFlow<String?>(null)
    val selectedClientId: StateFlow<String?> = _selectedClientId.asStateFlow()

    // Shop settings (Persistent via DataStore)
    val shopName: StateFlow<String> = preferencesRepository.shopName
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferencesRepository.DEFAULT_SHOP_NAME
        )

    val currency: StateFlow<String> = preferencesRepository.currency
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserPreferencesRepository.DEFAULT_CURRENCY
        )

    val shopPhone: StateFlow<String> = preferencesRepository.shopPhone
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ""
        )

    // Filtering & Search
    val searchQuery = MutableStateFlow("")
    val activeFilter = MutableStateFlow(ClientFilter.ALL)

    // Clients with balances
    val allClientsWithBalances: StateFlow<List<ClientWithBalance>> = repository.clientsWithBalances
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Filtered clients list
    val filteredClients: StateFlow<List<ClientWithBalance>> = combine(
        allClientsWithBalances,
        searchQuery,
        activeFilter
    ) { clients, query, filter ->
        val trimmed = query.trim().lowercase()
        clients.filter { item ->
            val client = item.client
            val matchesQuery = trimmed.isEmpty() ||
                    client.nom.lowercase().contains(trimmed) ||
                    (!client.prenom.isNullOrEmpty() && client.prenom.lowercase().contains(trimmed)) ||
                    client.nomComplet.lowercase().contains(trimmed) ||
                    client.residence.lowercase().contains(trimmed) ||
                    (!client.cin.isNullOrEmpty() && client.cin.lowercase().contains(trimmed)) ||
                    client.telephone.contains(trimmed)

            val matchesFilter = when (filter) {
                ClientFilter.ALL -> true
                ClientFilter.DEBTORS_ONLY -> item.soldeDu > 0L
                ClientFilter.OVERDUE_ONLY -> item.isOverdue
                ClientFilter.OVER_LIMIT_ONLY -> item.isOverLimit
            }

            matchesQuery && matchesFilter
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Global Statistics
    val globalCreances: StateFlow<Long> = allClientsWithBalances.combine(MutableStateFlow(Unit)) { clients, _ ->
        clients.sumOf { it.soldeDu }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0L
    )

    // Selected client data
    val selectedClientWithBalance: StateFlow<ClientWithBalance?> = combine(
        allClientsWithBalances,
        _selectedClientId
    ) { clients, selectedId ->
        if (selectedId == null) null else clients.find { it.client.id == selectedId }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    // Transactions for selected client
    private val _clientTransactions = MutableStateFlow<List<TransactionWithItems>>(emptyList())
    val clientTransactions: StateFlow<List<TransactionWithItems>> = _clientTransactions.asStateFlow()

    // Cryptographic audit state
    private val _auditReport = MutableStateFlow<CryptoSecurity.AuditReport?>(null)
    val auditReport: StateFlow<CryptoSecurity.AuditReport?> = _auditReport.asStateFlow()

    private val _isAuditing = MutableStateFlow(false)
    val isAuditing: StateFlow<Boolean> = _isAuditing.asStateFlow()

    // Post-transaction receipt popup
    private val _lastTransactionResult = MutableStateFlow<PostTransactionResult?>(null)
    val lastTransactionResult: StateFlow<PostTransactionResult?> = _lastTransactionResult.asStateFlow()

    init {
        refreshAudit()
    }

    fun unlockApp() {
        _isAppUnlocked.value = true
    }

    fun lockApp() {
        _isAppUnlocked.value = false
    }

    fun setSecurityPin(newPin: String) {
        viewModelScope.launch {
            preferencesRepository.setSecurityPin(newPin)
        }
    }

    fun toggleSecurityPinEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setSecurityPinEnabled(enabled)
        }
    }

    fun toggleBiometricEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBiometricEnabled(enabled)
        }
    }

    fun navigateTo(screen: Screen, clientId: String? = null) {
        if (clientId != null) {
            _selectedClientId.value = clientId
            loadTransactionsForClient(clientId)
        }
        _currentScreen.value = screen
    }

    fun selectClient(clientId: String) {
        _selectedClientId.value = clientId
        loadTransactionsForClient(clientId)
    }

    private fun loadTransactionsForClient(clientId: String) {
        viewModelScope.launch {
            repository.getTransactionsForClient(clientId).collect {
                _clientTransactions.value = it
            }
        }
    }

    fun updateSettings(name: String, curr: String, phone: String) {
        viewModelScope.launch {
            preferencesRepository.updateShopSettings(name, curr, phone)
        }
    }

    fun createClient(
        nom: String,
        prenom: String? = null,
        telephone: String,
        residence: String,
        cin: String? = null,
        plafondCredit: Long = 0L,
        note: String? = null,
        photoUri: String? = null,
        photoCinUri: String? = null,
        empreinteUri: String? = null,
        onSuccess: (Client) -> Unit = {}
    ) {
        viewModelScope.launch {
            val newClient = Client(
                nom = nom.trim(),
                prenom = prenom?.trim()?.ifBlank { null },
                telephone = telephone.trim(),
                residence = residence.trim(),
                cin = cin?.trim()?.ifBlank { null },
                plafondCredit = plafondCredit,
                note = note?.trim()?.ifBlank { null },
                photoUri = photoUri,
                photoCinUri = photoCinUri,
                empreinteUri = empreinteUri
            )
            repository.insertClient(newClient)
            onSuccess(newClient)
        }
    }

    fun recordCredit(
        clientId: String,
        dateCredit: Long,
        dateRemboursementPrevue: Long?,
        grandTotal: Long,
        acompteVerse: Long,
        raison: String?,
        signatureUri: String?,
        items: List<Pair<String, Pair<Long, Double>>>,
        isEmissaire: Boolean = false,
        emissaireNom: String? = null,
        emissaireLien: String? = null,
        emissaireTelephone: String? = null,
        emissaireConfirmation: String? = null,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val tx = repository.recordCreditTransaction(
                clientId = clientId,
                dateCredit = dateCredit,
                dateRemboursementPrevue = dateRemboursementPrevue,
                grandTotal = grandTotal,
                acompteVerse = acompteVerse,
                raison = raison,
                signatureUri = signatureUri,
                items = items,
                isEmissaire = isEmissaire,
                emissaireNom = emissaireNom,
                emissaireLien = emissaireLien,
                emissaireTelephone = emissaireTelephone,
                emissaireConfirmation = emissaireConfirmation
            )

            val client = repository.getClientById(clientId)
            if (client != null) {
                val itemEntities = items.map { (name, priceQty) ->
                    val (pu, qty) = priceQty
                    TransactionItemEntity(
                        id = "",
                        transactionId = tx.id,
                        designation = name,
                        prixUnitaire = pu,
                        quantite = qty,
                        totalLigne = (pu * qty).toLong()
                    )
                }
                val receipt = repository.buildWhatsAppReceipt(
                    shopName = shopName.value,
                    client = client,
                    transaction = tx,
                    items = itemEntities,
                    currency = currency.value
                )
                _lastTransactionResult.value = PostTransactionResult(client, tx, receipt)
            }

            refreshAudit()
            onSuccess()
        }
    }

    fun recordPayment(
        clientId: String,
        montant: Long,
        note: String?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            val tx = repository.recordPaymentTransaction(
                clientId = clientId,
                montantPaye = montant,
                note = note
            )

            val client = repository.getClientById(clientId)
            if (client != null) {
                val receipt = repository.buildWhatsAppReceipt(
                    shopName = shopName.value,
                    client = client,
                    transaction = tx,
                    items = emptyList(),
                    currency = currency.value
                )
                _lastTransactionResult.value = PostTransactionResult(client, tx, receipt)
            }

            refreshAudit()
            onSuccess()
        }
    }

    fun clearLastTransactionResult() {
        _lastTransactionResult.value = null
    }

    fun refreshAudit() {
        viewModelScope.launch {
            _isAuditing.value = true
            _auditReport.value = repository.runIntegrityAudit()
            _isAuditing.value = false
        }
    }

    fun getClientStatementText(client: Client, soldeDu: Long, transactions: List<TransactionWithItems>): String {
        return repository.buildWhatsAppStatement(
            shopName = shopName.value,
            client = client,
            soldeDu = soldeDu,
            transactions = transactions,
            currency = currency.value
        )
    }

    fun getTransactionReceiptText(client: Client, txWithItems: TransactionWithItems): String {
        return repository.buildWhatsAppReceipt(
            shopName = shopName.value,
            client = client,
            transaction = txWithItems.transaction,
            items = txWithItems.items,
            currency = currency.value
        )
    }

    /**
     * Génère un JSON exportable complet du registre pour sauvegarde
     */
    suspend fun exportLedgerJson(): String {
        val audit = repository.runIntegrityAudit()
        val clients = allClientsWithBalances.value
        val root = JSONObject()
        root.put("shop_name", shopName.value)
        root.put("currency", currency.value)
        root.put("exported_at", System.currentTimeMillis())
        root.put("chain_valid", audit.isChainValid)
        root.put("total_blocks", audit.totalBlocks)

        val clientsArray = JSONArray()
        clients.forEach { cwb ->
            val c = cwb.client
            val obj = JSONObject()
            obj.put("id", c.id)
            obj.put("nom", c.nom)
            obj.put("prenom", c.prenom)
            obj.put("nom_complet", c.nomComplet)
            obj.put("telephone", c.telephone)
            obj.put("residence", c.residence)
            obj.put("cin", c.cin)
            obj.put("plafond_credit", c.plafondCredit)
            obj.put("solde_du", cwb.soldeDu)
            clientsArray.put(obj)
        }
        root.put("clients", clientsArray)

        val blocksArray = JSONArray()
        audit.blocks.forEach { b ->
            val obj = JSONObject()
            obj.put("index", b.index)
            obj.put("transaction_id", b.transaction.id)
            obj.put("client_id", b.transaction.clientId)
            obj.put("type", b.transaction.type)
            obj.put("grand_total", b.transaction.grandTotal)
            obj.put("reste_a_payer", b.transaction.resteAPayer)
            obj.put("is_emissaire", b.transaction.isEmissaire)
            obj.put("emissaire_nom", b.transaction.emissaireNom)
            obj.put("emissaire_lien", b.transaction.emissaireLien)
            obj.put("previous_hash", b.transaction.previousHash)
            obj.put("current_hash", b.transaction.currentHash)
            obj.put("is_valid", b.isValid)
            blocksArray.put(obj)
        }
        root.put("blockchain_ledger", blocksArray)

        return root.toString(2)
    }
}
