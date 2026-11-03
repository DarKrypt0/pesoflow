package com.pesoflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.DirectionsTransit
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocalCafe
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingBasket
import androidx.compose.material.icons.outlined.WorkOutline
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.pesoflow.app.ui.theme.LocalPesoFlowColors
import com.pesoflow.app.ui.theme.PesoFlowTheme
import com.pesoflow.app.ui.viewmodel.BudgetUiState
import java.util.Locale

@Composable
fun DashboardScreen(
    state: BudgetUiState,
    onNavigateToBudgets: () -> Unit = {},
    onNavigateToActivity: () -> Unit = {},
    onToggleTheme: () -> Unit = {},
    onUpdateAccountBalance: (AccountEntity, Double) -> Unit = { _, _ -> },
    onSelectTransaction: (TransactionEntity) -> Unit = {}
) {
    val palette = LocalPesoFlowColors.current
    var showEditBalanceDialog by remember { mutableStateOf(false) }
    var editBalanceText by remember { mutableStateOf("") }

    val envelopeNameById = remember(state.envelopes) {
        state.envelopes.associate { it.id to it.name }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background),
        contentPadding = PaddingValues(horizontal = 22.dp, vertical = 16.dp)
    ) {
        // 1. Greeting Header + Initials Avatar (toggles Light / Dark theme on tap)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "A little clarity, every day.",
                        fontSize = 13.sp,
                        color = palette.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Hello, ${state.userName}",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary,
                        letterSpacing = (-0.6).sp
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (palette.isDark) palette.surface else palette.chipActiveBg)
                        .border(1.dp, palette.divider, CircleShape)
                        .clickable { onToggleTheme() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.userInitials,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = if (palette.isDark) palette.textPrimary else palette.chipActiveText
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 2. Total Balance Sage Hero Card (matches Figma Overview card)
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = palette.overviewHeroBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        editBalanceText = state.gcashBalance.toString()
                        showEditBalanceDialog = true
                    }
            ) {
                Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Total balance",
                            fontSize = 13.sp,
                            color = palette.overviewHeroSubtext
                        )
                        Text(
                            text = "↗",
                            fontSize = 16.sp,
                            color = palette.overviewHeroSubtext
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = formatPeso(state.netWorth),
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Medium,
                        color = palette.overviewHeroText,
                        letterSpacing = (-1.2).sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    val accCount = state.accounts.size.coerceAtLeast(1)
                    Text(
                        text = "Across $accCount connected ${if (accCount == 1) "account (GCash)" else "accounts (GCash primary)"}",
                        fontSize = 13.sp,
                        color = palette.overviewHeroSubtext
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "↻ Updated just now · Tap to adjust GCash balance",
                        fontSize = 11.sp,
                        color = palette.overviewHeroSubtext.copy(alpha = 0.85f)
                    )
                }
            }
            Spacer(modifier = Modifier.height(22.dp))
        }

        // 3. Month Selector + Income & Expenses Two-Column Section
        item {
            Text(
                text = "November 2026  ⌄",
                fontSize = 13.sp,
                color = palette.textSecondary
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "↙  Income",
                        fontSize = 12.sp,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatPeso(state.monthlyIncome),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary,
                        letterSpacing = (-0.5).sp
                    )
                }
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(42.dp)
                        .background(palette.divider)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 20.dp)
                ) {
                    Text(
                        text = "↗  Expenses",
                        fontSize = 12.sp,
                        color = palette.textMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatPeso(state.monthlyExpenses),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary,
                        letterSpacing = (-0.5).sp
                    )
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            HorizontalDivider(color = palette.divider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 4. "Room to breathe" Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Room to breathe",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary
                )
                Text(
                    text = "View budgets",
                    fontSize = 13.sp,
                    color = palette.textSecondary,
                    modifier = Modifier.clickable { onNavigateToBudgets() }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "${formatPesoCompact(state.leftToSpend)} left",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "of ${formatPesoCompact(state.totalPlannedBudget)} budget",
                    fontSize = 12.sp,
                    color = palette.textMuted
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            val breatheProgress = if (state.totalPlannedBudget <= 0.0) 0f
            else ((state.totalPlannedBudget - state.totalSpentInEnvelopes) / state.totalPlannedBudget)
                .toFloat().coerceIn(0f, 1f)
            LinearProgressIndicator(
                progress = { breatheProgress },
                color = palette.progressFill,
                trackColor = palette.progressTrack,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
            )
            Spacer(modifier = Modifier.height(22.dp))
            HorizontalDivider(color = palette.divider, thickness = 1.dp)
            Spacer(modifier = Modifier.height(20.dp))
        }

        // 5. "Recent activity" Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent activity",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = palette.textPrimary
                )
                Text(
                    text = "See all",
                    fontSize = 13.sp,
                    color = palette.textSecondary,
                    modifier = Modifier.clickable { onNavigateToActivity() }
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
        }

        if (state.transactions.isEmpty()) {
            item {
                Text(
                    text = "No transactions yet. Tap + or pay via GCash to auto-log.",
                    fontSize = 13.sp,
                    color = palette.textMuted,
                    modifier = Modifier.padding(vertical = 18.dp)
                )
            }
        } else {
            val recent = state.transactions.take(5)
            itemsIndexed(recent) { index, txn ->
                val categoryName = txn.envelopeId?.let { envelopeNameById[it] }
                    ?: if (txn.type == TransactionType.INCOME) "Income" else "General"
                TransactionRowItem(
                    merchant = txn.merchantOrPayee,
                    subtitle = "$categoryName · Today",
                    amount = txn.totalCashOutflow,
                    isIncome = txn.type == TransactionType.INCOME,
                    icon = categoryIconFor(categoryName, txn.type),
                    onClick = { onSelectTransaction(txn) }
                )
                if (index < recent.lastIndex) {
                    HorizontalDivider(color = palette.divider, thickness = 0.8.dp)
                }
            }
        }

        item { Spacer(modifier = Modifier.height(80.dp)) }
    }

    if (showEditBalanceDialog) {
        AlertDialog(
            onDismissRequest = { showEditBalanceDialog = false },
            containerColor = palette.surface,
            title = { Text("Update GCash Wallet Balance", color = palette.textPrimary) },
            text = {
                OutlinedTextField(
                    value = editBalanceText,
                    onValueChange = { editBalanceText = it },
                    label = { Text("GCash Balance (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val updated = editBalanceText.replace(",", "").toDoubleOrNull()
                        val acc = state.gcashAccount
                        if (updated != null && acc != null) {
                            onUpdateAccountBalance(acc, updated)
                        }
                        showEditBalanceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.chipActiveBg,
                        contentColor = palette.chipActiveText
                    )
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditBalanceDialog = false }) {
                    Text("Cancel", color = palette.textSecondary)
                }
            }
        )
    }
}

@Composable
fun TransactionRowItem(
    merchant: String,
    subtitle: String,
    amount: Double,
    isIncome: Boolean,
    icon: ImageVector,
    onClick: () -> Unit = {}
) {
    val palette = LocalPesoFlowColors.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(palette.iconCircleBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = palette.iconTint,
                    modifier = Modifier.size(19.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = merchant,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.textPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = palette.textMuted
                )
            }
        }
        Text(
            text = if (isIncome) "+${formatPeso(amount)}" else "-${formatPeso(amount)}",
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isIncome) palette.positiveGreen else palette.textPrimary
        )
    }
}

fun categoryIconFor(categoryOrIconName: String, type: TransactionType = TransactionType.EXPENSE): ImageVector {
    if (type == TransactionType.INCOME) return Icons.Outlined.WorkOutline
    val lower = categoryOrIconName.lowercase()
    return when {
        lower.contains("home") || lower.contains("bill") -> Icons.Outlined.Home
        lower.contains("grocer") || lower.contains("market") -> Icons.Outlined.ShoppingBasket
        lower.contains("food") || lower.contains("drink") || lower.contains("coffee") -> Icons.Outlined.LocalCafe
        lower.contains("transport") || lower.contains("grab") || lower.contains("commute") -> Icons.Outlined.DirectionsTransit
        lower.contains("wellbeing") || lower.contains("health") || lower.contains("heart") -> Icons.Outlined.FavoriteBorder
        else -> Icons.Outlined.ShoppingBag
    }
}

fun formatPeso(value: Double): String =
    String.format(Locale.US, "₱%,.2f", value)

fun formatPesoCompact(value: Double): String =
    if (value % 1.0 == 0.0) String.format(Locale.US, "₱%,.0f", value)
    else String.format(Locale.US, "₱%,.2f", value)

@Preview(showBackground = true)
@Composable
fun DashboardScreenPreview() {
    PesoFlowTheme(darkTheme = false) {
        DashboardScreen(
            state = BudgetUiState(
                isInitialized = true,
                userName = "Alex",
                accounts = listOf(
                    AccountEntity(1L, "GCash Wallet", AccountType.GCASH, 8120.0, true),
                    AccountEntity(2L, "Cash", AccountType.CASH, 0.0, true)
                ),
                envelopes = listOf(
                    EnvelopeEntity(1L, "Home & bills", "Fixed", "home", 1500.0, 1500.0),
                    EnvelopeEntity(2L, "Groceries", "Essentials", "groceries", 650.0, 420.0)
                ),
                transactions = listOf(
                    TransactionEntity(
                        id = 1L,
                        merchantOrPayee = "Whole Foods Market",
                        amount = 84.0,
                        type = TransactionType.EXPENSE,
                        source = TransactionSource.GCASH_NOTIFICATION,
                        envelopeId = 2L
                    )
                )
            )
        )
    }
}
