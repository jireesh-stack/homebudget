package com.homebudget.monthly.ui.screens.expense

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homebudget.monthly.data.entities.Expense
import com.homebudget.monthly.data.repository.BudgetRepository
import com.homebudget.monthly.ui.theme.ExpenseRed
import com.homebudget.monthly.viewmodel.ExpenseViewModel
import com.homebudget.monthly.viewmodel.ExpenseViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseScreen(
    repository: BudgetRepository,
    onAddExpense: () -> Unit,
    onEditExpense: (Long) -> Unit,
    onBack: () -> Unit
) {
    val vm: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(repository))
    val expenses by vm.expenses.collectAsState()
    val fmt = NumberFormat.getCurrencyInstance()
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<Expense?>(null) }

    val filtered = if (searchQuery.isEmpty()) expenses
    else expenses.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.categoryName.contains(searchQuery, ignoreCase = true)
    }

    // Group by month
    val grouped = filtered.groupBy {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(it.date))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Expenses", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onAddExpense) {
                        Icon(Icons.Default.Add, "Add Expense")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddExpense,
                containerColor = ExpenseRed,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(Icons.Default.Add, "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            // Summary bar
            val total = expenses.sumOf { it.amount }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Expenses", style = MaterialTheme.typography.labelMedium, color = ExpenseRed)
                        Text(fmt.format(total), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = ExpenseRed)
                    }
                    Text("${expenses.size} transactions", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            // Search
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search expenses...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, null)
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            LazyColumn(
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                grouped.forEach { (month, monthExpenses) ->
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                month,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                            Text(
                                fmt.format(monthExpenses.sumOf { it.amount }),
                                style = MaterialTheme.typography.titleSmall,
                                color = ExpenseRed,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                    items(monthExpenses, key = { it.id }) { expense ->
                        ExpenseItem(
                            expense = expense,
                            fmt = fmt,
                            onEdit = { onEditExpense(expense.id) },
                            onDelete = { showDeleteDialog = expense }
                        )
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { expense ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete \"${expense.title}\"?") },
            confirmButton = {
                Button(
                    onClick = { vm.deleteExpense(expense); showDeleteDialog = null },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun ExpenseItem(
    expense: Expense,
    fmt: NumberFormat,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val color = runCatching { Color(android.graphics.Color.parseColor(expense.categoryColor)) }
        .getOrDefault(ExpenseRed)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(expense.categoryIcon, fontSize = 22.sp)
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(expense.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(2.dp))
                Row {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            expense.categoryName,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = color
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(
                        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(expense.date)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    )
                }
                if (expense.note.isNotEmpty()) {
                    Text(
                        expense.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                        maxLines = 1
                    )
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    "-${fmt.format(expense.amount)}",
                    fontWeight = FontWeight.Bold,
                    color = ExpenseRed,
                    style = MaterialTheme.typography.titleMedium
                )
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("Edit") },
                            leadingIcon = { Icon(Icons.Default.Edit, null) },
                            onClick = { showMenu = false; onEdit() }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete", color = ExpenseRed) },
                            leadingIcon = { Icon(Icons.Default.Delete, null, tint = ExpenseRed) },
                            onClick = { showMenu = false; onDelete() }
                        )
                    }
                }
            }
        }
    }
}
