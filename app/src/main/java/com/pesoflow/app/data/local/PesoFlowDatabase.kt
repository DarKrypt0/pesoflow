package com.pesoflow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        AccountEntity::class,
        EnvelopeEntity::class,
        TransactionEntity::class,
        RecurringBillEntity::class,
        SavingsGoalEntity::class,
        CategorizationRuleEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class PesoFlowDatabase : RoomDatabase() {

    abstract fun budgetDao(): BudgetDao

    companion object {
        @Volatile
        private var INSTANCE: PesoFlowDatabase? = null

        fun getInstance(context: Context): PesoFlowDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    PesoFlowDatabase::class.java,
                    "pesoflow_budget.db"
                )
                    .addCallback(SeedDatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }

    private class SeedDatabaseCallback : Callback() {
        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            CoroutineScope(Dispatchers.IO).launch {
                val dao = INSTANCE?.budgetDao() ?: return@launch

                // 1. Seed Default Balance Sheet Accounts (GCash Primary + Cash + Bank + GCredit)
                val gcashId = dao.insertAccount(
                    AccountEntity(
                        name = "GCash Wallet",
                        type = AccountType.GCASH,
                        balance = 18450.00,
                        isAsset = true,
                        colorHex = "#007DFE"
                    )
                )
                dao.insertAccount(
                    AccountEntity(
                        name = "BPI Payroll / Savings",
                        type = AccountType.BANK,
                        balance = 32000.00,
                        isAsset = true,
                        colorHex = "#B91C1C"
                    )
                )
                dao.insertAccount(
                    AccountEntity(
                        name = "Cash on Hand",
                        type = AccountType.CASH,
                        balance = 2500.00,
                        isAsset = true,
                        colorHex = "#10B981"
                    )
                )
                dao.insertAccount(
                    AccountEntity(
                        name = "GCredit / Credit Card",
                        type = AccountType.CREDIT_CARD,
                        balance = 3200.00,
                        isAsset = false,
                        includeInZeroBasedBudget = false,
                        colorHex = "#F59E0B"
                    )
                )

                // 2. Seed Zero-Based Budget Envelopes
                val foodEnvId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Food & Dining",
                        groupName = "Daily Living",
                        iconName = "restaurant",
                        assignedAmount = 12000.0,
                        spentAmount = 3450.0
                    )
                )
                val transportEnvId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Grab & Commute",
                        groupName = "Daily Living",
                        iconName = "commute",
                        assignedAmount = 4500.0,
                        spentAmount = 1120.0
                    )
                )
                val billsEnvId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Utilities & Subscriptions",
                        groupName = "Fixed Bills",
                        iconName = "receipt_long",
                        assignedAmount = 7500.0,
                        spentAmount = 2499.0
                    )
                )
                val rentEnvId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Rent & Condo Dues",
                        groupName = "Fixed Bills",
                        iconName = "home",
                        assignedAmount = 15000.0,
                        spentAmount = 15000.0
                    )
                )
                val feesEnvId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "GCash & InstaPay Fees",
                        groupName = "Fees & Buffer",
                        iconName = "bolt",
                        assignedAmount = 300.0,
                        spentAmount = 45.0
                    )
                )

                // 3. Seed Smart Auto-Categorization Rules (PH Merchants & GCash Keywords)
                listOf(
                    CategorizationRuleEntity(pattern = "Jollibee", targetEnvelopeId = foodEnvId, priority = 20),
                    CategorizationRuleEntity(pattern = "McDonald", targetEnvelopeId = foodEnvId, priority = 20),
                    CategorizationRuleEntity(pattern = "Foodpanda", targetEnvelopeId = foodEnvId, priority = 20),
                    CategorizationRuleEntity(pattern = "Starbucks", targetEnvelopeId = foodEnvId, priority = 20),
                    CategorizationRuleEntity(pattern = "7-Eleven", targetEnvelopeId = foodEnvId, priority = 15),
                    CategorizationRuleEntity(pattern = "Grab", targetEnvelopeId = transportEnvId, priority = 20),
                    CategorizationRuleEntity(pattern = "Angkas", targetEnvelopeId = transportEnvId, priority = 20),
                    CategorizationRuleEntity(pattern = "Move It", targetEnvelopeId = transportEnvId, priority = 20),
                    CategorizationRuleEntity(pattern = "Meralco", targetEnvelopeId = billsEnvId, defaultFee = 10.0, priority = 25),
                    CategorizationRuleEntity(pattern = "Maynilad", targetEnvelopeId = billsEnvId, defaultFee = 7.0, priority = 25),
                    CategorizationRuleEntity(pattern = "PLDT", targetEnvelopeId = billsEnvId, defaultFee = 10.0, priority = 25),
                    CategorizationRuleEntity(pattern = "Globe", targetEnvelopeId = billsEnvId, priority = 20),
                    CategorizationRuleEntity(pattern = "Netflix", targetEnvelopeId = billsEnvId, priority = 20),
                    CategorizationRuleEntity(pattern = "Spotify", targetEnvelopeId = billsEnvId, priority = 20),
                    CategorizationRuleEntity(pattern = "InstaPay", targetEnvelopeId = feesEnvId, defaultFee = 15.0, priority = 30)
                ).forEach { dao.insertCategorizationRule(it) }

                // 4. Seed Recurring Bills & Subscriptions for Calendar View
                dao.insertRecurringBill(
                    RecurringBillEntity(
                        name = "Meralco Electric",
                        amount = 3400.0,
                        convenienceFee = 10.0,
                        dueDayOfMonth = 15,
                        envelopeId = billsEnvId,
                        payViaGCash = true
                    )
                )
                dao.insertRecurringBill(
                    RecurringBillEntity(
                        name = "PLDT Fiber Internet",
                        amount = 1699.0,
                        convenienceFee = 10.0,
                        dueDayOfMonth = 20,
                        envelopeId = billsEnvId,
                        payViaGCash = true,
                        isPaidThisMonth = true
                    )
                )
                dao.insertRecurringBill(
                    RecurringBillEntity(
                        name = "Spotify Premium Duo",
                        amount = 199.0,
                        convenienceFee = 0.0,
                        dueDayOfMonth = 5,
                        envelopeId = billsEnvId,
                        payViaGCash = true,
                        isSubscription = true,
                        isPaidThisMonth = true
                    )
                )
                dao.insertRecurringBill(
                    RecurringBillEntity(
                        name = "Condo Rent (InstaPay via GCash)",
                        amount = 15000.0,
                        convenienceFee = 15.0,
                        dueDayOfMonth = 1,
                        envelopeId = rentEnvId,
                        payViaGCash = true,
                        isPaidThisMonth = true
                    )
                )

                // 5. Seed Sinking Funds / Savings Goals
                dao.insertSavingsGoal(
                    SavingsGoalEntity(
                        name = "Emergency Fund (6 Months)",
                        targetAmount = 150000.0,
                        savedAmount = 68500.0,
                        targetDateIso = "2027-06-30",
                        colorHex = "#10B981",
                        isSinkingFund = false
                    )
                )
                dao.insertSavingsGoal(
                    SavingsGoalEntity(
                        name = "13th Month / Holiday Sinking Fund",
                        targetAmount = 25000.0,
                        savedAmount = 20000.0,
                        targetDateIso = "2026-12-15",
                        colorHex = "#007DFE",
                        isSinkingFund = true
                    )
                )
                dao.insertSavingsGoal(
                    SavingsGoalEntity(
                        name = "Japan Trip Sinking Fund",
                        targetAmount = 80000.0,
                        savedAmount = 34000.0,
                        targetDateIso = "2027-04-10",
                        colorHex = "#8B5CF6",
                        isSinkingFund = true
                    )
                )

                // 6. Seed Sample GCash Transactions
                dao.insertTransaction(
                    TransactionEntity(
                        merchantOrPayee = "Landlord (BDO InstaPay)",
                        note = "Monthly Condo Rent",
                        amount = 15000.0,
                        convenienceFee = 15.0,
                        type = TransactionType.EXPENSE,
                        source = TransactionSource.GCASH_NOTIFICATION,
                        accountId = gcashId,
                        envelopeId = rentEnvId,
                        gcashReferenceNo = "9018273645123"
                    )
                )
                dao.insertTransaction(
                    TransactionEntity(
                        merchantOrPayee = "Jollibee BGC Stopover",
                        note = "Chickenjoy Lunch via Scan-to-Pay",
                        amount = 245.0,
                        convenienceFee = 0.0,
                        type = TransactionType.EXPENSE,
                        source = TransactionSource.GCASH_NOTIFICATION,
                        accountId = gcashId,
                        envelopeId = foodEnvId,
                        gcashReferenceNo = "9018273649981"
                    )
                )
            }
        }
    }
}
