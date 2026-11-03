package com.pesoflow.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pesoflow.app.data.local.entities.EnvelopeEntity
import com.pesoflow.app.data.local.entities.TransactionEntity
import com.pesoflow.app.data.local.entities.TransactionType
import com.pesoflow.app.ui.theme.LocalPesoFlowColors
import com.pesoflow.app.ui.theme.TerracottaRose

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun EditTransactionSheet(
    transaction: TransactionEntity,
    envelopes: List<EnvelopeEntity>,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onDelete: (TransactionEntity) -> Unit
) {
    val palette = LocalPesoFlowColors.current

    var merchant by remember(transaction) { mutableStateOf(transaction.merchantOrPayee) }
    var amountText by remember(transaction) { mutableStateOf(transaction.amount.toString()) }
    var feeText by remember(transaction) { mutableStateOf(transaction.convenienceFee.toString()) }
    var noteText by remember(transaction) { mutableStateOf(transaction.note) }
    var isIncome by remember(transaction) { mutableStateOf(transaction.type == TransactionType.INCOME) }
    var selectedEnvelopeId by remember(transaction) { mutableStateOf(transaction.envelopeId) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = palette.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 22.dp, vertical = 10.dp)
        ) {
            Text(
                text = "EDIT TRANSACTION",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = palette.textMuted,
                letterSpacing = 1.2.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = transaction.merchantOrPayee,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = palette.textPrimary
            )

            Spacer(modifier = Modifier.height(14.dp))

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
                    selected = feeText.toDoubleOrNull() == 15.0,
                    onClick = {
                        feeText = if (feeText.toDoubleOrNull() == 15.0) "0.0" else "15.0"
                    },
                    label = { Text("+₱15 Fee") }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = merchant,
                onValueChange = { merchant = it },
                label = { Text("Merchant / Recipient") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(10.dp))

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
                    value = feeText,
                    onValueChange = { feeText = it },
                    label = { Text("Transfer Fee (₱)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.75f),
                    singleLine = true
                )
            }

            if (!isIncome && envelopes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Category",
                    fontSize = 12.sp,
                    color = palette.textSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    envelopes.forEach { env ->
                        val selected = selectedEnvelopeId == env.id
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(if (selected) palette.chipActiveBg else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (selected) Color.Transparent else palette.divider,
                                    shape = RoundedCornerShape(50)
                                )
                                .clickable { selectedEnvelopeId = env.id }
                                .padding(horizontal = 14.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = env.name,
                                fontSize = 12.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (selected) palette.chipActiveText else palette.textSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = noteText,
                onValueChange = { noteText = it },
                label = { Text("Note (Optional)") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        onDelete(transaction)
                        onDismiss()
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(0.8f)
                ) {
                    Text("Delete", color = TerracottaRose, fontWeight = FontWeight.Medium)
                }
                Button(
                    onClick = {
                        val newAmt = amountText.replace(",", "").toDoubleOrNull() ?: transaction.amount
                        val newFee = feeText.replace(",", "").toDoubleOrNull() ?: 0.0
                        if (newAmt > 0.0) {
                            onSave(
                                transaction.copy(
                                    merchantOrPayee = merchant.trim().ifBlank { transaction.merchantOrPayee },
                                    amount = newAmt,
                                    convenienceFee = newFee,
                                    type = if (isIncome) TransactionType.INCOME else TransactionType.EXPENSE,
                                    envelopeId = if (isIncome) null else selectedEnvelopeId,
                                    note = noteText.trim()
                                )
                            )
                            onDismiss()
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = palette.chipActiveBg,
                        contentColor = palette.chipActiveText
                    ),
                    modifier = Modifier.weight(1.2f)
                ) {
                    Text("Save changes", fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
        }
    }
}
