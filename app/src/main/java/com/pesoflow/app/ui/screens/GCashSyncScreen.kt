package com.pesoflow.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pesoflow.app.data.local.entities.CategorizationRuleEntity
import com.pesoflow.app.data.local.entities.EnvelopeEntity
import com.pesoflow.app.ui.theme.AmberWarning
import com.pesoflow.app.ui.theme.GCashBlue

@Composable
fun GCashSyncScreen(
    totalConvenienceFees: Double,
    rules: List<CategorizationRuleEntity>,
    envelopes: List<EnvelopeEntity>,
    onSimulateGCashNotification: (String) -> Unit,
    onAddRule: (String, Long, Double) -> Unit
) {
    val context = LocalContext.current
    var newKeyword by remember { mutableStateOf("") }
    var newFeeText by remember { mutableStateOf("0") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. GCash Convenience / InstaPay Fee Tracker Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = AmberWarning.copy(alpha = 0.16f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "GCASH TRANSFER & CONVENIENCE FEE TRACKER",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = formatPeso(totalConvenienceFees),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Total spent on ₱15 InstaPay transfers, Cash-In fees, and Biller convenience fees.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // 2. Live Notification & SMS Sync Setup
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Real-Time GCash Notification & SMS Auto-Sync",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = GCashBlue
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Automatically captures Express Send, Scan-to-Pay QR, InstaPay Bank Transfers (+₱15 fee), and Bills Payment alerts with Reference Number deduplication.",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            }
                        ) {
                            Text("Grant Notification Access")
                        }
                        OutlinedButton(
                            onClick = {
                                val randomRef = (100000000000L..999999999999L).random()
                                onSimulateGCashNotification(
                                    "You have paid PHP 195.00 of GCash to Starbucks BGC on 11/03/2026. Convenience Fee: PHP 0.00. Ref. No. $randomRef"
                                )
                            }
                        ) {
                            Text("Test GCash Alert")
                        }
                    }
                }
            }
        }

        // 3. Screenshot OCR & PDF Statement Import Info
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "2. GCash Receipt Screenshot OCR & PDF Importer",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "In the GCash app, tap 'Share' on any transaction receipt and choose 'Import GCash Receipt to PesoFlow'. On-device ML Kit OCR extracts the amount, merchant, fee, and Reference No. automatically.",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        // 4. Smart Auto-Categorization Rules
        item {
            Text(
                text = "3. Smart Auto-Categorization Rules (${rules.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newKeyword,
                    onValueChange = { newKeyword = it },
                    label = { Text("Merchant Keyword (e.g. Grab)") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newFeeText,
                    onValueChange = { newFeeText = it },
                    label = { Text("Default Fee ₱") },
                    modifier = Modifier.weight(0.5f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        val envId = envelopes.firstOrNull()?.id ?: 1L
                        if (newKeyword.isNotBlank()) {
                            onAddRule(newKeyword.trim(), envId, newFeeText.toDoubleOrNull() ?: 0.0)
                            newKeyword = ""
                        }
                    }
                ) {
                    Text("Add")
                }
            }
        }

        items(rules) { rule ->
            val envName = envelopes.firstOrNull { it.id == rule.targetEnvelopeId }?.name ?: "Envelope #${rule.targetEnvelopeId}"
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("If merchant matches \"${rule.pattern}\"", fontWeight = FontWeight.Medium)
                    Text(
                        text = "→ $envName" + if (rule.defaultFee > 0) " (+₱${rule.defaultFee.toInt()})" else "",
                        color = GCashBlue,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
