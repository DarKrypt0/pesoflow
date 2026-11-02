package com.pesoflow.app.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Smart auto-categorization rule:
 * Matches merchant/notification text (case-insensitive substring or regex)
 * and assigns a target Envelope + optional default convenience fee.
 */
@Entity(tableName = "categorization_rules")
data class CategorizationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val pattern: String,           // e.g. "Jollibee", "Grab", "Meralco", "InstaPay"
    val targetEnvelopeId: Long,
    val defaultFee: Double = 0.0,  // e.g. 15.0 for InstaPay bank transfers
    val priority: Int = 10
)
