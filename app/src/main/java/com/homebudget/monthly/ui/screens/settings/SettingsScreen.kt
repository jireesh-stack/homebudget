package com.homebudget.monthly.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homebudget.monthly.data.entities.CategoryType
import com.homebudget.monthly.data.repository.BudgetRepository
import com.homebudget.monthly.ui.screens.expense.AddCategoryDialog
import com.homebudget.monthly.ui.theme.Primary
import com.homebudget.monthly.viewmodel.ExpenseViewModel
import com.homebudget.monthly.viewmodel.ExpenseViewModelFactory
import com.homebudget.monthly.viewmodel.IncomeViewModel
import com.homebudget.monthly.viewmodel.IncomeViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    repository: BudgetRepository,
    onBack: () -> Unit
) {
    val expenseVm: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(repository))
    val incomeVm: IncomeViewModel = viewModel(factory = IncomeViewModelFactory(repository))
    val expenseCategories by expenseVm.expenseCategories.collectAsState()
    val incomeCategories by incomeVm.incomeCategories.collectAsState()

    var showExpenseCategoryDialog by remember { mutableStateOf(false) }
    var showIncomeCategoryDialog by remember { mutableStateOf(false) }
    var darkMode by remember { mutableStateOf(false) }
    var notifications by remember { mutableStateOf(true) }
    var currency by remember { mutableStateOf("USD") }
    var selectedSettingsTab by remember { mutableStateOf(0) }
    var showClearDataDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Profile Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(modifier = Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary),
                            contentAlignment = Alignment.Center
                        ) { Text("💰", fontSize = 32.sp) }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("HomeBudget", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                            Text("Monthly Budget Tracker", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text("v1.0.0", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }

            // General Settings
            item { SettingsSectionHeader("General") }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.DarkMode, title = "Dark Mode",
                    subtitle = "Switch to dark theme", checked = darkMode,
                    onToggle = { darkMode = it }
                )
            }
            item {
                SettingsToggleItem(
                    icon = Icons.Default.Notifications, title = "Notifications",
                    subtitle = "Bill reminders and alerts", checked = notifications,
                    onToggle = { notifications = it }
                )
            }
            item {
                SettingsClickableItem(
                    icon = Icons.Default.AttachMoney, title = "Currency",
                    subtitle = currency, onClick = {}
                )
            }

            // Categories Section
            item { SettingsSectionHeader("Categories") }
            item {
                SettingsClickableItem(
                    icon = Icons.Default.Receipt, title = "Expense Categories",
                    subtitle = "${expenseCategories.size} categories",
                    onClick = { showExpenseCategoryDialog = true }
                )
            }
            item {
                SettingsClickableItem(
                    icon = Icons.Default.AccountBalance, title = "Income Categories",
                    subtitle = "${incomeCategories.size} categories",
                    onClick = { showIncomeCategoryDialog = true }
                )
            }

            // Expense categories list
            if (showExpenseCategoryDialog) {
                items(expenseCategories) { cat ->
                    val color = runCatching { Color(android.graphics.Color.parseColor(cat.color)) }.getOrDefault(Primary)
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(cat.icon, fontSize = 20.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(cat.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = { expenseVm.deleteCategory(cat) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                item {
                    TextButton(
                        onClick = { showExpenseCategoryDialog = false },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Hide Categories") }
                }
            }

            if (showIncomeCategoryDialog) {
                items(incomeCategories) { cat ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 2.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(cat.icon, fontSize = 20.sp)
                            Spacer(Modifier.width(12.dp))
                            Text(cat.name, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                            IconButton(onClick = { incomeVm.deleteCategory(cat) }, modifier = Modifier.size(32.dp)) {
                                Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                }
                item {
                    TextButton(onClick = { showIncomeCategoryDialog = false }, modifier = Modifier.fillMaxWidth()) { Text("Hide Categories") }
                }
            }

            // Data Section
            item { SettingsSectionHeader("Data") }
            item {
                SettingsClickableItem(
                    icon = Icons.Default.FileDownload, title = "Export Data",
                    subtitle = "Download all data as CSV", onClick = {}
                )
            }
            item {
                SettingsClickableItem(
                    icon = Icons.Default.DeleteForever, title = "Clear All Data",
                    subtitle = "Remove all transactions", onClick = { showClearDataDialog = true },
                    titleColor = MaterialTheme.colorScheme.error
                )
            }

            // About
            item { SettingsSectionHeader("About") }
            item {
                SettingsClickableItem(icon = Icons.Default.Info, title = "Version", subtitle = "1.0.0", onClick = {})
            }
            item {
                SettingsClickableItem(icon = Icons.Default.Star, title = "Rate App", subtitle = "Love the app? Leave a review", onClick = {})
            }
        }
    }

    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            title = { Text("Clear All Data") },
            text = { Text("This will permanently delete all your transactions, bills, and categories. This action cannot be undone.") },
            confirmButton = {
                Button(onClick = { showClearDataDialog = false }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete All") }
            },
            dismissButton = { TextButton(onClick = { showClearDataDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        title,
        modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        fontWeight = FontWeight.SemiBold
    )
}

@Composable
private fun SettingsToggleItem(
    icon: ImageVector, title: String, subtitle: String, checked: Boolean, onToggle: (Boolean) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Switch(checked = checked, onCheckedChange = onToggle)
        }
    }
}

@Composable
private fun SettingsClickableItem(
    icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit,
    titleColor: Color = MaterialTheme.colorScheme.onSurface
) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 3.dp).clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(modifier = Modifier.size(40.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer), contentAlignment = Alignment.Center) {
                Icon(icon, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Medium, style = MaterialTheme.typography.bodyLarge, color = titleColor)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            }
            Icon(Icons.Default.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))
        }
    }
}
