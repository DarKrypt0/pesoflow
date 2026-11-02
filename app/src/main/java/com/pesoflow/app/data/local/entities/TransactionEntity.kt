package com.pesoflow.app.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType {
    EXPENSE,
    INCOME,
    TRANSFER
}

enum class TransactionSource {
    MANUAL,
    GCASH_NOTIFICATION,
    GCASH_SMS,
    GCASH_RECEIPT_OCR,
    GCASH_PDF_STATEMENT,
    AI_QUICK_ADD,
    WIDGET_QUICK_ADD
}

/**
 * Stores every cashflow event with built-in support for:
 * - GCash Reference Number deduplication (`gcashReferenceNo`)
 * - GCash Transfer / Convenience Fee tracking (`convenienceFee`, e.g., ₱15 InstaPay fee)
 * - Zero-Based Envelope link (`envelopeId`) + Balance Sheet Account link (`accountId`)
 */
@Entity(
    tableName = "transactions",
    indices = [Index(value = ["gcashReferenceNo"], unique = false)]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val merchantOrPayee: String,
    val note: String = "",
    val amount: Double,
    val convenienceFee: Double = 0.0, // e.g. ₱15.00 InstaPay / Cash-in / Bills fee
    val type: TransactionType = TransactionType.EXPENSE,
    val source: TransactionSource = TransactionSource.MANUAL,
    val accountId: Long = 1L,         // Default 1L = GCash Wallet
    val envelopeId: Long? = null,     // Linked Zero-Based Budget category envelope
    val gcashReferenceNo: String? = null,
    val timestampMillis: Long = System.currentTimeMillis()
) {
    val totalCashOutflow: Double
        get() = if (type == TransactionType.INCOME) amount else amount + convenienceFee
}
