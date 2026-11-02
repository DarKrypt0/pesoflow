package com.pesoflow.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.pesoflow.app.data.local.dao.BudgetDao
import com.pesoflow.app.data.local.entities.AccountEntity
import com.pesoflow.app.data.local.entities.CategorizationRuleEntity
import com.pesoflow.app.data.local.entities.EnvelopeEntity
import com.pesoflow.app.data.local.entities.RecurringBillEntity
import com.pesoflow.app.data.local.entities.SavingsGoalEntity
import com.pesoflow.app.data.local.entities.TransactionEntity
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
    version = 2,
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
                    .fallbackToDestructiveMigration()
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

                // Seed clean Zero-Based Budget Envelopes (0.0 assigned/spent so user starts fresh)
                val foodEnvId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Food & Dining",
                        groupName = "Essentials",
                        iconName = "restaurant",
                        assignedAmount = 0.0,
                        spentAmount = 0.0
                    )
                )
                val transportEnvId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Grab & Transit",
                        groupName = "Essentials",
                        iconName = "commute",
                        assignedAmount = 0.0,
                        spentAmount = 0.0
                    )
                )
                val billsEnvId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Utilities & Subscriptions",
                        groupName = "Fixed Bills",
                        iconName = "receipt_long",
                        assignedAmount = 0.0,
                        spentAmount = 0.0
                    )
                )
                val housingEnvId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Rent & Housing",
                        groupName = "Fixed Bills",
                        iconName = "home",
                        assignedAmount = 0.0,
                        spentAmount = 0.0
                    )
                )
                val feesEnvId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Transfer & InstaPay Fees",
                        groupName = "Buffer",
                        iconName = "bolt",
                        assignedAmount = 0.0,
                        spentAmount = 0.0
                    )
                )

                // Seed Smart Auto-Categorization Rules (PH Merchants & GCash Keywords)
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
                    CategorizationRuleEntity(pattern = "Rent", targetEnvelopeId = housingEnvId, priority = 15),
                    CategorizationRuleEntity(pattern = "InstaPay", targetEnvelopeId = feesEnvId, defaultFee = 15.0, priority = 30)
                ).forEach { dao.insertCategorizationRule(it) }
            }
        }
    }
}
