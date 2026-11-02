package com.pesoflow.app.ui.viewmodel

import android.app.Application
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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class BudgetUiState(
    val accounts: List<AccountEntity> = emptyList(),
    val envelopes: List<EnvelopeEntity> = emptyList(),
    val transactions: List<TransactionEntity> = emptyList(),
    val recurringBills: List<RecurringBillEntity> = emptyList(),
    val savingsGoals: List<SavingsGoalEntity> = emptyList(),
    val rules: List<CategorizationRuleEntity> = emptyList(),
    val totalConvenienceFees: Double = 0.0
) {
    // Balance Sheet metrics
    val totalAssets: Double
        get() = accounts.filter { it.isAsset }.sumOf { it.balance }

    val totalLiabilities: Double
        get() = accounts.filter { !it.isAsset }.sumOf { it.balance }

    val netWorth: Double
        get() = totalAssets - totalLiabilities

    val gcashBalance: Double
        get() = accounts.firstOrNull { it.id == 1L }?.balance ?: 0.0

    // Zero-Based Budgeting ("Ready to Assign") metric:
    // Total liquid asset cash minus total assigned across envelopes
    val totalAssigned: Double
        get() = envelopes.sumOf { it.assignedAmount }

    val totalAvailableInEnvelopes: Double
        get() = envelopes.sumOf { it.availableAmount }

    val readyToAssign: Double
        get() = totalAssets - totalAssigned
}

class BudgetViewModel(application: Application) : AndroidViewModel(application) {

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
                accounts = accounts,
                envelopes = envelopes,
                transactions = transactions,
                recurringBills = bills,
                savingsGoals = goals
            )
        }.combine(
            combine(repository.categorizationRules, repository.totalConvenienceFees) { rules, fees ->
                rules to fees
            }
        ) { baseState, (rules, fees) ->
            baseState.copy(
                rules = rules,
                totalConvenienceFees = fees
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = BudgetUiState()
        )
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
                    accountId = if (parsed.viaGCash) 1L else 3L,
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

    fun toggleBillPaid(bill: RecurringBillEntity) {
        viewModelScope.launch {
            repository.toggleBillPaid(bill)
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
