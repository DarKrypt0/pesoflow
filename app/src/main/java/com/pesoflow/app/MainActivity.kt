package com.pesoflow.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.PieChart
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.pesoflow.app.data.local.entities.TransactionEntity
import com.pesoflow.app.ui.components.EditTransactionSheet
import com.pesoflow.app.ui.components.QuickAddSheet
import com.pesoflow.app.ui.screens.ActivityScreen
import com.pesoflow.app.ui.screens.BudgetsScreen
import com.pesoflow.app.ui.screens.DashboardScreen
import com.pesoflow.app.ui.screens.OnboardingScreen
import com.pesoflow.app.ui.theme.LocalPesoFlowColors
import com.pesoflow.app.ui.theme.PesoFlowTheme
import com.pesoflow.app.ui.viewmodel.BudgetViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val openQuickAddFromWidget = intent?.getBooleanExtra("OPEN_QUICK_ADD", false) == true

        setContent {
            val viewModel: BudgetViewModel = viewModel()
            val uiState by viewModel.uiState.collectAsState()

            PesoFlowTheme(darkTheme = uiState.isDarkTheme) {
                val palette = LocalPesoFlowColors.current

                if (!uiState.isInitialized) {
                    Box(modifier = Modifier.background(palette.background))
                    return@PesoFlowTheme
                }

                if (uiState.requiresOnboarding) {
                    OnboardingScreen(
                        onComplete = { name, gcash, cash, bank ->
                            viewModel.completeOnboarding(name, gcash, cash, bank)
                        }
                    )
                    return@PesoFlowTheme
                }

                var selectedTab by remember { mutableIntStateOf(0) }
                var showQuickAdd by remember { mutableStateOf(openQuickAddFromWidget) }
                var editingTransaction by remember { mutableStateOf<TransactionEntity?>(null) }

                Scaffold(
                    containerColor = palette.background,
                    bottomBar = {
                        FigmaBottomBar(
                            selectedTab = selectedTab,
                            onSelectTab = { selectedTab = it }
                        )
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .padding(innerPadding)
                            .statusBarsPadding()
                    ) {
                        when (selectedTab) {
                            0 -> DashboardScreen(
                                state = uiState,
                                onNavigateToBudgets = { selectedTab = 1 },
                                onNavigateToActivity = { selectedTab = 2 },
                                onToggleTheme = { viewModel.toggleTheme() },
                                onUpdateAccountBalance = { acc, bal ->
                                    viewModel.updateAccountBalance(acc, bal)
                                },
                                onSelectTransaction = { editingTransaction = it }
                            )
                            1 -> BudgetsScreen(
                                state = uiState,
                                onUpdateCategoryBudget = { env, planned ->
                                    viewModel.setEnvelopePlannedAmount(env, planned)
                                },
                                onCreateCategory = { name, planned ->
                                    viewModel.addEnvelope(name, "Custom", planned)
                                },
                                onToggleBillPaid = { viewModel.toggleBillPaid(it) },
                                onAddBill = { name, amt, fee, day ->
                                    viewModel.addRecurringBill(name, amt, fee, day)
                                },
                                onContributeGoal = { goal, amt ->
                                    viewModel.contributeToGoal(goal, amt)
                                },
                                onAddGoal = { name, target, date ->
                                    viewModel.addSavingsGoal(name, target, date)
                                }
                            )
                            2 -> ActivityScreen(
                                state = uiState,
                                onOpenQuickAdd = { showQuickAdd = true },
                                onSelectTransaction = { editingTransaction = it },
                                onSimulateGCashNotification = {
                                    viewModel.simulateGCashNotification(it)
                                },
                                onAddRule = { pattern, envId, fee ->
                                    viewModel.addSmartRule(pattern, envId, fee)
                                }
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

                    editingTransaction?.let { txn ->
                        EditTransactionSheet(
                            transaction = txn,
                            envelopes = uiState.envelopes,
                            onDismiss = { editingTransaction = null },
                            onSave = { updated ->
                                viewModel.editTransaction(txn, updated)
                            },
                            onDelete = { toDelete ->
                                viewModel.deleteTransaction(toDelete)
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * Exact 3-item bottom navigation bar from the Figma design:
 * Overview (Grid) | Budgets (Pie) | Activity (Swap arrows)
 * with the oval sage pill indicator on the active icon.
 */
@Composable
private fun FigmaBottomBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    val palette = LocalPesoFlowColors.current
    val items = listOf(
        Triple(0, "Overview", Icons.Outlined.GridView),
        Triple(1, "Budgets", Icons.Outlined.PieChart),
        Triple(2, "Activity", Icons.Outlined.SwapHoriz)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(palette.navBarBg)
            .navigationBarsPadding()
    ) {
        HorizontalDivider(color = palette.divider, thickness = 0.8.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            for ((index, label, icon) in items) {
                val active = selectedTab == index
                FigmaNavItem(
                    label = label,
                    icon = icon,
                    active = active,
                    onClick = { onSelectTab(index) }
                )
            }
        }
    }
}

@Composable
private fun FigmaNavItem(
    label: String,
    icon: ImageVector,
    active: Boolean,
    onClick: () -> Unit
) {
    val palette = LocalPesoFlowColors.current
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(if (active) palette.navIndicatorPill else Color.Transparent)
                .padding(horizontal = 20.dp, vertical = 6.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (active) palette.navIconActive else palette.navIconInactive,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal,
            color = if (active) palette.textPrimary else palette.navIconInactive
        )
    }
}
