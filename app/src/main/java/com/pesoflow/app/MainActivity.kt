package com.pesoflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pesoflow.app.ui.components.PesoFlowLogo
import com.pesoflow.app.ui.components.QuickAddSheet
import com.pesoflow.app.ui.screens.BillsCalendarScreen
import com.pesoflow.app.ui.screens.DashboardScreen
import com.pesoflow.app.ui.screens.GCashSyncScreen
import com.pesoflow.app.ui.screens.OnboardingScreen
import com.pesoflow.app.ui.screens.SavingsGoalsScreen
import com.pesoflow.app.ui.theme.CharcoalSurface
import com.pesoflow.app.ui.theme.ObsidianBg
import com.pesoflow.app.ui.theme.PesoFlowTheme
import com.pesoflow.app.ui.theme.SageBright
import com.pesoflow.app.ui.theme.SageContainer
import com.pesoflow.app.ui.theme.SagePrimary
import com.pesoflow.app.ui.theme.TextPrimary
import com.pesoflow.app.ui.theme.TextSecondary
import com.pesoflow.app.ui.viewmodel.BudgetViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val openQuickAddFromWidget = intent?.getBooleanExtra("OPEN_QUICK_ADD", false) == true

        setContent {
            PesoFlowTheme {
                val viewModel: BudgetViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                if (!uiState.isInitialized) {
                    Box(modifier = Modifier)
                    return@PesoFlowTheme
                }

                if (uiState.requiresOnboarding) {
                    OnboardingScreen(
                        onComplete = { gcash, cash, bank ->
                            viewModel.completeOnboarding(gcash, cash, bank)
                        }
                    )
                    return@PesoFlowTheme
                }

                var selectedTab by remember { mutableIntStateOf(0) }
                var showQuickAdd by remember { mutableStateOf(openQuickAddFromWidget) }

                val navColors = NavigationBarItemDefaults.colors(
                    selectedIconColor = SageBright,
                    selectedTextColor = SageBright,
                    indicatorColor = SageContainer,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                )

                Scaffold(
                    containerColor = ObsidianBg,
                    topBar = {
                        TopAppBar(
                            colors = TopAppBarDefaults.topAppBarColors(
                                containerColor = ObsidianBg,
                                titleContentColor = TextPrimary
                            ),
                            title = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    PesoFlowLogo(size = 30.dp)
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "PesoFlow",
                                        fontWeight = FontWeight.SemiBold,
                                        letterSpacing = (-0.4).sp
                                    )
                                }
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(
                            onClick = { showQuickAdd = true },
                            containerColor = SagePrimary,
                            contentColor = ObsidianBg,
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Quick Add Expense")
                        }
                    },
                    bottomBar = {
                        NavigationBar(
                            containerColor = CharcoalSurface,
                            tonalElevation = 0.dp
                        ) {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                                label = { Text("Budget") },
                                colors = navColors
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                                label = { Text("Bills") },
                                colors = navColors
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(Icons.Default.Savings, contentDescription = null) },
                                label = { Text("Goals") },
                                colors = navColors
                            )
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = { Icon(Icons.Default.Sync, contentDescription = null) },
                                label = { Text("GCash") },
                                colors = navColors
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> DashboardScreen(
                                state = uiState,
                                onAssignEnvelope = { env, amt -> viewModel.assignFundsToEnvelope(env, amt) },
                                onUpdateAccountBalance = { acc, bal -> viewModel.updateAccountBalance(acc, bal) },
                                onCreateEnvelope = { name, grp, amt -> viewModel.addEnvelope(name, grp, amt) }
                            )
                            1 -> BillsCalendarScreen(
                                bills = uiState.recurringBills,
                                onTogglePaid = { viewModel.toggleBillPaid(it) },
                                onAddBill = { name, amt, fee, day -> viewModel.addRecurringBill(name, amt, fee, day) }
                            )
                            2 -> SavingsGoalsScreen(
                                goals = uiState.savingsGoals,
                                onContribute = { goal, amt -> viewModel.contributeToGoal(goal, amt) },
                                onAddGoal = { name, target, date -> viewModel.addSavingsGoal(name, target, date) }
                            )
                            3 -> GCashSyncScreen(
                                totalConvenienceFees = uiState.totalConvenienceFees,
                                rules = uiState.rules,
                                envelopes = uiState.envelopes,
                                onSimulateGCashNotification = { viewModel.simulateGCashNotification(it) },
                                onAddRule = { pattern, envId, fee -> viewModel.addSmartRule(pattern, envId, fee) }
                            )
                        }
                    }

                    if (showQuickAdd) {
                        QuickAddSheet(
                            envelopes = uiState.envelopes,
                            onDismiss = { showQuickAdd = false },
                            onAddManual = { merchant, amount, fee, type, accountId, envelopeId ->
                                viewModel.addManualTransaction(merchant, amount, fee, type, accountId, envelopeId)
                            },
                            onAddNaturalLanguage = { prompt ->
                                viewModel.addNaturalLanguageTransaction(prompt)
                            }
                        )
                    }
                }
            }
        }
    }
}
