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
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.pesoflow.app.data.local.entities.SavingsGoalEntity
import com.pesoflow.app.ui.theme.EmeraldGreen

@Composable
fun SavingsGoalsScreen(
    goals: List<SavingsGoalEntity>,
    onContribute: (SavingsGoalEntity, Double) -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Sinking Funds & Savings Goals",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Track long-term goals and sinking funds with visual progress bars.",
                style = MaterialTheme.typography.bodyMedium
            )
        }

        items(goals) { goal ->
            val pct = (goal.progressFraction * 100).toInt()
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(goal.name, fontWeight = FontWeight.Bold)
                            Text(
                                text = "Target Date: ${goal.targetDateIso} • ${if (goal.isSinkingFund) "Sinking Fund" else "Emergency Savings"}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Text(
                            text = "$pct%",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = EmeraldGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = { goal.progressFraction },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${formatPeso(goal.savedAmount)} / ${formatPeso(goal.targetAmount)}",
                            fontWeight = FontWeight.SemiBold
                        )
                        Button(onClick = { onContribute(goal, 1000.0) }) {
                            Text("+ Save ₱1,000")
                        }
                    }
                }
            }
        }
    }
}
