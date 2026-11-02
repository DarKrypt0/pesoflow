package com.pesoflow.app.data.repository

import com.pesoflow.app.data.local.dao.BudgetDao
import com.pesoflow.app.data.local.entities.AccountEntity
import com.pesoflow.app.data.local.entities.CategorizationRuleEntity
import com.pesoflow.app.data.local.entities.EnvelopeEntity
import com.pesoflow.app.data.local.entities.RecurringBillEntity
import com.pesoflow.app.data.local.entities.SavingsGoalEntity
import com.pesoflow.app.data.local.entities.TransactionEntity
import com.pesoflow.app.data.local.entities.TransactionSource
import com.pesoflow.app.data.local.entities.TransactionType
import com.pesoflow.app.domain.AutoCategorizationEngine
import com.pesoflow.app.sync.ParsedGCashTransaction
import kotlinx.coroutines.flow.Flow

class BudgetRepository(private val dao: BudgetDao) {

    val accounts: Flow<List<AccountEntity>> = dao.observeAccounts()
    val envelopes: Flow<List<EnvelopeEntity>> = dao.observeEnvelopes()
    val transactions: Flow<List<TransactionEntity>> = dao.observeTransactions()
    val recurringBills: Flow<List<RecurringBillEntity>> = dao.observeRecurringBills()
    val savingsGoals: Flow<List<SavingsGoalEntity>> = dao.observeSavingsGoals()
    val categorizationRules: Flow<List<CategorizationRuleEntity>> = dao.observeCategorizationRules()
    val totalConvenienceFees: Flow<Double> = dao.observeTotalConvenienceFees()

    /**
     * Records a manual or AI quick-add transaction, applies smart auto-categorization
     * if no envelope was explicitly chosen, updates the account balance sheet, and
     * updates the Zero-Based envelope spent balance.
     */
    suspend fun addTransaction(
        merchantOrPayee: String,
        amount: Double,
        convenienceFee: Double = 0.0,
        type: TransactionType = TransactionType.EXPENSE,
        source: TransactionSource = TransactionSource.MANUAL,
        accountId: Long = 1L,
        envelopeId: Long? = null,
        gcashReferenceNo: String? = null,
        note: String = ""
    ): Boolean {
        if (gcashReferenceNo != null && dao.existsByGCashRefNo(gcashReferenceNo)) {
            return false // Duplicate GCash Reference No. ignored
        }

        val rules = dao.getCategorizationRulesList()
        val autoMatch = AutoCategorizationEngine.match(merchantOrPayee, rules)
        val resolvedEnvelopeId = envelopeId ?: autoMatch.envelopeId
        val resolvedFee = if (convenienceFee > 0.0) convenienceFee else autoMatch.suggestedFee

        val txn = TransactionEntity(
            merchantOrPayee = merchantOrPayee,
            note = note,
            amount = amount,
            convenienceFee = resolvedFee,
            type = type,
            source = source,
            accountId = accountId,
            envelopeId = resolvedEnvelopeId,
            gcashReferenceNo = gcashReferenceNo
        )
        dao.insertTransaction(txn)

        // Update Account Balance Sheet
        val account = dao.getAccountById(accountId)
        if (account != null) {
            val delta = if (type == TransactionType.INCOME) amount else -(amount + resolvedFee)
            val newBalance = if (account.isAsset) account.balance + delta else account.balance - delta
            dao.updateAccount(account.copy(balance = newBalance))
        }

        // Update Zero-Based Budget Envelope spent amount
        if (type == TransactionType.EXPENSE && resolvedEnvelopeId != null) {
            val env = dao.getEnvelopeById(resolvedEnvelopeId)
            if (env != null) {
                dao.updateEnvelope(env.copy(spentAmount = env.spentAmount + amount + resolvedFee))
            }
        }

        return true
    }

    suspend fun recordParsedGCashTransaction(
        parsed: ParsedGCashTransaction,
        source: TransactionSource
    ): Boolean {
        return addTransaction(
            merchantOrPayee = parsed.merchantOrPayee,
            amount = parsed.amount,
            convenienceFee = parsed.convenienceFee,
            type = parsed.type,
            source = source,
            accountId = 1L, // Primary GCash Wallet
            gcashReferenceNo = parsed.referenceNo,
            note = parsed.rawSummary
        )
    }

    suspend fun assignToEnvelope(envelope: EnvelopeEntity, additionalAssigned: Double) {
        dao.updateEnvelope(
            envelope.copy(assignedAmount = (envelope.assignedAmount + additionalAssigned).coerceAtLeast(0.0))
        )
    }

    suspend fun toggleBillPaid(bill: RecurringBillEntity) {
        val updated = bill.copy(isPaidThisMonth = !bill.isPaidThisMonth)
        dao.updateRecurringBill(updated)
        if (updated.isPaidThisMonth) {
            addTransaction(
                merchantOrPayee = bill.name,
                amount = bill.amount,
                convenienceFee = bill.convenienceFee,
                type = TransactionType.EXPENSE,
                source = TransactionSource.MANUAL,
                accountId = if (bill.payViaGCash) 1L else 2L,
                envelopeId = bill.envelopeId,
                note = "Recurring Bill Paid"
            )
        }
    }

    suspend fun contributeToSavingsGoal(goal: SavingsGoalEntity, amount: Double) {
        dao.updateSavingsGoal(goal.copy(savedAmount = goal.savedAmount + amount))
    }

    suspend fun addCategorizationRule(pattern: String, targetEnvelopeId: Long, defaultFee: Double) {
        dao.insertCategorizationRule(
            CategorizationRuleEntity(
                pattern = pattern,
                targetEnvelopeId = targetEnvelopeId,
                defaultFee = defaultFee
            )
        )
    }
}
