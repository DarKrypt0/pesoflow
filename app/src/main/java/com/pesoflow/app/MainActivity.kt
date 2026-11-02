package com.pesoflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pesoflow.app.ui.components.QuickAddSheet
import com.pesoflow.app.ui.screens.BillsCalendarScreen
import com.pesoflow.app.ui.screens.DashboardScreen
import com.pesoflow.app.ui.screens.GCashSyncScreen
import com.pesoflow.app.ui.screens.SavingsGoalsScreen
import com.pesoflow.app.ui.theme.PesoFlowTheme
import com.pesoflow.app.ui.viewmodel.BudgetViewModel

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val openQuickAddFromWidget = intent?.getBooleanExtra("OPEN_QUICK_ADD", false) == true

        setContent {
            PesoFlowTheme {
                val viewModel: BudgetViewModel = viewModel()
                val uiState by viewModel.uiState.collectAsState()

                var selectedTab by remember { mutableIntStateOf(0) }
                var showQuickAdd by remember { mutableStateOf(openQuickAddFromWidget) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {
                                Text(
                                    text = "PesoFlow • Zero-Based & GCash",
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        )
                    },
                    floatingActionButton = {
                        FloatingActionButton(onClick = { showQuickAdd = true }) {
                            Icon(Icons.Default.Add, contentDescription = "Quick Add Expense")
                        }
                    },
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null) },
                                label = { Text("Budget") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null) },
                                label = { Text("Bills") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 2,
                                onClick = { selectedTab = 2 },
                                icon = { Icon(Icons.Default.Savings, contentDescription = null) },
                                label = { Text("Goals") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 3,
                                onClick = { selectedTab = 3 },
                                icon = { Icon(Icons.Default.Sync, contentDescription = null) },
                                label = { Text("GCash Sync") }
                            )
                        }
                    }
                ) { innerPadding ->
                    androidx.compose.foundation.layout.Box(modifier = Modifier.padding(innerPadding)) {
                        when (selectedTab) {
                            0 -> DashboardScreen(
                                state = uiState,
                                onAssignEnvelope = { env, amt -> viewModel.assignFundsToEnvelope(env, amt) }
                            )
                            1 -> BillsCalendarScreen(
                                bills = uiState.recurringBills,
                                onTogglePaid = { viewModel.toggleBillPaid(it) }
                            )
                            2 -> SavingsGoalsScreen(
                                goals = uiState.savingsGoals,
                                onContribute = { goal, amt -> viewModel.contributeToGoal(goal, amt) }
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
