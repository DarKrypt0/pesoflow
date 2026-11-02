package com.pesoflow.app.ui.screens

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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pesoflow.app.data.local.entities.EnvelopeEntity
import com.pesoflow.app.data.local.entities.TransactionType
import com.pesoflow.app.ui.theme.EmeraldGreen
import com.pesoflow.app.ui.theme.GCashBlue
import com.pesoflow.app.ui.theme.RoseExpense
import com.pesoflow.app.ui.viewmodel.BudgetUiState
import java.util.Locale

@Composable
fun DashboardScreen(
    state: BudgetUiState,
    onAssignEnvelope: (EnvelopeEntity, Double) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Hybrid Top Card: Zero-Based "Ready to Assign" + Balance Sheet Net Worth
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GCashBlue),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "ZERO-BASED BUDGET • READY TO ASSIGN",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f)
                    )
                    Text(
                        text = formatPeso(state.readyToAssign),
                        style = MaterialTheme.typography.headlineLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "GCash Primary Wallet",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = formatPeso(state.gcashBalance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "Net Worth (Assets - Debt)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                            )
                            Text(
                                text = formatPeso(state.netWorth),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
            }
        }

        // 2. Balance Sheet Accounts Snapshot
        item {
            Text(
                text = "Balance Sheet & Cashflow Accounts",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(state.accounts) { account ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(account.name, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = if (account.isAsset) "Liquid Asset • ${account.type}" else "Liability / Debt • ${account.type}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Text(
                        text = if (account.isAsset) formatPeso(account.balance) else "-${formatPeso(account.balance)}",
                        fontWeight = FontWeight.Bold,
                        color = if (account.isAsset) EmeraldGreen else RoseExpense
                    )
                }
            }
        }

        // 3. Zero-Based Budget Envelopes
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Zero-Based Envelopes (Assign Every Peso)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(state.envelopes) { envelope ->
            val ratio = if (envelope.assignedAmount <= 0.0) 0f
            else (envelope.spentAmount / envelope.assignedAmount).toFloat().coerceIn(0f, 1f)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(envelope.name, fontWeight = FontWeight.Bold)
                            Text(
                                text = "${envelope.groupName} • Assigned: ${formatPeso(envelope.assignedAmount)}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = formatPeso(envelope.availableAmount),
                                fontWeight = FontWeight.ExtraBold,
                                color = if (envelope.availableAmount >= 0) EmeraldGreen else RoseExpense
                            )
                            Text(
                                text = "Available",
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { ratio },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Spent: ${formatPeso(envelope.spentAmount)}",
                            style = MaterialTheme.typography.labelMedium
                        )
                        OutlinedButton(onClick = { onAssignEnvelope(envelope, 500.0) }) {
                            Text("+ Assign ₱500")
                        }
                    }
                }
            }
        }

        // 4. Recent GCash & Cashflow Transactions
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Recent Cashflow & GCash Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        items(state.transactions.take(10)) { txn ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(txn.merchantOrPayee, fontWeight = FontWeight.SemiBold)
                        val feeBadge = if (txn.convenienceFee > 0) " • +₱${txn.convenienceFee.toInt()} Fee" else ""
                        val refBadge = txn.gcashReferenceNo?.let { " • Ref #$it" } ?: ""
                        Text(
                            text = "${txn.source}$feeBadge$refBadge",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                        )
                    }
                    Text(
                        text = if (txn.type == TransactionType.INCOME) "+${formatPeso(txn.amount)}"
                        else "-${formatPeso(txn.totalCashOutflow)}",
                        fontWeight = FontWeight.Bold,
                        color = if (txn.type == TransactionType.INCOME) EmeraldGreen else RoseExpense
                    )
                }
            }
        }
    }
}

fun formatPeso(value: Double): String =
    String.format(Locale.US, "₱%,.2f", value)
