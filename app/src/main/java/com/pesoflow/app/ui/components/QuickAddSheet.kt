package com.pesoflow.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.sp
import com.pesoflow.app.data.local.entities.EnvelopeEntity
import com.pesoflow.app.data.local.entities.TransactionType
import com.pesoflow.app.ui.theme.CharcoalSurface
import com.pesoflow.app.ui.theme.ObsidianBg
import com.pesoflow.app.ui.theme.SagePrimary
import com.pesoflow.app.ui.theme.SubtleBorder
import com.pesoflow.app.ui.theme.TextPrimary
import com.pesoflow.app.ui.theme.TextSecondary

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

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CharcoalSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            Text(
                text = "AI NATURAL LANGUAGE QUICK-ADD",
                style = MaterialTheme.typography.labelSmall,
                color = SagePrimary,
                letterSpacing = 1.4.sp
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = naturalLanguageInput,
                onValueChange = { naturalLanguageInput = it },
                placeholder = { Text("Spent 185 on Jollibee via GCash + 15 fee", color = TextSecondary.copy(alpha = 0.6f)) },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = {
                    if (naturalLanguageInput.isNotBlank()) {
                        onAddNaturalLanguage(naturalLanguageInput)
                        onDismiss()
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SagePrimary, contentColor = ObsidianBg),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Parse & Log Automatically", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(18.dp))
            HorizontalDivider(color = SubtleBorder)
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "MANUAL ENTRY",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                letterSpacing = 1.4.sp
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
                    label = { Text("Cash-In / Income") }
                )
                FilterChip(
                    selected = include15PesoFee,
                    onClick = { include15PesoFee = !include15PesoFee },
                    label = { Text("+₱15 Fee") }
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant / Recipient") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Amount (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = if (include15PesoFee) "15.0" else customFeeText,
                    onValueChange = {
                        include15PesoFee = false
                        customFeeText = it
                    },
                    label = { Text("Fee (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.75f),
                    singleLine = true
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancel", color = TextSecondary)
                }
                Button(
                    onClick = {
                        val amt = amountText.replace(",", "").toDoubleOrNull() ?: 0.0
                        val fee = if (include15PesoFee) 15.0 else (customFeeText.toDoubleOrNull() ?: 0.0)
                        if (amt > 0.0) {
                            onAddManual(
                                merchant.ifBlank { if (isIncome) "GCash Cash-In" else "GCash Expense" },
                                amt,
                                fee,
                                if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                                1L,
                                selectedEnvelopeId
                            )
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SagePrimary, contentColor = ObsidianBg),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Save", fontWeight = FontWeight.SemiBold, color = ObsidianBg)
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
