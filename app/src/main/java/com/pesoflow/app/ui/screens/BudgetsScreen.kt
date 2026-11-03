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
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pesoflow.app.data.local.entities.EnvelopeEntity
import com.pesoflow.app.data.local.entities.RecurringBillEntity
import com.pesoflow.app.data.local.entities.SavingsGoalEntity
import com.pesoflow.app.ui.theme.LocalPesoFlowColors
import com.pesoflow.app.ui.viewmodel.BudgetUiState

/**
 * Matches the middle screen ("Your budgets") from the Figma mockup,
 * plus a sub-view selector to access Recurring Bills Calendar & Sinking Fund Goals.
 */
@Composable
fun BudgetsScreen(
    state: BudgetUiState,
    onUpdateCategoryBudget: (EnvelopeEntity, Double) -> Unit,
    onCreateCategory: (name: String, plannedAmount: Double) -> Unit,
    onToggleBillPaid: (RecurringBillEntity) -> Unit,
    onAddBill: (name: String, amount: Double, fee: Double, dueDay: Int) -> Unit,
    onContributeGoal: (SavingsGoalEntity, Double) -> Unit,
    onAddGoal: (name: String, target: Double, dateIso: String) -> Unit
) {
    val palette = LocalPesoFlowColors.current
    var activeSubTab by remember { mutableIntStateOf(0) } // 0 = Categories, 1 = Bills Calendar, 2 = Savings Goals
    var editingEnvelope by remember { mutableStateOf<EnvelopeEntity?>(null) }
    var editPlannedText by remember { mutableStateOf("") }

    var showNewCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }
    var newCategoryAmount by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
    ) {
        // Top Header ("Your budgets" + circular '+' button)
        Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Your budgets",
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
                        .clickable { showNewCategoryDialog = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Budget Category",
                        tint = palette.textPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Subtle Pill Switcher: Categories | Bills Calendar | Savings Goals
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Categories", "Bills Calendar", "Savings Goals").forEachIndexed { idx, title ->
                    val selected = activeSubTab == idx
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) palette.chipActiveBg else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (selected) Color.Transparent else palette.divider,
                                shape = RoundedCornerShape(50)
                            )
                            .clickable { activeSubTab = idx }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = title,
                            fontSize = 12.sp,
                            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                            color = if (selected) palette.chipActiveText else palette.textSecondary
                        )
                    }
                }
            }
        }

        when (activeSubTab) {
            0 -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 22.dp, vertical = 4.dp)
                ) {
                    // "Left to spend" Dark Hero Card (matches Figma center screen)
                    item {
                        Surface(
                            shape = RoundedCornerShape(22.dp),
                            color = palette.budgetsHeroBg,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 22.dp, vertical = 20.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = "Left to spend",
                                            fontSize = 12.sp,
                                            color = palette.budgetsHeroSubtext
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = formatPeso(state.leftToSpend),
                                            fontSize = 36.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = palette.budgetsHeroText,
                                            letterSpacing = (-1).sp
                                        )
                                    }
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(palette.budgetsHeroPillBg)
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = "${state.usedPercentage}% used",
                                            fontSize = 12.sp,
                                            color = palette.budgetsHeroSubtext
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                val ratio = if (state.totalPlannedBudget <= 0.0) 0f
                                else (state.totalSpentInEnvelopes / state.totalPlannedBudget)
                                    .toFloat().coerceIn(0f, 1f)

                                LinearProgressIndicator(
                                    progress = { ratio },
                                    color = Color(0xFFC5D5BC),
                                    trackColor = Color(0xFF2E3831),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(4.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "${formatPesoCompact(state.totalSpentInEnvelopes)} spent",
                                        fontSize = 12.sp,
                                        color = palette.budgetsHeroSubtext
                                    )
                                    Text(
                                        text = "${formatPesoCompact(state.totalPlannedBudget)} planned",
                                        fontSize = 12.sp,
                                        color = palette.budgetsHeroSubtext
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(26.dp))
                    }

                    // Categories Section Header
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Categories",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = palette.textPrimary
                            )
                            Text(
                                text = "Edit budgets",
                                fontSize = 13.sp,
                                color = palette.textSecondary,
                                modifier = Modifier.clickable {
                                    state.envelopes.firstOrNull()?.let { first ->
                                        editingEnvelope = first
                                        editPlannedText = first.assignedAmount.toInt().toString()
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    // Exact Figma Category Rows
                    itemsIndexed(state.envelopes) { idx, env ->
                        val catRatio = if (env.assignedAmount <= 0.0) 0f
                        else (env.spentAmount / env.assignedAmount).toFloat().coerceIn(0f, 1f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    editingEnvelope = env
                                    editPlannedText = env.assignedAmount.toInt().toString()
                                }
                                .padding(vertical = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = categoryIconFor(env.name),
                                contentDescription = null,
                                tint = palette.textSecondary,
                                modifier = Modifier.size(21.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = env.name,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = palette.textPrimary
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                LinearProgressIndicator(
                                    progress = { catRatio },
                                    color = palette.progressFill,
                                    trackColor = palette.progressTrack,
                                    modifier = Modifier
                                        .fillMaxWidth(0.78f)
                                        .height(3.5.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = formatPesoCompact(env.spentAmount),
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = palette.textPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "of ${formatPesoCompact(env.assignedAmount)}",
                                    fontSize = 11.sp,
                                    color = palette.textMuted
                                )
                            }
                        }
                        if (idx < state.envelopes.lastIndex) {
                            HorizontalDivider(color = palette.divider, thickness = 0.8.dp)
                        }
                    }

                    item { Spacer(modifier = Modifier.height(80.dp)) }
                }
            }
            1 -> {
                BillsCalendarScreen(
                    bills = state.recurringBills,
                    onTogglePaid = onToggleBillPaid,
                    onAddBill = onAddBill
                )
            }
            2 -> {
                SavingsGoalsScreen(
                    goals = state.savingsGoals,
                    onContribute = onContributeGoal,
                    onAddGoal = onAddGoal
                )
            }
        }
    }

    editingEnvelope?.let { env ->
        AlertDialog(
            onDismissRequest = { editingEnvelope = null },
            containerColor = palette.surface,
            title = { Text("Edit ${env.name} budget", color = palette.textPrimary) },
            text = {
                OutlinedTextField(
                    value = editPlannedText,
                    onValueChange = { editPlannedText = it },
                    label = { Text("Monthly Planned Budget (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val planned = editPlannedText.replace(",", "").toDoubleOrNull()
                        if (planned != null) {
                            onUpdateCategoryBudget(env, planned)
                        }
                        editingEnvelope = null
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
                TextButton(onClick = { editingEnvelope = null }) {
                    Text("Cancel", color = palette.textSecondary)
                }
            }
        )
    }

    if (showNewCategoryDialog) {
        AlertDialog(
            onDismissRequest = { showNewCategoryDialog = false },
            containerColor = palette.surface,
            title = { Text("New budget category", color = palette.textPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = newCategoryName,
                        onValueChange = { newCategoryName = it },
                        label = { Text("Category name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = newCategoryAmount,
                        onValueChange = { newCategoryAmount = it },
                        label = { Text("Monthly planned budget (₱)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newCategoryName.isNotBlank()) {
                            onCreateCategory(
                                newCategoryName.trim(),
                                newCategoryAmount.replace(",", "").toDoubleOrNull() ?: 0.0
                            )
                            newCategoryName = ""
                            newCategoryAmount = ""
                            showNewCategoryDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.chipActiveBg,
                        contentColor = palette.chipActiveText
                    )
                ) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showNewCategoryDialog = false }) {
                    Text("Cancel", color = palette.textSecondary)
                }
            }
        )
    }
}
