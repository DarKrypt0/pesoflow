package com.pesoflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pesoflow.app.data.local.entities.AccountEntity
import com.pesoflow.app.data.local.entities.AccountType
import com.pesoflow.app.data.local.entities.EnvelopeEntity
import com.pesoflow.app.data.local.entities.TransactionEntity
import com.pesoflow.app.data.local.entities.TransactionSource
import com.pesoflow.app.data.local.entities.TransactionType
import com.pesoflow.app.ui.components.MinimalistCard
import com.pesoflow.app.ui.theme.CharcoalSurface
import com.pesoflow.app.ui.theme.ObsidianBg
import com.pesoflow.app.ui.theme.PesoFlowTheme
import com.pesoflow.app.ui.theme.SageBright
import com.pesoflow.app.ui.theme.SageContainer
import com.pesoflow.app.ui.theme.SageMuted
import com.pesoflow.app.ui.theme.SagePrimary
import com.pesoflow.app.ui.theme.SubtleBorder
import com.pesoflow.app.ui.theme.TerracottaRose
import com.pesoflow.app.ui.theme.TextPrimary
import com.pesoflow.app.ui.theme.TextSecondary
import com.pesoflow.app.ui.viewmodel.BudgetUiState
import java.util.Locale

@Composable
fun DashboardScreen(
    state: BudgetUiState,
    onAssignEnvelope: (EnvelopeEntity, Double) -> Unit,
    onUpdateAccountBalance: (AccountEntity, Double) -> Unit = { _, _ -> },
    onCreateEnvelope: (name: String, group: String, assigned: Double) -> Unit = { _, _, _ -> }
) {
    var editingAccount by remember { mutableStateOf<AccountEntity?>(null) }
    var editBalanceText by remember { mutableStateOf("") }

    var assigningEnvelope by remember { mutableStateOf<EnvelopeEntity?>(null) }
    var assignAmountText by remember { mutableStateOf("") }

    var showNewEnvelopeDialog by remember { mutableStateOf(false) }
    var newEnvName by remember { mutableStateOf("") }
    var newEnvGroup by remember { mutableStateOf("Essentials") }
    var newEnvAssigned by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Minimalist Hero Card: Ready to Assign + GCash Balance + Net Worth
        item {
            MinimalistCard(
                containerColor = SageContainer,
                borderColor = SagePrimary.copy(alpha = 0.35f)
            ) {
                Column(modifier = Modifier.padding(22.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "READY TO ASSIGN",
                            style = MaterialTheme.typography.labelSmall,
                            color = SageBright,
                            letterSpacing = 1.5.sp
                        )
                        Text(
                            text = if (state.readyToAssign >= 0) "ZERO-BASED" else "OVER-ASSIGNED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (state.readyToAssign >= 0) SagePrimary else TerracottaRose,
                            letterSpacing = 1.2.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatPeso(state.readyToAssign),
                        fontSize = 34.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (state.readyToAssign >= 0) TextPrimary else TerracottaRose,
                        letterSpacing = (-1).sp
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    HorizontalDivider(color = SubtleBorder)
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(
                            modifier = Modifier.clickable {
                                state.gcashAccount?.let { acc ->
                                    editingAccount = acc
                                    editBalanceText = acc.balance.toString()
                                }
                            }
                        ) {
                            Text(
                                text = "GCASH WALLET (TAP TO EDIT)",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatPeso(state.gcashBalance),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.semiboldOrBold(),
                                color = SageBright
                            )
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "NET WORTH",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary,
                                letterSpacing = 0.8.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = formatPeso(state.netWorth),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 2. Horizontal Minimalist Balance Sheet Accounts
        if (state.accounts.isNotEmpty()) {
            item {
                Text(
                    text = "ACCOUNTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.4.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.accounts) { account ->
                        MinimalistCard(
                            modifier = Modifier
                                .width(165.dp)
                                .clickable {
                                    editingAccount = account
                                    editBalanceText = account.balance.toString()
                                }
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text(
                                    text = account.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = if (account.isAsset) formatPeso(account.balance) else "-${formatPeso(account.balance)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (account.isAsset) TextPrimary else TerracottaRose
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Zero-Based Envelopes Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ENVELOPES",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.4.sp
                )
                Text(
                    text = "+ New Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = SagePrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.clickable { showNewEnvelopeDialog = true }
                )
            }
        }

        items(state.envelopes) { envelope ->
            val ratio = if (envelope.assignedAmount <= 0.0) 0f
            else (envelope.spentAmount / envelope.assignedAmount).toFloat().coerceIn(0f, 1f)

            MinimalistCard(
                modifier = Modifier.clickable {
                    assigningEnvelope = envelope
                    assignAmountText = ""
                }
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = envelope.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "${envelope.groupName} • ${formatPeso(envelope.spentAmount)} of ${formatPeso(envelope.assignedAmount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (envelope.availableAmount >= 0) SageContainer else TerracottaRose.copy(alpha = 0.18f)
                                )
                                .padding(horizontal = 10.dp, vertical = 5.dp)
                        ) {
                            Text(
                                text = formatPeso(envelope.availableAmount),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (envelope.availableAmount >= 0) SageBright else TerracottaRose
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    LinearProgressIndicator(
                        progress = { ratio },
                        color = if (envelope.availableAmount >= 0) SagePrimary else TerracottaRose,
                        trackColor = CharcoalSurface,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "+₱500",
                            style = MaterialTheme.typography.labelMedium,
                            color = SageMuted,
                            modifier = Modifier
                                .clickable { onAssignEnvelope(envelope, 500.0) }
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Assign →",
                            style = MaterialTheme.typography.labelMedium,
                            color = SagePrimary,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier
                                .clickable {
                                    assigningEnvelope = envelope
                                    assignAmountText = ""
                                }
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }

        // 4. Minimalist Recent Activity Feed
        item {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "RECENT ACTIVITY",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.4.sp
            )
        }

        if (state.transactions.isEmpty()) {
            item {
                MinimalistCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "No transactions yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap + below or pay with GCash to auto-log expenses.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(state.transactions.take(15)) { txn ->
                MinimalistCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(SageContainer),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = txn.merchantOrPayee.take(1).uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = SageBright,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = txn.merchantOrPayee,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    color = TextPrimary
                                )
                                val feeBadge = if (txn.convenienceFee > 0) " • +₱${txn.convenienceFee.toInt()} fee" else ""
                                Text(
                                    text = "${txn.source.name.replace("_", " ").lowercase()}$feeBadge",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                        Text(
                            text = if (txn.type == TransactionType.INCOME) "+${formatPeso(txn.amount)}"
                            else "-${formatPeso(txn.totalCashOutflow)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = if (txn.type == TransactionType.INCOME) SageBright else TextPrimary
                        )
                    }
                }
            }
        }

        item { Spacer(modifier = Modifier.height(72.dp)) }
    }

    // Dialog: Edit Account / GCash Balance
    editingAccount?.let { account ->
        AlertDialog(
            onDismissRequest = { editingAccount = null },
            containerColor = CharcoalSurface,
            title = { Text("Update ${account.name}", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = editBalanceText,
                    onValueChange = { editBalanceText = it },
                    label = { Text("Current Balance (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = editBalanceText.replace(",", "").toDoubleOrNull()
                        if (updated != null) {
                            onUpdateAccountBalance(account, updated)
                        }
                        editingAccount = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary, contentColor = ObsidianBg)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingAccount = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Dialog: Assign Custom Amount to Envelope
    assigningEnvelope?.let { env ->
        AlertDialog(
            onDismissRequest = { assigningEnvelope = null },
            containerColor = CharcoalSurface,
            title = { Text("Assign to ${env.name}", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = assignAmountText,
                    onValueChange = { assignAmountText = it },
                    label = { Text("Amount to Add (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val delta = assignAmountText.replace(",", "").toDoubleOrNull() ?: 0.0
                        if (delta != 0.0) {
                            onAssignEnvelope(env, delta)
                        }
                        assigningEnvelope = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary, contentColor = ObsidianBg)
                ) {
                    Text("Assign")
                }
            },
            dismissButton = {
                TextButton(onClick = { assigningEnvelope = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Dialog: Create New Envelope Category
    if (showNewEnvelopeDialog) {
        AlertDialog(
            onDismissRequest = { showNewEnvelopeDialog = false },
            containerColor = CharcoalSurface,
            title = { Text("New Budget Envelope", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newEnvName,
                        onValueChange = { newEnvName = it },
                        label = { Text("Category Name (e.g. Coffee)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newEnvGroup,
                        onValueChange = { newEnvGroup = it },
                        label = { Text("Group (e.g. Essentials / Wants)") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newEnvAssigned,
                        onValueChange = { newEnvAssigned = it },
                        label = { Text("Initial Assigned Amount (₱)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newEnvName.isNotBlank()) {
                            onCreateEnvelope(
                                newEnvName.trim(),
                                newEnvGroup.trim().ifBlank { "General" },
                                newEnvAssigned.toDoubleOrNull() ?: 0.0
                            )
                            newEnvName = ""
                            newEnvAssigned = ""
                            showNewEnvelopeDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary, contentColor = ObsidianBg)
                ) {
                    Text("Create")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewEnvelopeDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

private fun FontWeight.Companion.semiboldOrBold() = FontWeight.SemiBold

fun formatPeso(value: Double): String =
    String.format(Locale.US, "₱%,.2f", value)

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    PesoFlowTheme {
        DashboardScreen(
            state = BudgetUiState(
                isInitialized = true,
                accounts = listOf(
                    AccountEntity(1L, "GCash Wallet", AccountType.GCASH, 12500.0, true),
                    AccountEntity(2L, "Cash on Hand", AccountType.CASH, 2000.0, true)
                ),
                envelopes = listOf(
                    EnvelopeEntity(1L, "Food & Dining", "Essentials", "restaurant", 6000.0, 1450.0),
                    EnvelopeEntity(2L, "Grab & Transit", "Essentials", "commute", 2500.0, 620.0),
                    EnvelopeEntity(3L, "Transfer & InstaPay Fees", "Buffer", "bolt", 300.0, 30.0)
                ),
                transactions = listOf(
                    TransactionEntity(
                        id = 1L,
                        merchantOrPayee = "Jollibee BGC",
                        amount = 245.0,
                        convenienceFee = 0.0,
                        type = TransactionType.EXPENSE,
                        source = TransactionSource.GCASH_NOTIFICATION
                    )
                )
            ),
            onAssignEnvelope = { _, _ -> }
        )
    }
}
