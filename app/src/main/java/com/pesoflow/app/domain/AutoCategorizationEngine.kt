package com.pesoflow.app.domain

import com.pesoflow.app.data.local.entities.CategorizationRuleEntity

data class CategorizationMatch(
    val envelopeId: Long?,
    val suggestedFee: Double
)

object AutoCategorizationEngine {

    /**
     * Evaluates a merchant name or notification body against user-defined
     * and seeded rules ordered by priority.
     */
    fun match(
        merchantOrText: String,
        rules: List<CategorizationRuleEntity>
    ): CategorizationMatch {
        val normalized = merchantOrText.trim()
        for (rule in rules.sortedByDescending { it.priority }) {
            val regex = runCatching { Regex(rule.pattern, RegexOption.IGNORE_CASE) }.getOrNull()
            val matched = regex?.containsMatchIn(normalized)
                ?: normalized.contains(rule.pattern, ignoreCase = true)

            if (matched) {
                return CategorizationMatch(
                    envelopeId = rule.targetEnvelopeId,
                    suggestedFee = rule.defaultFee
                )
            }
        }
        return CategorizationMatch(envelopeId = null, suggestedFee = 0.0)
    }
}
