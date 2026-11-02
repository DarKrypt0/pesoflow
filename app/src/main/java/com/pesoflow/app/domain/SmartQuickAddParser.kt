package com.pesoflow.app.domain

import com.google.ai.client.generativeai.GenerativeModel
import com.pesoflow.app.BuildConfig
import com.pesoflow.app.data.local.entities.TransactionType

data class ParsedQuickAdd(
    val merchantOrPayee: String,
    val amount: Double,
    val convenienceFee: Double = 0.0,
    val type: TransactionType = TransactionType.EXPENSE,
    val viaGCash: Boolean = true,
    val note: String = ""
)

/**
 * Parses natural language phrases such as:
 * - "Spent 185 on iced coffee at Starbucks via GCash"
 * - "Sent 2500 to landlord via GCash + 15 fee"
 * - "Received 25000 salary"
 *
 * Uses Gemini AI when GEMINI_API_KEY is configured, with a fast, offline
 * deterministic regex parser as fallback.
 */
object SmartQuickAddParser {

    private val amountRegex = Regex("""(?:₱|PHP|php|P)?\s*(\d+(?:,\d{3})*(?:\.\d{1,2})?)""")
    private val feeRegex = Regex("""(?:\+|plus|with)\s*(?:₱|PHP|P)?\s*(\d+(?:\.\d{1,2})?)\s*(?:fee|instapay|transfer fee)""", RegexOption.IGNORE_CASE)
    private val merchantRegex = Regex("""(?:on|at|to|from|for)\s+([A-Za-z0-9\s&'-]+?)(?:\s+via|\s+\+|\s+with|$)""", RegexOption.IGNORE_CASE)

    suspend fun parse(input: String): ParsedQuickAdd {
        val trimmed = input.trim()
        if (BuildConfig.GEMINI_API_KEY.isNotBlank()) {
            runCatching {
                return parseWithGemini(trimmed)
            }
        }
        return parseOffline(trimmed)
    }

    fun parseOffline(input: String): ParsedQuickAdd {
        val isIncome = input.contains(Regex("""\b(received|salary|income|cash in|refund)\b""", RegexOption.IGNORE_CASE))
        val feeMatch = feeRegex.find(input)
        val explicitFee = feeMatch?.groupValues?.get(1)?.toDoubleOrNull()
            ?: if (input.contains("instapay", ignoreCase = true)) 15.0 else 0.0

        val textWithoutFee = if (feeMatch != null) input.removeRange(feeMatch.range) else input
        val amountMatch = amountRegex.find(textWithoutFee)
        val amount = amountMatch?.groupValues?.get(1)?.replace(",", "")?.toDoubleOrNull() ?: 0.0

        val merchant = merchantRegex.find(textWithoutFee)?.groupValues?.get(1)?.trim()
            ?.takeIf { it.isNotBlank() }
            ?: textWithoutFee.replace(amountRegex, "")
                .replace(Regex("""\b(spent|paid|sent|bought|via|gcash|cash|received)\b""", RegexOption.IGNORE_CASE), "")
                .trim()
                .ifBlank { "Quick Expense" }

        val viaGCash = !input.contains("cash only", ignoreCase = true)

        return ParsedQuickAdd(
            merchantOrPayee = merchant.replaceFirstChar { it.uppercase() },
            amount = amount,
            convenienceFee = explicitFee,
            type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
            viaGCash = viaGCash,
            note = "Parsed from: \"$input\""
        )
    }

    private suspend fun parseWithGemini(input: String): ParsedQuickAdd {
        val model = GenerativeModel(
            modelName = "gemini-1.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY
        )
        val prompt = """
            Extract budgeting transaction fields from this user input: "$input"
            Return ONLY pipe-separated values in this exact format:
            MERCHANT|AMOUNT|FEE|TYPE(EXPENSE or INCOME)|VIA_GCASH(true or false)
            Example: Jollibee|245.0|0.0|EXPENSE|true
        """.trimIndent()

        val response = model.generateContent(prompt).text?.trim() ?: return parseOffline(input)
        val parts = response.split("|")
        if (parts.size < 5) return parseOffline(input)

        return ParsedQuickAdd(
            merchantOrPayee = parts[0].trim().ifBlank { "Quick Expense" },
            amount = parts[1].trim().toDoubleOrNull() ?: 0.0,
            convenienceFee = parts[2].trim().toDoubleOrNull() ?: 0.0,
            type = if (parts[3].trim().equals("INCOME", ignoreCase = true)) TransactionType.INCOME else TransactionType.EXPENSE,
            viaGCash = parts[4].trim().toBooleanStrictOrNull() ?: true,
            note = "AI Quick-Add: \"$input\""
        )
    }
}
