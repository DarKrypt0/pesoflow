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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pesoflow.app.data.local.entities.TransactionType
import com.pesoflow.app.ui.theme.LocalPesoFlowColors
import com.pesoflow.app.ui.viewmodel.BudgetUiState
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Matches the right screen ("Activity") from the Figma mockup,
 * plus a "GCash Sync" pill / tune button to manage real-time GCash sync & fee analytics.
 */
@Composable
fun ActivityScreen(
    state: BudgetUiState,
    onOpenQuickAdd: () -> Unit,
    onSimulateGCashNotification: (String) -> Unit,
    onAddRule: (String, Long, Double) -> Unit
) {
    val palette = LocalPesoFlowColors.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableIntStateOf(0) } // 0 = All, 1 = Expenses, 2 = Income, 3 = GCash Sync

    val envelopeNameById = remember(state.envelopes) {
        state.envelopes.associate { it.id to it.name }
    }

    val filteredTransactions = remember(state.transactions, searchQuery, selectedFilter) {
        state.transactions.filter { txn ->
            val matchesQuery = searchQuery.isBlank() ||
                txn.merchantOrPayee.contains(searchQuery, ignoreCase = true) ||
                txn.note.contains(searchQuery, ignoreCase = true)
            val matchesType = when (selectedFilter) {
                1 -> txn.type == TransactionType.EXPENSE
                2 -> txn.type == TransactionType.INCOME
                else -> true
            }
            matchesQuery && matchesType
        }
    }

    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.US) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
            // 1. Top Header ("Activity" + circular Tune/Sync button)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Activity",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = palette.textPrimary,
                        letterSpacing = (-0.6).sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "November 2026  ⌄",
                        fontSize = 13.sp,
                        color = palette.textSecondary
                    )
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .border(1.dp, palette.divider, CircleShape)
                        .clickable {
                            selectedFilter = if (selectedFilter == 3) 0 else 3
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Tune,
                        contentDescription = "GCash Sync & Filters",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. "Spent this month" Big Display Row
            Text(
                text = "Spent this month",
                fontSize = 12.sp,
                color = palette.textMuted
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = formatPeso(state.monthlyExpenses),
                    fontSize = 36.sp,
                    fontWeight = FontWeight.Medium,
                    color = palette.textPrimary,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = "${state.transactions.size} transactions",
                    fontSize = 12.sp,
                    color = palette.textMuted,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Minimalist Rounded Search Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(palette.searchBarBg)
                    .padding(horizontal = 14.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Search,
                    contentDescription = null,
                    tint = palette.textMuted,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Box(modifier = Modifier.weight(1f)) {
                    if (searchQuery.isEmpty()) {
                        Text(
                            text = "Search transactions",
                            fontSize = 14.sp,
                            color = palette.textMuted
                        )
                    }
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        singleLine = true,
                        textStyle = TextStyle(
                            color = palette.textPrimary,
                            fontSize = 14.sp
                        ),
                        cursorBrush = SolidColor(palette.textPrimary),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 4. Filter Pills ("All", "Expenses", "Income", "GCash Sync")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("All", "Expenses", "Income", "GCash Sync").forEachIndexed { idx, label ->
                    val selected = selectedFilter == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) palette.chipActiveBg else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (selected) Color.Transparent else palette.divider,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { selectedFilter = idx }
                            .padding(horizontal = 16.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = label,
                            fontSize = 13.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) palette.chipActiveText else palette.textSecondary
                        )
                    }
                }
            }
        }

        if (selectedFilter == 3) {
            GCashSyncScreen(
                totalConvenienceFees = state.totalConvenienceFees,
                rules = state.rules,
                envelopes = state.envelopes,
                onSimulateGCashNotification = onSimulateGCashNotification,
                onAddRule = onAddRule
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 22.dp, vertical = 6.dp)
            ) {
                item {
                    val netToday = filteredTransactions.sumOf {
                        if (it.type == TransactionType.INCOME) it.amount else -it.totalCashOutflow
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "TODAY, NOV 3",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.textMuted,
                            letterSpacing = 0.8.sp
                        )
                        Text(
                            text = if (netToday >= 0) "+${formatPeso(netToday)}" else "-${formatPeso(-netToday)}",
                            fontSize = 11.sp,
                            color = palette.textMuted
                        )
                    }
                }

                if (filteredTransactions.isEmpty()) {
                    item {
                        Text(
                            text = "No matching transactions yet. Tap '+ Log transaction' below to add one.",
                            fontSize = 13.sp,
                            color = palette.textMuted,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    }
                } else {
                    itemsIndexed(filteredTransactions) { index, txn ->
                        val catName = txn.envelopeId?.let { envelopeNameById[it] }
                            ?: if (txn.type == TransactionType.INCOME) "Salary / Cash-In" else "General"
                        val timeStr = timeFormatter.format(Date(txn.timestampMillis))
                        val feeNote = if (txn.convenienceFee > 0) " · +₱${txn.convenienceFee.toInt()} fee" else ""

                        TransactionRowItem(
                            merchant = txn.merchantOrPayee,
                            subtitle = "$catName · $timeStr$feeNote",
                            amount = txn.totalCashOutflow,
                            isIncome = txn.type == TransactionType.INCOME,
                            icon = categoryIconFor(catName, txn.type)
                        )
                        if (index < filteredTransactions.lastIndex) {
                            HorizontalDivider(color = palette.divider, thickness = 0.8.dp)
                        }
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(18.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenQuickAdd() }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "+ Log transaction or AI quick-add",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = palette.textSecondary
                        )
                    }
                    Spacer(modifier = Modifier.height(70.dp))
                }
            }
        }
    }
}
