package com.homebudget.monthly.ui.screens.expense

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homebudget.monthly.data.entities.Category
import com.homebudget.monthly.data.entities.CategoryType
import com.homebudget.monthly.data.entities.Expense
import com.homebudget.monthly.data.repository.BudgetRepository
import com.homebudget.monthly.ui.theme.ExpenseRed
import com.homebudget.monthly.viewmodel.ExpenseViewModel
import com.homebudget.monthly.viewmodel.ExpenseViewModelFactory
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExpenseScreen(
    repository: BudgetRepository,
    expenseId: Long = -1L,
    onBack: () -> Unit
) {
    val vm: ExpenseViewModel = viewModel(factory = ExpenseViewModelFactory(repository))
    val categories by vm.expenseCategories.collectAsState()
    val scope = rememberCoroutineScope()
    val isEdit = expenseId != -1L

    var title by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    var selectedDate by remember { mutableStateOf(System.currentTimeMillis()) }
    var showDatePicker by remember { mutableStateOf(false) }
    var showCategoryDialog by remember { mutableStateOf(false) }
    var titleError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    // Load for edit
    LaunchedEffect(expenseId, categories) {
        if (isEdit && categories.isNotEmpty()) {
            val expense = vm.getExpenseById(expenseId)
            expense?.let {
                title = it.title
                amount = it.amount.toString()
                note = it.note
                selectedDate = it.date
                selectedCategory = categories.find { c -> c.id == it.categoryId }
                    ?: Category(id = -1, name = it.categoryName, icon = it.categoryIcon, color = it.categoryColor, type = CategoryType.EXPENSE)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Expense" else "Add Expense", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") }
                },
                actions = {
                    TextButton(onClick = {
                        titleError = title.isBlank()
                        amountError = amount.toDoubleOrNull() == null
                        if (!titleError && !amountError) {
                            val expense = Expense(
                                id = if (isEdit) expenseId else 0,
                                title = title.trim(),
                                amount = amount.toDouble(),
                                categoryId = selectedCategory?.id,
                                categoryName = selectedCategory?.name ?: "Others",
                                categoryIcon = selectedCategory?.icon ?: "📦",
                                categoryColor = selectedCategory?.color ?: "#AEB6BF",
                                date = selectedDate,
                                note = note.trim()
                            )
                            scope.launch {
                                if (isEdit) vm.updateExpense(expense) else vm.insertExpense(expense)
                                onBack()
                            }
                        }
                    }) {
                        Text("Save", color = ExpenseRed, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount input (large)
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = ExpenseRed.copy(alpha = 0.05f)),
                elevation = CardDefaults.cardElevation(0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Amount", style = MaterialTheme.typography.labelLarge, color = ExpenseRed.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$", fontSize = 28.sp, color = ExpenseRed, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        BasicAmountField(
                            value = amount,
                            onValueChange = { amount = it; amountError = false },
                            isError = amountError
                        )
                    }
                    if (amountError) {
                        Text("Enter a valid amount", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                    }
                }
            }

            // Title
            OutlinedTextField(
                value = title,
                onValueChange = { title = it; titleError = false },
                label = { Text("Expense Title *") },
                leadingIcon = { Icon(Icons.Default.Edit, null) },
                isError = titleError,
                supportingText = { if (titleError) Text("Title is required") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            // Category
            Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(categories) { cat ->
                    val isSelected = selectedCategory?.id == cat.id
                    val color = runCatching { Color(android.graphics.Color.parseColor(cat.color)) }.getOrDefault(ExpenseRed)
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) color else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(12.dp)
                            )
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

            // Date
            OutlinedButton(
                onClick = { showDatePicker = true },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.CalendarMonth, null)
                Spacer(Modifier.width(8.dp))
                Text(SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date(selectedDate)))
            }

            // Note
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (optional)") },
                leadingIcon = { Icon(Icons.Default.Notes, null) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                maxLines = 3
            )

            // Save Button
            Button(
                onClick = {
                    titleError = title.isBlank()
                    amountError = amount.toDoubleOrNull() == null
                    if (!titleError && !amountError) {
                        val expense = Expense(
                            id = if (isEdit) expenseId else 0,
                            title = title.trim(),
                            amount = amount.toDouble(),
                            categoryId = selectedCategory?.id,
                            categoryName = selectedCategory?.name ?: "Others",
                            categoryIcon = selectedCategory?.icon ?: "📦",
                            categoryColor = selectedCategory?.color ?: "#AEB6BF",
                            date = selectedDate,
                            note = note.trim()
                        )
                        scope.launch {
                            if (isEdit) vm.updateExpense(expense) else vm.insertExpense(expense)
                            onBack()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed)
            ) {
                Icon(if (isEdit) Icons.Default.Save else Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEdit) "Update Expense" else "Add Expense", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }

    if (showDatePicker) {
        val state = rememberDatePickerState(initialSelectedDateMillis = selectedDate)
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    state.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = state)
        }
    }

    if (showCategoryDialog) {
        AddCategoryDialog(
            type = CategoryType.EXPENSE,
            onDismiss = { showCategoryDialog = false },
            onSave = { cat ->
                vm.insertCategory(cat)
                showCategoryDialog = false
            }
        )
    }
}

@Composable
fun BasicAmountField(value: String, onValueChange: (String) -> Unit, isError: Boolean) {
    BasicTextField(
        value = value,
        onValueChange = { new ->
            if (new.isEmpty() || new.matches(Regex("^\\d*\\.?\\d{0,2}$"))) onValueChange(new)
        },
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
        textStyle = androidx.compose.ui.text.TextStyle(
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            color = if (isError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
        ),
        decorationBox = { inner ->
            if (value.isEmpty()) {
                Text("0.00", fontSize = 36.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f), fontWeight = FontWeight.Bold)
            }
            inner()
        }
    )
}

private val BasicTextField = androidx.compose.foundation.text.BasicTextField

val categoryIcons = listOf("🍔","🚗","🛍️","🎮","💊","💡","📚","✈️","🏠","📦","💼","💻","📈","🏢","🎁","💰","🎵","🏋️","🌿","🐾","🎨","🔧","📱","🎓","🏥","🏪")
val categoryColors = listOf("#FF6B6B","#4ECDC4","#45B7D1","#96CEB4","#FFEAA7","#DDA0DD","#98D8C8","#F7DC6F","#BB8FCE","#AEB6BF","#2ECC71","#27AE60","#F39C12","#E74C3C","#9B59B6","#1ABC9C","#3498DB","#E67E22","#16A085","#8E44AD")

@Composable
fun AddCategoryDialog(type: CategoryType, onDismiss: () -> Unit, onSave: (Category) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedIcon by remember { mutableStateOf("📦") }
    var selectedColor by remember { mutableStateOf("#AEB6BF") }

    Dialog(onDismissRequest = onDismiss) {
        Card(shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("New Category", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Category Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
                Text("Icon", style = MaterialTheme.typography.labelLarge)
                LazyVerticalGrid(
                    columns = GridCells.Fixed(6),
                    modifier = Modifier.height(160.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(categoryIcons) { icon ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(if (selectedIcon == icon) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface)
                                .border(
                                    if (selectedIcon == icon) 2.dp else 0.dp,
                                    MaterialTheme.colorScheme.primary,
                                    CircleShape
                                )
                                .clickable { selectedIcon = icon },
                            contentAlignment = Alignment.Center
                        ) { Text(icon, fontSize = 20.sp) }
                    }
                }
                Text("Color", style = MaterialTheme.typography.labelLarge)
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryColors) { hex ->
                        val color = runCatching { Color(android.graphics.Color.parseColor(hex)) }.getOrDefault(Color.Gray)
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(color)
                                .border(
                                    if (selectedColor == hex) 3.dp else 0.dp,
                                    MaterialTheme.colorScheme.onSurface,
                                    CircleShape
                                )
                                .clickable { selectedColor = hex }
                        )
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (name.isNotBlank()) {
                                onSave(Category(name = name.trim(), icon = selectedIcon, color = selectedColor, type = type))
                            }
                        }
                    ) { Text("Add") }
                }
            }
        }
    }
}
