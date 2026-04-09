package com.homebudget.monthly.ui.screens.income

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import com.homebudget.monthly.data.entities.Category
import com.homebudget.monthly.data.entities.CategoryType
import com.homebudget.monthly.data.entities.Income
import com.homebudget.monthly.data.repository.BudgetRepository
import com.homebudget.monthly.ui.screens.expense.AddCategoryDialog
import com.homebudget.monthly.ui.screens.expense.BasicAmountField
import com.homebudget.monthly.ui.theme.IncomeGreen
import com.homebudget.monthly.viewmodel.IncomeViewModel
import com.homebudget.monthly.viewmodel.IncomeViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditIncomeScreen(
    repository: BudgetRepository,
    incomeId: Long = -1L,
    onBack: () -> Unit
) {
    val vm: IncomeViewModel = viewModel(factory = IncomeViewModelFactory(repository))
    val categories by vm.incomeCategories.collectAsState()
    val scope = rememberCoroutineScope()
    val isEdit = incomeId != -1L

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var isRecurring by remember { mutableStateOf(false) }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    LaunchedEffect(incomeId, categories) {
        if (isEdit && categories.isNotEmpty()) {
            val income = vm.getIncomeById(incomeId)
            income?.let {
                title = it.title; amount = it.amount.toString(); note = it.note
                selectedDate = it.date; isRecurring = it.isRecurring
                selectedCategory = categories.find { c -> c.id == it.categoryId }
                    ?: Category(id = -1, name = it.categoryName, icon = it.categoryIcon, color = it.categoryColor, type = CategoryType.INCOME)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Income" else "Add Income", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    TextButton(onClick = {
                        titleError = title.isBlank(); amountError = amount.toDoubleOrNull() == null
                        if (!titleError && !amountError) {
                            val income = Income(
                                id = if (isEdit) incomeId else 0, title = title.trim(),
                                amount = amount.toDouble(), categoryId = selectedCategory?.id,
                                categoryName = selectedCategory?.name ?: "Other Income",
                                categoryIcon = selectedCategory?.icon ?: "💰",
                                categoryColor = selectedCategory?.color ?: "#1ABC9C",
                                date = selectedDate, note = note.trim(), isRecurring = isRecurring
                            )
                            scope.launch { if (isEdit) vm.updateIncome(income) else vm.insertIncome(income); onBack() }
                        }
                    }) { Text("Save", color = IncomeGreen, fontWeight = FontWeight.SemiBold) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = IncomeGreen.copy(alpha = 0.05f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Amount", style = MaterialTheme.typography.labelLarge, color = IncomeGreen.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$", fontSize = 28.sp, color = IncomeGreen, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        BasicAmountField(value = amount, onValueChange = { amount = it; amountError = false }, isError = amountError)
                    }
                    if (amountError) Text("Enter a valid amount", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }

            OutlinedTextField(
                value = title, onValueChange = { title = it; titleError = false },
                label = { Text("Income Title *") }, leadingIcon = { Icon(Icons.Default.Edit, null) },
                isError = titleError, supportingText = { if (titleError) Text("Title is required") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
            )

            Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategory?.id == cat.id
                    val color = runCatching { Color(android.graphics.Color.parseColor(cat.color)) }.getOrDefault(IncomeGreen)
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { selectedCategory = cat }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(cat.icon, fontSize = 24.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(cat.name, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) color else MaterialTheme.colorScheme.onSurface)
                    }
                }
                item {
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { showCategoryDialog = true }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.height(4.dp))
                        Text("New", fontSize = 11.sp)
                    }
                }
            }

            OutlinedButton(onClick = { showDatePicker = true }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp)) {
                Icon(Icons.Default.CalendarMonth, null); Spacer(Modifier.width(8.dp))
                Text(SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(selectedDate)))
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Recurring Income", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Repeat every month", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Switch(checked = isRecurring, onCheckedChange = { isRecurring = it }, thumbContent = {
                    if (isRecurring) Icon(Icons.Default.Repeat, null, modifier = Modifier.size(14.dp))
                })
            }

            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("Note (optional)") }, leadingIcon = { Icon(Icons.Default.Notes, null) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), maxLines = 3
            )

            Button(
                onClick = {
                    titleError = title.isBlank(); amountError = amount.toDoubleOrNull() == null
                    if (!titleError && !amountError) {
                        val income = Income(
                            id = if (isEdit) incomeId else 0, title = title.trim(), amount = amount.toDouble(),
                            categoryId = selectedCategory?.id, categoryName = selectedCategory?.name ?: "Other Income",
                            categoryIcon = selectedCategory?.icon ?: "💰", categoryColor = selectedCategory?.color ?: "#1ABC9C",
                            date = selectedDate, note = note.trim(), isRecurring = isRecurring
                        )
                        scope.launch { if (isEdit) vm.updateIncome(income) else vm.insertIncome(income); onBack() }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IncomeGreen)
            ) {
                Icon(if (isEdit) Icons.Default.Save else Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEdit) "Update Income" else "Add Income", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = { TextButton(onClick = { state.selectedDateMillis?.let { selectedDate = it }; showDatePicker = false }) { Text("OK") } },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = state) }
    }

    if (showCategoryDialog) {
        AddCategoryDialog(type = CategoryType.INCOME, onDismiss = { showCategoryDialog = false }, onSave = { cat -> vm.insertCategory(cat); showCategoryDialog = false })
    }
}
