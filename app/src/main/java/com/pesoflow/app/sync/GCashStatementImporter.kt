package com.pesoflow.app.sync

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.tasks.await
import java.io.BufferedReader
import java.io.InputStreamReader

/**
 * Handles:
 * 1. On-device OCR scanning of shared GCash receipt screenshots via Google ML Kit
 * 2. Text / CSV / PDF statement line parsing from exported GCash Transaction History
 */
object GCashStatementImporter {

    suspend fun parseReceiptScreenshot(
        context: Context,
        imageUri: Uri
    ): ParsedGCashTransaction? {
        return runCatching {
            val image = InputImage.fromFilePath(context, imageUri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val visionText = recognizer.process(image).await()
            GCashParser.parseNotificationOrSms(visionText.text)
        }.getOrNull()
    }

    fun parseStatementLines(
        context: Context,
        fileUri: Uri
    ): List<ParsedGCashTransaction> {
        val results = mutableListOf<ParsedGCashTransaction>()
        runCatching {
            context.contentResolver.openInputStream(fileUri)?.use { stream ->
                BufferedReader(InputStreamReader(stream)).forEachLine { line ->
                    val parsed = GCashParser.parseNotificationOrSms(line)
                    if (parsed != null) {
                        results.add(parsed)
                    }
                }
            }
        }
        return results
    }
}
