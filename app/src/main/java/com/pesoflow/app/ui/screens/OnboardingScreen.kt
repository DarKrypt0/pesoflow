package com.pesoflow.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.pesoflow.app.ui.components.MinimalistCard
import com.pesoflow.app.ui.components.PesoFlowLogo
import com.pesoflow.app.ui.theme.ObsidianBg
import com.pesoflow.app.ui.theme.SageBright
import com.pesoflow.app.ui.theme.SageContainer
import com.pesoflow.app.ui.theme.SagePrimary
import com.pesoflow.app.ui.theme.SubtleBorder
import com.pesoflow.app.ui.theme.TextPrimary
import com.pesoflow.app.ui.theme.TextSecondary

/**
 * First-launch minimalist onboarding flow requiring the user to input their
 * starting GCash wallet balance (plus optional Cash / Bank balances).
 */
@Composable
fun OnboardingScreen(
    onComplete: (gcashBalance: Double, cashBalance: Double, bankBalance: Double) -> Unit
) {
    var gcashText by remember { mutableStateOf("") }
    var cashText by remember { mutableStateOf("") }
    var bankText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianBg)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 40.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            PesoFlowLogo(size = 48.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = "PesoFlow",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    letterSpacing = (-0.5).sp
                )
                Text(
                    text = "MINIMALIST ZERO-BASED BUDGETING",
                    style = MaterialTheme.typography.labelSmall,
                    color = SagePrimary,
                    letterSpacing = 1.4.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Let's set your starting balance.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Medium,
            color = TextPrimary,
            letterSpacing = (-0.5).sp
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Enter how much you currently have in your GCash wallet so PesoFlow can calculate your exact Ready-to-Assign funds and track future transfers.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        MinimalistCard(
            containerColor = SageContainer.copy(alpha = 0.55f),
            borderColor = SagePrimary.copy(alpha = 0.45f)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "REQUIRED • PRIMARY WALLET",
                    style = MaterialTheme.typography.labelSmall,
                    color = SageBright,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Current GCash Balance",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = gcashText,
                    onValueChange = {
                        gcashText = it
                        showError = false
                    },
                    placeholder = { Text("0.00", color = TextSecondary.copy(alpha = 0.5f)) },
                    prefix = {
                        Text(
                            text = "₱ ",
                            color = SagePrimary,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = SagePrimary,
                        unfocusedBorderColor = SubtleBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Please enter your current GCash balance (0 or more) to continue.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        MinimalistCard {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "OPTIONAL • OTHER LIQUID ACCOUNTS",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = cashText,
                    onValueChange = { cashText = it },
                    label = { Text("Cash on Hand (Optional)") },
                    prefix = { Text("₱ ", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = bankText,
                    onValueChange = { bankText = it },
                    label = { Text("Bank Savings / Payroll (Optional)") },
                    prefix = { Text("₱ ", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = {
                val gcash = gcashText.replace(",", "").trim().toDoubleOrNull()
                if (gcash == null || gcash < 0.0) {
                    showError = true
                } else {
                    val cash = cashText.replace(",", "").trim().toDoubleOrNull() ?: 0.0
                    val bank = bankText.replace(",", "").trim().toDoubleOrNull() ?: 0.0
                    onComplete(gcash, cash, bank)
                }
            },
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SagePrimary,
                contentColor = ObsidianBg
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Continue to PesoFlow →",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
