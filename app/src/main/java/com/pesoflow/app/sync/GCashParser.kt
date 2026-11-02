package com.pesoflow.app.sync

import com.pesoflow.app.data.local.entities.TransactionType

data class ParsedGCashTransaction(
    val merchantOrPayee: String,
    val amount: Double,
    val convenienceFee: Double,
    val referenceNo: String?,
    val type: TransactionType,
    val rawSummary: String
)

/**
 * Parses GCash push notifications, SMS messages, and OCR receipt text.
 *
 * Supported GCash formats:
 * 1. Express Send: "You have sent PHP 500.00 of GCash to JUAN D. 09171234567 ... Ref. No. 1234567890123"
 * 2. QR / Merchant Payment: "You have paid PHP 245.00 of GCash to JOLLIBEE ... Ref. No. 9876543210123"
 * 3. Bank Transfer (InstaPay): "You have transferred PHP 15,000.00 to BDO ... Convenience Fee: PHP 15.00 ... Ref No. 5556667778889"
 * 4. Bills Payment: "Your payment of PHP 3,400.00 to MERALCO has been processed ... Fee: PHP 10.00 ... Ref. No. 111222333444"
 * 5. Incoming / Received: "You have received PHP 1,200.00 of GCash from MARIA S. ... Ref. No. 4445556667778"
 */
object GCashParser {

    private val phpAmountRegex = Regex(
        """(?:PHP|₱|Php|Amount(?:\s*Due|\s*Paid)?[:\s]*)\s*(\d{1,3}(?:,\d{3})*(?:\.\d{2})|\d+(?:\.\d{2}))""",
        RegexOption.IGNORE_CASE
    )

    private val feeRegex = Regex(
        """(?:Fee|Service Fee|Convenience Fee|Transfer Fee)[:\s]*(?:PHP|₱|Php)?\s*(\d+(?:\.\d{2}))""",
        RegexOption.IGNORE_CASE
    )

    private val refNoRegex = Regex(
        """(?:Ref\.?\s*No\.?|Reference\s*No\.?)[:\s#]*(\d{8,16})""",
        RegexOption.IGNORE_CASE
    )

    private val recipientRegex = Regex(
        """(?:to|from|Merchant[:\s]+)\s*([A-Za-z0-9\s.*&'-]{2,35}?)(?:\s+on|\s+with|\s+Ref|\s+via|\.|$)""",
        RegexOption.IGNORE_CASE
    )

    fun parseNotificationOrSms(text: String): ParsedGCashTransaction? {
        val cleaned = text.replace("\n", " ").trim()
        if (!looksLikeGCashTransaction(cleaned)) return null

        val amountStr = phpAmountRegex.find(cleaned)?.groupValues?.get(1)?.replace(",", "")
        val amount = amountStr?.toDoubleOrNull() ?: return null

        val explicitFee = feeRegex.find(cleaned)?.groupValues?.get(1)?.toDoubleOrNull()
        val isBankTransfer = cleaned.contains("transferred", ignoreCase = true) ||
            cleaned.contains("InstaPay", ignoreCase = true) ||
            cleaned.contains("Bank Transfer", ignoreCase = true)

        val fee = explicitFee ?: if (isBankTransfer) 15.0 else 0.0
        val refNo = refNoRegex.find(cleaned)?.groupValues?.get(1)

        val isIncoming = cleaned.contains("received", ignoreCase = true) ||
            cleaned.contains("Cash-In", ignoreCase = true)

        val rawRecipient = recipientRegex.find(cleaned)?.groupValues?.get(1)?.trim()
            ?: if (isBankTransfer) "GCash Bank Transfer (InstaPay)"
            else if (isIncoming) "GCash Incoming Transfer"
            else "GCash Payment"

        return ParsedGCashTransaction(
            merchantOrPayee = rawRecipient,
            amount = amount,
            convenienceFee = fee,
            referenceNo = refNo,
            type = if (isIncoming) TransactionType.INCOME else TransactionType.EXPENSE,
            rawSummary = cleaned.take(160)
        )
    }

    private fun looksLikeGCashTransaction(text: String): Boolean {
        val hasKeyword = text.contains("GCash", ignoreCase = true) ||
            text.contains("Ref. No.", ignoreCase = true) ||
            text.contains("Reference No", ignoreCase = true) ||
            text.contains("InstaPay", ignoreCase = true) ||
            text.contains("Express Send", ignoreCase = true)
        val hasAmount = phpAmountRegex.containsMatchIn(text)
        return hasKeyword && hasAmount
    }
}
