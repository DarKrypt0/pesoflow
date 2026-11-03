package com.pesoflow.app.data.repository

import com.pesoflow.app.data.local.dao.BudgetDao
import com.pesoflow.app.data.local.entities.AccountEntity
import com.pesoflow.app.data.local.entities.AccountType
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
import kotlinx.coroutines.flow.firstOrNull
import kotlin.math.roundToLong

class BudgetRepository(private val dao: BudgetDao) {

    val accounts: Flow<List<AccountEntity>> = dao.observeAccounts()
    val envelopes: Flow<List<EnvelopeEntity>> = dao.observeEnvelopes()
    val transactions: Flow<List<TransactionEntity>> = dao.observeTransactions()
    val recurringBills: Flow<List<RecurringBillEntity>> = dao.observeRecurringBills()
    val savingsGoals: Flow<List<SavingsGoalEntity>> = dao.observeSavingsGoals()
    val categorizationRules: Flow<List<CategorizationRuleEntity>> = dao.observeCategorizationRules()
    val totalConvenienceFees: Flow<Double> = dao.observeTotalConvenienceFees()

    /**
     * Initializes user accounts on first-launch onboarding with the user's real
     * starting GCash balance and optional Cash / Bank balances, and allocates an
     * initial zero-based budget across the 6 Figma categories so the user can
     * immediately see their "Left to spend" & "Room to breathe" (editable anytime).
     */
    suspend fun initializeOnboardingAccounts(
        gcashInitialBalance: Double,
        cashInitialBalance: Double,
        bankInitialBalance: Double
    ) {
        dao.insertAccount(
            AccountEntity(
                id = 1L,
                name = "GCash Wallet",
                type = AccountType.GCASH,
                balance = gcashInitialBalance,
                isAsset = true,
                colorHex = "#C5D5BC"
            )
        )
        if (cashInitialBalance > 0.0) {
            dao.insertAccount(
                AccountEntity(
                    id = 2L,
                    name = "Cash on Hand",
                    type = AccountType.CASH,
                    balance = cashInitialBalance,
                    isAsset = true,
                    colorHex = "#D2DFC9"
                )
            )
        }
        if (bankInitialBalance > 0.0) {
            dao.insertAccount(
                AccountEntity(
                    id = 3L,
                    name = "Bank Account",
                    type = AccountType.BANK,
                    balance = bankInitialBalance,
                    isAsset = true,
                    colorHex = "#8E9B91"
                )
            )
        }

        val totalLiquid = gcashInitialBalance + cashInitialBalance + bankInitialBalance
        val existingEnvelopes = dao.observeEnvelopes().firstOrNull().orEmpty()
        if (totalLiquid > 0.0 && existingEnvelopes.all { it.assignedAmount == 0.0 }) {
            // Allocate sensible zero-based default percentages across the 6 categories
            val weights = mapOf(
                "Home & bills" to 0.35,
                "Groceries" to 0.20,
                "Food & drink" to 0.15,
                "Transport" to 0.10,
                "Shopping" to 0.10,
                "Wellbeing" to 0.10
            )
            for (env in existingEnvelopes) {
                val w = weights[env.name] ?: (1.0 / existingEnvelopes.size.coerceAtLeast(1))
                val allocated = (totalLiquid * w).roundToLong().toDouble()
                dao.updateEnvelope(env.copy(assignedAmount = allocated))
            }
        }
    }

    suspend fun updateAccountBalance(account: AccountEntity, newBalance: Double) {
        dao.updateAccount(account.copy(balance = newBalance))
    }

    suspend fun setEnvelopePlannedAmount(envelope: EnvelopeEntity, newPlannedAmount: Double) {
        dao.updateEnvelope(envelope.copy(assignedAmount = newPlannedAmount.coerceAtLeast(0.0)))
    }

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
            return false
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

        val account = dao.getAccountById(accountId)
        if (account != null) {
            val delta = if (type == TransactionType.INCOME) amount else -(amount + resolvedFee)
            val newBalance = if (account.isAsset) account.balance + delta else account.balance - delta
            dao.updateAccount(account.copy(balance = newBalance))
        }

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
            accountId = 1L,
            gcashReferenceNo = parsed.referenceNo,
            note = parsed.rawSummary
        )
    }

    suspend fun assignToEnvelope(envelope: EnvelopeEntity, additionalAssigned: Double) {
        dao.updateEnvelope(
            envelope.copy(assignedAmount = (envelope.assignedAmount + additionalAssigned).coerceAtLeast(0.0))
        )
    }

    suspend fun createEnvelope(name: String, groupName: String, assignedAmount: Double) {
        dao.insertEnvelope(
            EnvelopeEntity(
                name = name,
                groupName = groupName,
                iconName = "shopping",
                assignedAmount = assignedAmount,
                spentAmount = 0.0
            )
        )
    }

    suspend fun createRecurringBill(name: String, amount: Double, fee: Double, dueDay: Int) {
        dao.insertRecurringBill(
            RecurringBillEntity(
                name = name,
                amount = amount,
                convenienceFee = fee,
                dueDayOfMonth = dueDay.coerceIn(1, 31),
                payViaGCash = true
            )
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
                accountId = 1L,
                envelopeId = bill.envelopeId,
                note = "Recurring Bill Paid"
            )
        }
    }

    suspend fun createSavingsGoal(name: String, targetAmount: Double, targetDateIso: String) {
        dao.insertSavingsGoal(
            SavingsGoalEntity(
                name = name,
                targetAmount = targetAmount,
                savedAmount = 0.0,
                targetDateIso = targetDateIso,
                colorHex = "#C5D5BC",
                isSinkingFund = true
            )
        )
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
