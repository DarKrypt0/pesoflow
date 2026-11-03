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
    version = 3,
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

                // Seed the exact 6 Figma categories
                val homeBillsId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Home & bills",
                        groupName = "Fixed",
                        iconName = "home",
                        assignedAmount = 0.0,
                        spentAmount = 0.0
                    )
                )
                val groceriesId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Groceries",
                        groupName = "Essentials",
                        iconName = "groceries",
                        assignedAmount = 0.0,
                        spentAmount = 0.0
                    )
                )
                val foodDrinkId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Food & drink",
                        groupName = "Essentials",
                        iconName = "coffee",
                        assignedAmount = 0.0,
                        spentAmount = 0.0
                    )
                )
                val transportId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Transport",
                        groupName = "Essentials",
                        iconName = "transport",
                        assignedAmount = 0.0,
                        spentAmount = 0.0
                    )
                )
                val shoppingId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Shopping",
                        groupName = "Lifestyle",
                        iconName = "shopping",
                        assignedAmount = 0.0,
                        spentAmount = 0.0
                    )
                )
                val wellbeingId = dao.insertEnvelope(
                    EnvelopeEntity(
                        name = "Wellbeing",
                        groupName = "Lifestyle",
                        iconName = "wellbeing",
                        assignedAmount = 0.0,
                        spentAmount = 0.0
                    )
                )

                // Seed Smart Auto-Categorization Rules (PH Merchants & GCash Keywords)
                listOf(
                    CategorizationRuleEntity(pattern = "Meralco", targetEnvelopeId = homeBillsId, defaultFee = 10.0, priority = 25),
                    CategorizationRuleEntity(pattern = "Maynilad", targetEnvelopeId = homeBillsId, defaultFee = 7.0, priority = 25),
                    CategorizationRuleEntity(pattern = "PLDT", targetEnvelopeId = homeBillsId, defaultFee = 10.0, priority = 25),
                    CategorizationRuleEntity(pattern = "Globe", targetEnvelopeId = homeBillsId, priority = 20),
                    CategorizationRuleEntity(pattern = "Rent", targetEnvelopeId = homeBillsId, priority = 20),
                    CategorizationRuleEntity(pattern = "InstaPay", targetEnvelopeId = homeBillsId, defaultFee = 15.0, priority = 30),
                    CategorizationRuleEntity(pattern = "SM Markets", targetEnvelopeId = groceriesId, priority = 20),
                    CategorizationRuleEntity(pattern = "Puregold", targetEnvelopeId = groceriesId, priority = 20),
                    CategorizationRuleEntity(pattern = "Landers", targetEnvelopeId = groceriesId, priority = 20),
                    CategorizationRuleEntity(pattern = "7-Eleven", targetEnvelopeId = groceriesId, priority = 15),
                    CategorizationRuleEntity(pattern = "Jollibee", targetEnvelopeId = foodDrinkId, priority = 20),
                    CategorizationRuleEntity(pattern = "McDonald", targetEnvelopeId = foodDrinkId, priority = 20),
                    CategorizationRuleEntity(pattern = "Starbucks", targetEnvelopeId = foodDrinkId, priority = 20),
                    CategorizationRuleEntity(pattern = "Foodpanda", targetEnvelopeId = foodDrinkId, priority = 20),
                    CategorizationRuleEntity(pattern = "Grab", targetEnvelopeId = transportId, priority = 20),
                    CategorizationRuleEntity(pattern = "Angkas", targetEnvelopeId = transportId, priority = 20),
                    CategorizationRuleEntity(pattern = "Move It", targetEnvelopeId = transportId, priority = 20),
                    CategorizationRuleEntity(pattern = "Shopee", targetEnvelopeId = shoppingId, priority = 20),
                    CategorizationRuleEntity(pattern = "Lazada", targetEnvelopeId = shoppingId, priority = 20),
                    CategorizationRuleEntity(pattern = "Uniqlo", targetEnvelopeId = shoppingId, priority = 20),
                    CategorizationRuleEntity(pattern = "Mercury Drug", targetEnvelopeId = wellbeingId, priority = 20),
                    CategorizationRuleEntity(pattern = "Watsons", targetEnvelopeId = wellbeingId, priority = 20),
                    CategorizationRuleEntity(pattern = "Spotify", targetEnvelopeId = wellbeingId, priority = 20),
                    CategorizationRuleEntity(pattern = "Netflix", targetEnvelopeId = wellbeingId, priority = 20)
                ).forEach { dao.insertCategorizationRule(it) }
            }
        }
    }
}
