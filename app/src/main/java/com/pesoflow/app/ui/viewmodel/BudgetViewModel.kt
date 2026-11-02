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

    val totalAssigned: Double
        get() = envelopes.sumOf { it.assignedAmount }

    val totalSpent: Double
        get() = envelopes.sumOf { it.spentAmount }

    val readyToAssign: Double
        get() = totalAssets - totalAssigned
}

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

    private val prefs = application.getSharedPreferences("pesoflow_prefs", Context.MODE_PRIVATE)
    private val onboardingCompleteFlow = MutableStateFlow(prefs.getBoolean("onboarding_complete", false))

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
                onboardingCompleteFlow
            ) { rules, fees, onboarded ->
                Triple(rules, fees, onboarded)
            }
        ) { baseState, (rules, fees, onboarded) ->
            baseState.copy(
                requiresOnboarding = !onboarded && baseState.accounts.isEmpty(),
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
        gcashInitialBalance: Double,
        cashInitialBalance: Double,
        bankInitialBalance: Double
    ) {
        viewModelScope.launch {
            repository.initializeOnboardingAccounts(
                gcashInitialBalance = gcashInitialBalance,
                cashInitialBalance = cashInitialBalance,
                bankInitialBalance = bankInitialBalance
            )
            prefs.edit().putBoolean("onboarding_complete", true).apply()
            onboardingCompleteFlow.value = true
        }
    }

    fun updateAccountBalance(account: AccountEntity, newBalance: Double) {
        viewModelScope.launch {
            repository.updateAccountBalance(account, newBalance)
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
                    accountId = if (parsed.viaGCash) 1L else 2L,
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
