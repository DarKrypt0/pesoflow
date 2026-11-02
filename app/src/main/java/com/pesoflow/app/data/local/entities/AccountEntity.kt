package com.pesoflow.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class AccountType {
    GCASH,       // Primary e-wallet (Asset)
    BANK,        // Checking / Savings account (Asset)
    CASH,        // Physical cash on hand (Asset)
    CREDIT_CARD, // Credit Card / GCredit (Liability)
    LOAN         // GLoan / GGives / Personal Loan (Liability)
}

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: AccountType,
    val balance: Double,
    val isAsset: Boolean = type == AccountType.GCASH || type == AccountType.BANK || type == AccountType.CASH,
    val includeInZeroBasedBudget: Boolean = isAsset,
    val colorHex: String = "#007DFE"
)
