package com.pesoflow.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.pesoflow.app.data.local.entities.EnvelopeEntity
import com.pesoflow.app.data.local.entities.TransactionType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickAddSheet(
    envelopes: List<EnvelopeEntity>,
    onDismiss: () -> Unit,
    onAddManual: (merchant: String, amount: Double, fee: Double, type: TransactionType, accountId: Long, envelopeId: Long?) -> Unit,
    onAddNaturalLanguage: (prompt: String) -> Unit
) {
    var naturalLanguageInput by remember { mutableStateOf("") }
    var merchant by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var include15PesoFee by remember { mutableStateOf(false) }
    var customFeeText by remember { mutableStateOf("") }
    var isIncome by remember { mutableStateOf(false) }
    var selectedEnvelopeId by remember { mutableStateOf<Long?>(envelopes.firstOrNull()?.id) }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Text(
                text = "⚡ AI / Natural Language Quick-Add",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = naturalLanguageInput,
                onValueChange = { naturalLanguageInput = it },
                placeholder = { Text("e.g. Spent 185 on Jollibee via GCash + 15 fee") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = {
                    if (naturalLanguageInput.isNotBlank()) {
                        onAddNaturalLanguage(naturalLanguageInput)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Parse & Log with Smart Rules")
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Manual Fast Entry (Privacy-First)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = !isIncome,
                    onClick = { isIncome = false },
                    label = { Text("Expense") }
                )
                FilterChip(
                    selected = isIncome,
                    onClick = { isIncome = true },
                    label = { Text("Income / Cash-In") }
                )
                FilterChip(
                    selected = include15PesoFee,
                    onClick = { include15PesoFee = !include15PesoFee },
                    label = { Text("+₱15 InstaPay Fee") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant / Recipient") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = if (include15PesoFee) "15.0" else customFeeText,
                    onValueChange = {
                        include15PesoFee = false
                        customFeeText = it
                    },
                    label = { Text("Transfer Fee (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel")
                }
                Button(
                    onClick = {
                        val amt = amountText.toDoubleOrNull() ?: 0.0
                        val fee = if (include15PesoFee) 15.0 else (customFeeText.toDoubleOrNull() ?: 0.0)
                        if (amt > 0.0) {
                            onAddManual(
                                merchant.ifBlank { "GCash Expense" },
                                amt,
                                fee,
                                if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                                1L,
                                selectedEnvelopeId
                            )
                            onDismiss()
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save Transaction")
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
