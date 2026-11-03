package com.pesoflow.app.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pesoflow.app.data.local.PesoFlowDatabase
import com.pesoflow.app.data.local.entities.AccountEntity
import com.pesoflow.app.data.local.entities.CategorizationRuleEntity
import com.pesoflow.app.data.local.entities.EnvelopeEntity
import com.pesoflow.app.data.local.entities.RecurringBillEntity
import com.pesoflow.app.data.local.entities.SavingsGoalEntity
import com.pesoflow.app.data.local.entities.TransactionEntity
import com.pesoflow.app.data.local.entities.TransactionSource
import com.pesoflow.app.data.local.entities.TransactionType
import com.pesoflow.app.data.repository.BudgetRepository
import com.pesoflow.app.domain.SmartQuickAddParser
import com.pesoflow.app.sync.GCashParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BudgetUiState(
    val isInitialized: Boolean = false,
    val requiresOnboarding: Boolean = false,
    val userName: String = "Ken",
    val isDarkTheme: Boolean = true,
    val accounts: List<AccountEntity> = emptyList(),
    val envelopes: List<EnvelopeEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val recurringBills: List<RecurringBillEntity> = emptyList(),
    val savingsGoals: List<SavingsGoalEntity> = emptyList(),
    val rules: List<CategorizationRuleEntity> = emptyList(),
    val totalConvenienceFees: Double = 0.0
) {
    val totalAssets: Double
        get() = accounts.filter { it.isAsset }.sumOf { it.balance }

    val totalLiabilities: Double
        get() = accounts.filter { !it.isAsset }.sumOf { it.balance }

    val netWorth: Double
        get() = totalAssets - totalLiabilities

    val gcashAccount: AccountEntity?
        get() = accounts.firstOrNull { it.id == 1L } ?: accounts.firstOrNull()

    val gcashBalance: Double
        get() = gcashAccount?.balance ?: 0.0

    val totalPlannedBudget: Double
        get() = envelopes.sumOf { it.assignedAmount }

    val totalSpentInEnvelopes: Double
        get() = envelopes.sumOf { it.spentAmount }

    val leftToSpend: Double
        get() = (totalPlannedBudget - totalSpentInEnvelopes).coerceAtLeast(0.0)

    val usedPercentage: Int
        get() = if (totalPlannedBudget <= 0.0) 0
        else ((totalSpentInEnvelopes / totalPlannedBudget) * 100.0).toInt().coerceIn(0, 100)

    val monthlyIncome: Double
        get() {
            val loggedIncome = transactions.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
            return if (loggedIncome > 0.0) loggedIncome else totalAssets
        }

    val monthlyExpenses: Double
        get() = transactions.filter { it.type == TransactionType.EXPENSE }.sumOf { it.totalCashOutflow }

    val readyToAssign: Double
        get() = totalAssets - totalPlannedBudget

    val userInitials: String
        get() {
            val parts = userName.trim().split(" ").filter { it.isNotBlank() }
            return when {
                parts.size >= 2 -> "${parts[0].first()}${parts[1].first()}".uppercase()
                parts.size == 1 && parts[0].length >= 2 -> parts[0].take(2).uppercase()
                parts.size == 1 -> parts[0].uppercase()
                else -> "PF"
            }
        }
}

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("pesoflow_prefs_v3", Context.MODE_PRIVATE)
    private val onboardingCompleteFlow = MutableStateFlow(prefs.getBoolean("onboarding_complete", false))
    private val userNameFlow = MutableStateFlow(prefs.getString("user_name", "Ken") ?: "Ken")
    private val darkThemeFlow = MutableStateFlow(prefs.getBoolean("dark_theme", true))

    private val repository: BudgetRepository
    val uiState: StateFlow<BudgetUiState>

    init {
        val dao = PesoFlowDatabase.getInstance(application).budgetDao()
        repository = BudgetRepository(dao)

        uiState = combine(
            repository.accounts,
            repository.envelopes,
            repository.transactions,
            repository.recurringBills,
            repository.savingsGoals
        ) { accounts, envelopes, transactions, bills, goals ->
            BudgetUiState(
                isInitialized = true,
                accounts = accounts,
                envelopes = envelopes,
                transactions = transactions,
                recurringBills = bills,
                savingsGoals = goals
            )
        }.combine(
            combine(
                repository.categorizationRules,
                repository.totalConvenienceFees,
                onboardingCompleteFlow,
                userNameFlow,
                darkThemeFlow
            ) { rules, fees, onboarded, name, isDark ->
                 listOf(rules, fees, onboarded, name, isDark)
            }
        ) { baseState, extras ->
            @Suppress("UNCHECKED_CAST")
            val rules = extras[0] as List<CategorizationRuleEntity>
            val fees = extras[1] as Double
            val onboarded = extras[2] as Boolean
            val name = extras[3] as String
            val isDark = extras[4] as Boolean

            baseState.copy(
                requiresOnboarding = !onboarded && baseState.accounts.isEmpty(),
                userName = name,
                isDarkTheme = isDark,
                rules = rules,
                totalConvenienceFees = fees
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetUiState()
        )
    }

    fun completeOnboarding(
        userName: String,
        gcashInitialBalance: Double,
        cashInitialBalance: Double,
        bankInitialBalance: Double
    ) {
        viewModelScope.launch {
            val cleanName = userName.trim().ifBlank { "Ken" }
            repository.initializeOnboardingAccounts(
                gcashInitialBalance = gcashInitialBalance,
                cashInitialBalance = cashInitialBalance,
                bankInitialBalance = bankInitialBalance
            )
            prefs.edit()
                .putBoolean("onboarding_complete", true)
                .putString("user_name", cleanName)
                .apply()
            userNameFlow.value = cleanName
            onboardingCompleteFlow.value = true
        }
    }

    fun toggleTheme() {
        val next = !darkThemeFlow.value
        prefs.edit().putBoolean("dark_theme", next).apply()
        darkThemeFlow.value = next
    }

    fun updateAccountBalance(account: AccountEntity, newBalance: Double) {
        viewModelScope.launch {
            repository.updateAccountBalance(account, newBalance)
        }
    }

    fun setEnvelopePlannedAmount(envelope: EnvelopeEntity, newPlannedAmount: Double) {
        viewModelScope.launch {
            repository.setEnvelopePlannedAmount(envelope, newPlannedAmount)
        }
    }

    fun addManualTransaction(
        merchant: String,
        amount: Double,
        convenienceFee: Double,
        type: TransactionType,
        accountId: Long,
        envelopeId: Long?,
        note: String = ""
    ) {
        viewModelScope.launch {
            repository.addTransaction(
                merchantOrPayee = merchant,
                amount = amount,
                convenienceFee = convenienceFee,
                type = type,
                source = TransactionSource.MANUAL,
                accountId = accountId,
                envelopeId = envelopeId,
                note = note
            )
        }
    }

    fun addNaturalLanguageTransaction(input: String) {
        viewModelScope.launch {
            val parsed = SmartQuickAddParser.parse(input)
            if (parsed.amount > 0.0) {
                repository.addTransaction(
                    merchantOrPayee = parsed.merchantOrPayee,
                    amount = parsed.amount,
                    convenienceFee = parsed.convenienceFee,
                    type = parsed.type,
                    source = TransactionSource.AI_QUICK_ADD,
                    accountId = 1L,
                    note = parsed.note
                )
            }
        }
    }

    fun simulateGCashNotification(sampleText: String) {
        viewModelScope.launch {
            val parsed = GCashParser.parseNotificationOrSms(sampleText) ?: return@launch
            repository.recordParsedGCashTransaction(parsed, TransactionSource.GCASH_NOTIFICATION)
        }
    }

    fun assignFundsToEnvelope(envelope: EnvelopeEntity, delta: Double) {
        viewModelScope.launch {
            repository.assignToEnvelope(envelope, delta)
        }
    }

    fun addEnvelope(name: String, groupName: String, assignedAmount: Double) {
        viewModelScope.launch {
            repository.createEnvelope(name, groupName, assignedAmount)
        }
    }

    fun addRecurringBill(name: String, amount: Double, fee: Double, dueDay: Int) {
        viewModelScope.launch {
            repository.createRecurringBill(name, amount, fee, dueDay)
        }
    }

    fun toggleBillPaid(bill: RecurringBillEntity) {
        viewModelScope.launch {
            repository.toggleBillPaid(bill)
        }
    }

    fun addSavingsGoal(name: String, targetAmount: Double, targetDateIso: String) {
        viewModelScope.launch {
            repository.createSavingsGoal(name, targetAmount, targetDateIso)
        }
    }

    fun contributeToGoal(goal: SavingsGoalEntity, amount: Double) {
        viewModelScope.launch {
            repository.contributeToSavingsGoal(goal, amount)
        }
    }

    fun addSmartRule(pattern: String, envelopeId: Long, defaultFee: Double) {
        viewModelScope.launch {
            repository.addCategorizationRule(pattern, envelopeId, defaultFee)
        }
    }
}
