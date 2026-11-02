package com.pesoflow.app

import com.pesoflow.app.data.local.entities.TransactionType
import com.pesoflow.app.domain.SmartQuickAddParser
import com.pesoflow.app.sync.GCashParser
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class GCashParserTest {

    @Test
    fun `parses GCash QR merchant payment and reference number`() {
        val sms = "You have paid PHP 245.00 of GCash to JOLLIBEE BGC on 11/03/2026. Ref. No. 9018273649981"
        val parsed = GCashParser.parseNotificationOrSms(sms)

        assertNotNull(parsed)
        assertEquals(245.00, parsed!!.amount, 0.001)
        assertEquals(0.00, parsed.convenienceFee, 0.001)
        assertEquals("9018273649981", parsed.referenceNo)
        assertEquals(TransactionType.EXPENSE, parsed.type)
    }

    @Test
    fun `auto detects 15 peso fee on GCash InstaPay bank transfer`() {
        val notif = "You have transferred PHP 15,000.00 to BDO via InstaPay. Ref. No. 5556667778889"
        val parsed = GCashParser.parseNotificationOrSms(notif)

        assertNotNull(parsed)
        assertEquals(15000.00, parsed!!.amount, 0.001)
        assertEquals(15.00, parsed.convenienceFee, 0.001)
        assertEquals("5556667778889", parsed.referenceNo)
    }

    @Test
    fun `parses offline natural language quick add with fee`() {
        val quickAdd = SmartQuickAddParser.parseOffline("Spent 185 on Starbucks via GCash + 15 fee")
        assertEquals(185.0, quickAdd.amount, 0.001)
        assertEquals(15.0, quickAdd.convenienceFee, 0.001)
        assertEquals("Starbucks", quickAdd.merchantOrPayee)
    }
}
