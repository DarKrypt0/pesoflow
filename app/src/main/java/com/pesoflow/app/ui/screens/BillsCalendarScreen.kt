package com.pesoflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pesoflow.app.data.local.entities.RecurringBillEntity
import com.pesoflow.app.ui.theme.EmeraldGreen
import com.pesoflow.app.ui.theme.GCashBlue
import com.pesoflow.app.ui.theme.RoseExpense

@Composable
fun BillsCalendarScreen(
    bills: List<RecurringBillEntity>,
    onTogglePaid: (RecurringBillEntity) -> Unit
) {
    val billDays = bills.associateBy { it.dueDayOfMonth }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Recurring Bills & Subscriptions Calendar",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Highlighted days have GCash bills or subscriptions due.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // Visual 30-Day Monthly Calendar Grid
        item {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    for (week in 0 until 5) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            for (dayOffset in 1..6) {
                                val day = week * 6 + dayOffset
                                val matchingBill = billDays[day]
                                val bgColor = when {
                                    matchingBill == null -> Color.Transparent
                                    matchingBill.isPaidThisMonth -> EmeraldGreen
                                    else -> RoseExpense
                                }
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(bgColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = day.toString(),
                                        fontWeight = if (matchingBill != null) FontWeight.Bold else FontWeight.Normal,
                                        color = if (matchingBill != null) Color.White else MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        items(bills) { bill ->
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(bill.name, fontWeight = FontWeight.Bold)
                        val feeNote = if (bill.convenienceFee > 0) " (+₱${bill.convenienceFee.toInt()} GCash Biller Fee)" else ""
                        Text(
                            text = "Due every Day ${bill.dueDayOfMonth} • ${formatPeso(bill.amount)}$feeNote",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Button(onClick = { onTogglePaid(bill) }) {
                        Text(if (bill.isPaidThisMonth) "Paid ✓" else "Mark Paid")
                    }
                }
            }
        }
    }
}
