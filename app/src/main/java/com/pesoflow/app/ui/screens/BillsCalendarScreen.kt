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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pesoflow.app.data.local.entities.RecurringBillEntity
import com.pesoflow.app.ui.components.MinimalistCard
import com.pesoflow.app.ui.theme.CharcoalSurface
import com.pesoflow.app.ui.theme.ObsidianBg
import com.pesoflow.app.ui.theme.SageBright
import com.pesoflow.app.ui.theme.SageContainer
import com.pesoflow.app.ui.theme.SagePrimary
import com.pesoflow.app.ui.theme.SubtleBorder
import com.pesoflow.app.ui.theme.TerracottaRose
import com.pesoflow.app.ui.theme.TextPrimary
import com.pesoflow.app.ui.theme.TextSecondary

@Composable
fun BillsCalendarScreen(
    bills: List<RecurringBillEntity>,
    onTogglePaid: (RecurringBillEntity) -> Unit,
    onAddBill: (name: String, amount: Double, fee: Double, dueDay: Int) -> Unit = { _, _, _, _ -> }
) {
    val billDays = bills.associateBy { it.dueDayOfMonth }
    var showAddBillDialog by remember { mutableStateOf(false) }
    var billName by remember { mutableStateOf("") }
    var billAmount by remember { mutableStateOf("") }
    var billFee by remember { mutableStateOf("0") }
    var billDueDay by remember { mutableStateOf("15") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BILLS & SUBSCRIPTIONS",
                        style = MaterialTheme.typography.labelSmall,
                        color = SagePrimary,
                        letterSpacing = 1.4.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Monthly Due Calendar",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
                Text(
                    text = "+ Add Bill",
                    style = MaterialTheme.typography.labelLarge,
                    color = SagePrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable { showAddBillDialog = true }
                )
            }
        }

        // Minimalist 30-Day Grid
        item {
            MinimalistCard {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    for (week in 0 until 5) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            for (dayOffset in 1..6) {
                                val day = week * 6 + dayOffset
                                val matchingBill = billDays[day]
                                val bgColor = when {
                                    matchingBill == null -> Color.Transparent
                                    matchingBill.isPaidThisMonth -> SageContainer
                                    else -> TerracottaRose.copy(alpha = 0.22f)
                                }
                                val borderColor = when {
                                    matchingBill == null -> SubtleBorder.copy(alpha = 0.5f)
                                    matchingBill.isPaidThisMonth -> SagePrimary
                                    else -> TerracottaRose
                                }
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(bgColor)
                                        .border(1.dp, borderColor, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = if (matchingBill != null) FontWeight.Bold else FontWeight.Normal,
                                        color = when {
                                            matchingBill == null -> TextSecondary
                                            matchingBill.isPaidThisMonth -> SageBright
                                            else -> TerracottaRose
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        if (bills.isEmpty()) {
            item {
                MinimalistCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No recurring bills added yet", color = TextSecondary)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap '+ Add Bill' above to track Meralco, PLDT, Rent, or Spotify.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        } else {
            items(bills) { bill ->
                MinimalistCard {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = bill.name,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Medium,
                                color = TextPrimary
                            )
                            val feeNote = if (bill.convenienceFee > 0) " • +₱${bill.convenienceFee.toInt()} fee" else ""
                            Text(
                                text = "Day ${bill.dueDayOfMonth} • ${formatPeso(bill.amount)}$feeNote",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (bill.isPaidThisMonth) SageContainer else SagePrimary)
                                .clickable { onTogglePaid(bill) }
                                .padding(horizontal = 14.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = if (bill.isPaidThisMonth) "Paid ✓" else "Mark Paid",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = if (bill.isPaidThisMonth) SageBright else ObsidianBg
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddBillDialog) {
        AlertDialog(
            onDismissRequest = { showAddBillDialog = false },
            containerColor = CharcoalSurface,
            title = { Text("Add Recurring Bill", color = TextPrimary) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = billName,
                        onValueChange = { billName = it },
                        label = { Text("Bill or Subscription Name") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = billAmount,
                        onValueChange = { billAmount = it },
                        label = { Text("Monthly Amount (₱)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = billDueDay,
                            onValueChange = { billDueDay = it },
                            label = { Text("Due Day (1-31)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = billFee,
                            onValueChange = { billFee = it },
                            label = { Text("GCash Fee (₱)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = billAmount.toDoubleOrNull() ?: 0.0
                        val day = billDueDay.toIntOrNull() ?: 15
                        val fee = billFee.toDoubleOrNull() ?: 0.0
                        if (billName.isNotBlank() && amt > 0.0) {
                            onAddBill(billName.trim(), amt, fee, day)
                            billName = ""
                            billAmount = ""
                            showAddBillDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary, contentColor = ObsidianBg)
                ) {
                    Text("Save Bill")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddBillDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}
