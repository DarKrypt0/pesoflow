package com.pesoflow.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pesoflow.app.data.local.entities.AccountEntity
import com.pesoflow.app.data.local.entities.CategorizationRuleEntity
import com.pesoflow.app.data.local.entities.EnvelopeEntity
import com.pesoflow.app.data.local.entities.RecurringBillEntity
import com.pesoflow.app.data.local.entities.SavingsGoalEntity
import com.pesoflow.app.data.local.entities.TransactionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BudgetDao {

    // --- Balance Sheet Accounts ---
    @Query("SELECT * FROM accounts ORDER BY id ASC")
    fun observeAccounts(): Flow<List<AccountEntity>>

    @Query("SELECT * FROM accounts WHERE id = :accountId LIMIT 1")
    suspend fun getAccountById(accountId: Long): AccountEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: AccountEntity): Long

    @Update
    suspend fun updateAccount(account: AccountEntity)

    // --- Zero-Based Budget Envelopes ---
    @Query("SELECT * FROM envelopes ORDER BY groupName ASC, id ASC")
    fun observeEnvelopes(): Flow<List<EnvelopeEntity>>

    @Query("SELECT * FROM envelopes WHERE id = :envelopeId LIMIT 1")
    suspend fun getEnvelopeById(envelopeId: Long): EnvelopeEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEnvelope(envelope: EnvelopeEntity): Long

    @Update
    suspend fun updateEnvelope(envelope: EnvelopeEntity)

    // --- Transactions & GCash Deduplication ---
    @Query("SELECT * FROM transactions ORDER BY timestampMillis DESC")
    fun observeTransactions(): Flow<List<TransactionEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM transactions WHERE gcashReferenceNo = :refNo AND gcashReferenceNo IS NOT NULL)")
    suspend fun existsByGCashRefNo(refNo: String): Boolean

    @Query("SELECT COALESCE(SUM(convenienceFee), 0.0) FROM transactions")
    fun observeTotalConvenienceFees(): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    // --- Recurring Bills & Subscriptions (Calendar View) ---
    @Query("SELECT * FROM recurring_bills ORDER BY dueDayOfMonth ASC")
    fun observeRecurringBills(): Flow<List<RecurringBillEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecurringBill(bill: RecurringBillEntity): Long

    @Update
    suspend fun updateRecurringBill(bill: RecurringBillEntity)

    // --- Sinking Funds & Savings Goals ---
    @Query("SELECT * FROM savings_goals ORDER BY id ASC")
    fun observeSavingsGoals(): Flow<List<SavingsGoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavingsGoal(goal: SavingsGoalEntity): Long

    @Update
    suspend fun updateSavingsGoal(goal: SavingsGoalEntity)

    // --- Smart Auto-Categorization Rules ---
    @Query("SELECT * FROM categorization_rules ORDER BY priority DESC")
    fun observeCategorizationRules(): Flow<List<CategorizationRuleEntity>>

    @Query("SELECT * FROM categorization_rules ORDER BY priority DESC")
    suspend fun getCategorizationRulesList(): List<CategorizationRuleEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategorizationRule(rule: CategorizationRuleEntity): Long
}
