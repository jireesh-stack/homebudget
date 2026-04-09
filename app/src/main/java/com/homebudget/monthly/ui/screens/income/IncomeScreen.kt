package com.homebudget.monthly.ui.screens.income

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
import com.homebudget.monthly.data.entities.Income
import com.homebudget.monthly.data.repository.BudgetRepository
import com.homebudget.monthly.ui.theme.IncomeGreen
import com.homebudget.monthly.viewmodel.IncomeViewModel
import com.homebudget.monthly.viewmodel.IncomeViewModelFactory
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncomeScreen(
    repository: BudgetRepository,
    onAddIncome: () -> Unit,
    onEditIncome: (Long) -> Unit,
    onBack: () -> Unit
) {
    val vm: IncomeViewModel = viewModel(factory = IncomeViewModelFactory(repository))
    val incomes by vm.incomes.collectAsState()
    val fmt = NumberFormat.getCurrencyInstance()
    var searchQuery by remember { mutableStateOf("") }
    var showDeleteDialog by remember { mutableStateOf<Income?>(null) }

    val filtered = if (searchQuery.isEmpty()) incomes
    else incomes.filter {
        it.title.contains(searchQuery, ignoreCase = true) ||
        it.categoryName.contains(searchQuery, ignoreCase = true)
    }

    val grouped = filtered.groupBy {
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(it.date))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Income", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = { IconButton(onClick = onAddIncome) { Icon(Icons.Default.Add, "Add") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddIncome,
                containerColor = IncomeGreen,
                contentColor = Color.White,
                shape = CircleShape
            ) { Icon(Icons.Default.Add, "Add") }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
        ) {
            val total = incomes.sumOf { it.amount }
            Card(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.1f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Total Income", style = MaterialTheme.typography.labelMedium, color = IncomeGreen)
                        Text(fmt.format(total), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = IncomeGreen)
                    }
                    Text("${incomes.size} entries", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search income...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) IconButton(onClick = { searchQuery = "" }) { Icon(Icons.Default.Close, null) }
                },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                grouped.forEach { (month, monthIncomes) ->
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(month, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(fmt.format(monthIncomes.sumOf { it.amount }), style = MaterialTheme.typography.titleSmall, color = IncomeGreen, fontWeight = FontWeight.SemiBold)
                        }
                    }
                    items(monthIncomes, key = { it.id }) { income ->
                        IncomeItem(income = income, fmt = fmt, onEdit = { onEditIncome(income.id) }, onDelete = { showDeleteDialog = income })
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { income ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Income") },
            text = { Text("Are you sure you want to delete \"${income.title}\"?") },
            confirmButton = {
                Button(onClick = { vm.deleteIncome(income); showDeleteDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun IncomeItem(income: Income, fmt: NumberFormat, onEdit: () -> Unit, onDelete: () -> Unit) {
    var showMenu by remember { mutableStateOf(false) }
    val color = runCatching { Color(android.graphics.Color.parseColor(income.categoryColor)) }.getOrDefault(IncomeGreen)

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp).clickable(onClick = onEdit),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(income.categoryIcon, fontSize = 22.sp) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(income.title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(2.dp))
                Row {
                    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
                        Text(income.categoryName, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text(SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date(income.date)), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
                if (income.isRecurring) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Repeat, null, modifier = Modifier.size(12.dp), tint = IncomeGreen)
                        Text(" Recurring", style = MaterialTheme.typography.labelSmall, color = IncomeGreen)
                    }
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("+${fmt.format(income.amount)}", fontWeight = FontWeight.Bold, color = IncomeGreen, style = MaterialTheme.typography.titleMedium)
                Box {
                    IconButton(onClick = { showMenu = true }, modifier = Modifier.size(24.dp)) {
                        Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(16.dp))
                    }
                    DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                        DropdownMenuItem(text = { Text("Edit") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { showMenu = false; onEdit() })
                        DropdownMenuItem(text = { Text("Delete", color = MaterialTheme.colorScheme.error) }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = MaterialTheme.colorScheme.error) }, onClick = { showMenu = false; onDelete() })
                    }
                }
            }
        }
    }
}
