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
import androidx.compose.material3.Surface
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
import com.pesoflow.app.ui.components.PesoFlowLogo
import com.pesoflow.app.ui.theme.LocalPesoFlowColors

@Composable
fun OnboardingScreen(
    onComplete: (userName: String, gcashBalance: Double, cashBalance: Double, bankBalance: Double) -> Unit
) {
    val palette = LocalPesoFlowColors.current
    var nameText by remember { mutableStateOf("") }
    var gcashText by remember { mutableStateOf("") }
    var cashText by remember { mutableStateOf("") }
    var bankText by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(palette.background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp, vertical = 48.dp),
        verticalArrangement = Arrangement.Center
    ) {
        // Brand Logo + Wordmark matching Image 2
        Row(verticalAlignment = Alignment.CenterVertically) {
            PesoFlowLogo(size = 46.dp)
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = "PesoFlow",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = palette.textPrimary,
                letterSpacing = (-0.6).sp
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "A little clarity, every day.",
            style = MaterialTheme.typography.bodyMedium,
            color = palette.textSecondary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "Set up your wallet",
            fontSize = 30.sp,
            fontWeight = FontWeight.SemiBold,
            color = palette.textPrimary,
            letterSpacing = (-0.8).sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Main Sage Hero Card for Initial GCash Balance
        Surface(
            shape = RoundedCornerShape(22.dp),
            color = palette.overviewHeroBg,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(22.dp)) {
                Text(
                    text = "Primary GCash balance (Required)",
                    style = MaterialTheme.typography.bodySmall,
                    color = palette.overviewHeroSubtext
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = gcashText,
                    onValueChange = {
                        gcashText = it
                        showError = false
                    },
                    placeholder = { Text("8,120.00", color = palette.overviewHeroSubtext.copy(alpha = 0.5f)) },
                    prefix = {
                        Text(
                            text = "₱",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = palette.overviewHeroText
                        )
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = palette.overviewHeroText,
                        unfocusedBorderColor = palette.overviewHeroSubtext.copy(alpha = 0.35f),
                        focusedTextColor = palette.overviewHeroText,
                        unfocusedTextColor = palette.overviewHeroText
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                if (showError) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Please enter your starting GCash balance to continue.",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.overviewHeroText
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        OutlinedTextField(
            value = nameText,
            onValueChange = { nameText = it },
            label = { Text("Your first name (e.g. Ken)") },
            singleLine = true,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
                value = cashText,
                onValueChange = { cashText = it },
                label = { Text("Cash (Optional)") },
                prefix = { Text("₱") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = bankText,
                onValueChange = { bankText = it },
                label = { Text("Bank (Optional)") },
                prefix = { Text("₱") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.weight(1f)
            )
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
                    onComplete(nameText.ifBlank { "Ken" }, gcash, cash, bank)
                }
            },
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = palette.chipActiveBg,
                contentColor = palette.chipActiveText
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
        ) {
            Text(
                text = "Start budgeting",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
