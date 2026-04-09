package com.homebudget.monthly.ui.screens.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homebudget.monthly.data.entities.Expense
import com.homebudget.monthly.data.entities.Income
import com.homebudget.monthly.data.repository.BudgetRepository
import com.homebudget.monthly.ui.components.ChartSegment
import com.homebudget.monthly.ui.components.DonutChart
import com.homebudget.monthly.ui.navigation.Screen
import com.homebudget.monthly.ui.theme.*
import com.homebudget.monthly.viewmodel.DashboardViewModel
import com.homebudget.monthly.viewmodel.DashboardViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    repository: BudgetRepository,
    onNavigate: (String) -> Unit,
    onDrawerOpen: () -> Unit
) {
    val vm: DashboardViewModel = viewModel(factory = DashboardViewModelFactory(repository))

    val totalExpense by vm.monthlyExpense.collectAsState(0.0)
    val totalIncome by vm.monthlyIncome.collectAsState(0.0)
    val totalBalance by vm.totalBalance.collectAsState(0.0)
    val totalBills by vm.totalUnpaidBills.collectAsState(0.0)
    val recentExpenses by vm.recentExpenses.collectAsState(emptyList())
    val recentIncomes by vm.recentIncomes.collectAsState(emptyList())
    val expensesByCategory by vm.expensesByCategory.collectAsState(emptyMap())
    val weeklyExpenses by vm.weeklyExpenses.collectAsState(emptyList())
    val weeklyIncomes by vm.weeklyIncomes.collectAsState(emptyList())

    val balance = totalIncome - totalExpense
    val fmt = NumberFormat.getCurrencyInstance()

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("HomeBudget", fontWeight = FontWeight.Bold, fontSize = 20.sp)
                        Text(
                            SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date()),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onDrawerOpen) {
                        Icon(Icons.Default.Menu, contentDescription = "Menu")
                    }
                },
                actions = {
                    IconButton(onClick = { onNavigate(Screen.Settings.route) }) {
                        Icon(Icons.Default.Notifications, contentDescription = "Notifications")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(Screen.AddExpense.createRoute()) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Expense")
            }
        }
    ) { padding ->
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn() + slideInVertically { it / 4 }
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(padding),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                // ---- Hero Balance Card ----
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .shadow(8.dp, RoundedCornerShape(24.dp))
                            .clip(RoundedCornerShape(24.dp))
                            .background(Brush.linearGradient(listOf(CardGradientStart, CardGradientEnd)))
                            .padding(24.dp)
                    ) {
                        Column {
                            Text(
                                "Total Balance",
                                color = Color.White.copy(alpha = 0.8f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                fmt.format(totalBalance),
                                color = Color.White,
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Monthly: ${fmt.format(balance)}",
                                color = if (balance >= 0) Color(0xFF90EE90) else Color(0xFFFFB3B3),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Spacer(Modifier.height(24.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                BalanceStat("Income", fmt.format(totalIncome), IncomeGreen)
                                BalanceStat("Expenses", fmt.format(totalExpense), ExpenseRed)
                                BalanceStat("Bills Due", fmt.format(totalBills), BillOrange)
                            }
                        }
                    }
                }

                // ---- Quick Actions ----
                item {
                    Text(
                        "Quick Actions",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionButton(
                            label = "Add Expense",
                            icon = Icons.Default.Remove,
                            color = ExpenseRed,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(Screen.AddExpense.createRoute()) }
                        )
                        QuickActionButton(
                            label = "Add Income",
                            icon = Icons.Default.Add,
                            color = IncomeGreen,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(Screen.AddIncome.createRoute()) }
                        )
                        QuickActionButton(
                            label = "Bills",
                            icon = Icons.Default.Receipt,
                            color = BillOrange,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(Screen.Bills.route) }
                        )
                        QuickActionButton(
                            label = "Reports",
                            icon = Icons.Default.BarChart,
                            color = SavingsBlue,
                            modifier = Modifier.weight(1f),
                            onClick = { onNavigate(Screen.Reports.route) }
                        )
                    }
                }

                // ---- Monthly Overview Cards ----
                item {
                    Text(
                        "Monthly Overview",
                        modifier = Modifier.padding(horizontal = 16.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.height(8.dp))
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            OverviewCard(
                                title = "Expenses",
                                amount = fmt.format(totalExpense),
                                icon = "💸",
                                color = ExpenseRed,
                                onClick = { onNavigate(Screen.Expenses.route) }
                            )
                        }
                        item {
                            OverviewCard(
                                title = "Income",
                                amount = fmt.format(totalIncome),
                                icon = "💰",
                                color = IncomeGreen,
                                onClick = { onNavigate(Screen.Incomes.route) }
                            )
                        }
                        item {
                            OverviewCard(
                                title = "Bills Due",
                                amount = fmt.format(totalBills),
                                icon = "📋",
                                color = BillOrange,
                                onClick = { onNavigate(Screen.Bills.route) }
                            )
                        }
                        item {
                            OverviewCard(
                                title = "Savings",
                                amount = fmt.format(totalBalance),
                                icon = "🏦",
                                color = SavingsBlue,
                                onClick = { }
                            )
                        }
                    }
                }

                // ---- Weekly Summary ----
                item {
                    WeeklySummaryCard(
                        weeklyExpenses = weeklyExpenses,
                        weeklyIncomes = weeklyIncomes,
                        fmt = fmt,
                        onViewAll = { onNavigate(Screen.Reports.route) }
                    )
                }

                // ---- Expense Breakdown Chart ----
                if (expensesByCategory.isNotEmpty()) {
                    item {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp),
                            shape = RoundedCornerShape(20.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(20.dp)) {
                                Text(
                                    "Expense Breakdown",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(16.dp))
                                val segments = expensesByCategory.entries.take(6).map { (cat, amt) ->
                                    ChartSegment(cat.first, amt.toFloat(), Color(android.graphics.Color.parseColor(cat.second)))
                                }
                                if (segments.isNotEmpty()) {
                                    DonutChart(
                                        segments = segments,
                                        centerLabel = fmt.format(totalExpense),
                                        centerSubLabel = "Total",
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                // ---- Recent Transactions ----
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "Recent Transactions",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = { onNavigate(Screen.Expenses.route) }) {
                            Text("See All")
                        }
                    }
                }
                val recent = (recentExpenses.map { it to false } + recentIncomes.map { it to true })
                    .sortedByDescending {
                        when (val item = it.first) {
                            is Expense -> item.date
                            is Income -> item.date
                            else -> 0L
                        }
                    }.take(8)

                items(recent) { (item, isIncome) ->
                    when (item) {
                        is Expense -> TransactionRow(
                            icon = item.categoryIcon,
                            title = item.title,
                            category = item.categoryName,
                            amount = "-${fmt.format(item.amount)}",
                            amountColor = ExpenseRed,
                            date = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(item.date)),
                            color = Color(android.graphics.Color.parseColor(item.categoryColor))
                        )
                        is Income -> TransactionRow(
                            icon = item.categoryIcon,
                            title = item.title,
                            category = item.categoryName,
                            amount = "+${fmt.format(item.amount)}",
                            amountColor = IncomeGreen,
                            date = SimpleDateFormat("MMM dd", Locale.getDefault()).format(Date(item.date)),
                            color = Color(android.graphics.Color.parseColor(item.categoryColor))
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BalanceStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 12.sp)
        Box(modifier = Modifier.width(20.dp).height(2.dp).background(color).clip(RoundedCornerShape(1.dp)))
    }
}

@Composable
private fun QuickActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(color.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(label, fontSize = 10.sp, fontWeight = FontWeight.Medium, color = color)
    }
}

@Composable
private fun OverviewCard(
    title: String,
    amount: String,
    icon: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .width(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(icon, fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.labelMedium, color = color)
            Spacer(Modifier.height(4.dp))
            Text(amount, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun WeeklySummaryCard(
    weeklyExpenses: List<Expense>,
    weeklyIncomes: List<Income>,
    fmt: NumberFormat,
    onViewAll: () -> Unit
) {
    val weekExpTotal = weeklyExpenses.sumOf { it.amount }
    val weekIncTotal = weeklyIncomes.sumOf { it.amount }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Weekly Summary", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onViewAll) { Text("Reports") }
            }
            Spacer(Modifier.height(16.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                WeekStat("Income", fmt.format(weekIncTotal), IncomeGreen)
                Box(Modifier.width(1.dp).height(50.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                WeekStat("Spent", fmt.format(weekExpTotal), ExpenseRed)
                Box(Modifier.width(1.dp).height(50.dp).background(MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)))
                WeekStat("Net", fmt.format(weekIncTotal - weekExpTotal), if (weekIncTotal >= weekExpTotal) IncomeGreen else ExpenseRed)
            }
        }
    }
}

@Composable
private fun WeekStat(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        Spacer(Modifier.height(4.dp))
        Text(value, fontWeight = FontWeight.Bold, color = color, fontSize = 14.sp)
    }
}

@Composable
fun TransactionRow(
    icon: String,
    title: String,
    category: String,
    amount: String,
    amountColor: Color,
    date: String,
    color: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(icon, fontSize = 20.sp)
        }
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
            Text(category, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(amount, fontWeight = FontWeight.Bold, color = amountColor, style = MaterialTheme.typography.bodyMedium)
            Text(date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
        }
    }
}
