package com.pesoflow.app

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import com.pesoflow.app.data.local.PesoFlowDatabase
import com.pesoflow.app.data.local.entities.TransactionSource
import com.pesoflow.app.data.repository.BudgetRepository
import com.pesoflow.app.sync.GCashStatementImporter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Receives GCash receipt screenshots (`image/*`) or exported GCash statement files
 * shared via Android's native Share Sheet, runs ML Kit OCR / statement parsing,
 * and saves them directly into the Room database.
 */
class ShareReceiptActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (intent?.action == Intent.ACTION_SEND) {
            val sharedUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(Intent.EXTRA_STREAM)
            }

            val mimeType = intent.type.orEmpty()
            if (sharedUri != null) {
                processSharedFile(sharedUri, mimeType)
                return
            }
        }
        finish()
    }

    private fun processSharedFile(uri: Uri, mimeType: String) {
        lifecycleScope.launch(Dispatchers.IO) {
            val dao = PesoFlowDatabase.getInstance(applicationContext).budgetDao()
            val repo = BudgetRepository(dao)

            var importedCount = 0
            if (mimeType.startsWith("image/")) {
                val parsed = GCashStatementImporter.parseReceiptScreenshot(applicationContext, uri)
                if (parsed != null && repo.recordParsedGCashTransaction(parsed, TransactionSource.GCASH_RECEIPT_OCR)) {
                    importedCount = 1
                }
            } else {
                val list = GCashStatementImporter.parseStatementLines(applicationContext, uri)
                for (item in list) {
                    if (repo.recordParsedGCashTransaction(item, TransactionSource.GCASH_PDF_STATEMENT)) {
                        importedCount++
                    }
                }
            }

            withContext(Dispatchers.Main) {
                val msg = if (importedCount > 0) {
                    "PesoFlow imported $importedCount GCash transaction(s)!"
                } else {
                    "Could not detect a new GCash receipt (or Reference No. already logged)."
                }
                Toast.makeText(applicationContext, msg, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }
}
