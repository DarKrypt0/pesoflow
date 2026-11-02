package com.pesoflow.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_bills")
data class RecurringBillEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val convenienceFee: Double = 0.0, // e.g. ₱7-₱15 GCash biller fee
    val dueDayOfMonth: Int,           // 1..31 for Calendar View
    val envelopeId: Long? = null,
    val payViaGCash: Boolean = true,
    val isSubscription: Boolean = false,
    val isPaidThisMonth: Boolean = false
)
