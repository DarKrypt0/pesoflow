package com.pesoflow.app.sync

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import androidx.glance.appwidget.updateAll
import com.pesoflow.app.widget.PesoFlowGlanceWidget
import com.pesoflow.app.data.local.PesoFlowDatabase
import com.pesoflow.app.data.local.entities.TransactionSource
import com.pesoflow.app.data.repository.BudgetRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Listens for real-time push notifications from the official GCash Android app
 * (`com.globe.gcash.android`) as well as SMS apps delivering GCash alerts.
 * Automatically parses amount, merchant, ₱15 InstaPay / biller fees, and
 * Reference Number, then records the transaction into Room SQLite with deduplication.
 */
class GCashNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private val SUPPORTED_PACKAGES = setOf(
            "com.globe.gcash.android",          // Official GCash App
            "com.google.android.apps.messaging",// Google Messages (GCash SMS)
            "com.samsung.android.messaging",    // Samsung Messages (GCash SMS)
            "com.android.mms"                   // AOSP SMS
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notification = sbn ?: return
        val packageName = notification.packageName ?: return

        if (packageName !in SUPPORTED_PACKAGES) return

        val extras = notification.notification.extras ?: return
        val title = extras.getString(Notification.EXTRA_TITLE).orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()

        val combinedPayload = buildString {
            append(title)
            append(" ")
            append(if (bigText.isNotBlank()) bigText else text)
        }.trim()

        val parsed = GCashParser.parseNotificationOrSms(combinedPayload) ?: return

        val source = if (packageName == "com.globe.gcash.android") {
            TransactionSource.GCASH_NOTIFICATION
        } else {
            TransactionSource.GCASH_SMS
        }

        serviceScope.launch {
            val db = PesoFlowDatabase.getInstance(applicationContext)
            val repository = BudgetRepository(db.budgetDao())
            repository.recordParsedGCashTransaction(parsed, source)
            runCatching {
                PesoFlowGlanceWidget().updateAll(applicationContext)
            }
        }
    }
}
