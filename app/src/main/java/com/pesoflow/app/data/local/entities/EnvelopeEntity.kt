package com.pesoflow.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Zero-Based Budgeting Envelope ("Give every peso a job").
 * Available = assignedAmount - spentAmount + rolloverAmount
 */
@Entity(tableName = "envelopes")
data class EnvelopeEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val groupName: String, // e.g., "Needs", "Wants", "Bills & Utilities", "Fees & Buffer"
    val iconName: String,
    val assignedAmount: Double = 0.0,
    val spentAmount: Double = 0.0,
    val rolloverAmount: Double = 0.0,
    val yearMonth: String = "2026-11" // YYYY-MM cycle
) {
    val availableAmount: Double
        get() = assignedAmount + rolloverAmount - spentAmount
}
