package com.pesoflow.app.ui.screens

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.unit.sp
import com.pesoflow.app.data.local.entities.CategorizationRuleEntity
import com.pesoflow.app.data.local.entities.EnvelopeEntity
import com.pesoflow.app.ui.components.MinimalistCard
import com.pesoflow.app.ui.theme.ObsidianBg
import com.pesoflow.app.ui.theme.SageBright
import com.pesoflow.app.ui.theme.SageContainer
import com.pesoflow.app.ui.theme.SagePrimary
import com.pesoflow.app.ui.theme.TextPrimary
import com.pesoflow.app.ui.theme.TextSecondary
import com.pesoflow.app.ui.theme.WarmSand

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
            .background(ObsidianBg),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            MinimalistCard(
                containerColor = SageContainer,
                borderColor = WarmSand.copy(alpha = 0.4f)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = "TRANSFER & CONVENIENCE FEES",
                        style = MaterialTheme.typography.labelSmall,
                        color = WarmSand,
                        letterSpacing = 1.4.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatPeso(totalConvenienceFees),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Cumulative ₱15 InstaPay bank transfers, Cash-In fees, and biller convenience charges.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            MinimalistCard {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "LIVE GCASH AUTO-SYNC",
                        style = MaterialTheme.typography.labelSmall,
                        color = SagePrimary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Notification & SMS Listener",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Detects GCash push notifications and SMS receipts in real time with Reference No. deduplication.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = SagePrimary,
                                contentColor = ObsidianBg
                            )
                        ) {
                            Text("Notification Access", fontWeight = FontWeight.SemiBold)
                        }
                        OutlinedButton(
                            onClick = {
                                val randomRef = (100000000000L..999999999999L).random()
                                onSimulateGCashNotification(
                                    "You have paid PHP 195.00 of GCash to Starbucks BGC on 11/03/2026. Convenience Fee: PHP 0.00. Ref. No. $randomRef"
                                )
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("Test Alert", color = SageBright)
                        }
                    }
                }
            }
        }

        item {
            MinimalistCard {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "RECEIPT OCR & PDF IMPORT",
                        style = MaterialTheme.typography.labelSmall,
                        color = SagePrimary,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Share from GCash App",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Tap 'Share' on any GCash receipt screen and select PesoFlow. On-device ML Kit OCR extracts amount, merchant, and Reference No.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        item {
            Text(
                text = "SMART CATEGORIZATION RULES (${rules.size})",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newKeyword,
                    onValueChange = { newKeyword = it },
                    label = { Text("Merchant Keyword") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = newFeeText,
                    onValueChange = { newFeeText = it },
                    label = { Text("Fee ₱") },
                    modifier = Modifier.weight(0.45f),
                    singleLine = true
                )
                Button(
                    onClick = {
                        val envId = envelopes.firstOrNull()?.id ?: 1L
                        if (newKeyword.isNotBlank()) {
                            onAddRule(newKeyword.trim(), envId, newFeeText.toDoubleOrNull() ?: 0.0)
                            newKeyword = ""
                        }
                    },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary, contentColor = ObsidianBg)
                ) {
                    Text("Add")
                }
            }
        }

        items(rules) { rule ->
            val envName = envelopes.firstOrNull { it.id == rule.targetEnvelopeId }?.name ?: "Envelope #${rule.targetEnvelopeId}"
            MinimalistCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = rule.pattern,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "$envName" + if (rule.defaultFee > 0) " (+₱${rule.defaultFee.toInt()})" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = SageBright
                    )
                }
            }
        }
    }
}
