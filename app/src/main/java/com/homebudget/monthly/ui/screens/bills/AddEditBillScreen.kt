package com.homebudget.monthly.ui.screens.bills

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homebudget.monthly.data.entities.Bill
import com.homebudget.monthly.data.repository.BudgetRepository
import com.homebudget.monthly.ui.screens.expense.BasicAmountField
import com.homebudget.monthly.ui.theme.BillOrange
import com.homebudget.monthly.viewmodel.BillViewModel
import com.homebudget.monthly.viewmodel.BillViewModelFactory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditBillScreen(
    repository: BudgetRepository,
    billId: Long = -1L,
    onBack: () -> Unit
) {
    val vm: BillViewModel = viewModel(factory = BillViewModelFactory(repository))
    val scope = rememberCoroutineScope()
    val isEdit = billId != -1L

    var name by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var dueDay by remember { mutableStateOf("1") }
    var reminderEmail by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var reminderEnabled by remember { mutableStateOf(true) }
    var isRecurring by remember { mutableStateOf(true) }
    var selectedIcon by remember { mutableStateOf("📋") }
    var selectedCategory by remember { mutableStateOf("Bills") }
    var selectedColor by remember { mutableStateOf("#F39C12") }
    var nameError by remember { mutableStateOf(false) }
    var amountError by remember { mutableStateOf(false) }

    val billCategories = listOf(
        Triple("📋", "Bills", "#F39C12"), Triple("🏠", "Rent", "#BB8FCE"),
        Triple("💡", "Utilities", "#DDA0DD"), Triple("📱", "Phone", "#45B7D1"),
        Triple("🌐", "Internet", "#4ECDC4"), Triple("🏥", "Insurance", "#FFEAA7"),
        Triple("🚗", "Car", "#96CEB4"), Triple("📺", "Streaming", "#FF6B6B"),
        Triple("🏦", "Loan", "#AEB6BF"), Triple("💳", "Credit", "#98D8C8")
    )

    LaunchedEffect(billId) {
        if (isEdit) {
            vm.getBillById(billId)?.let {
                name = it.name; amount = it.amount.toString(); dueDay = it.dueDay.toString()
                reminderEmail = it.reminderEmail; note = it.note
                reminderEnabled = it.reminderEnabled; isRecurring = it.isRecurring
                selectedIcon = it.categoryIcon; selectedCategory = it.categoryName; selectedColor = it.categoryColor
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isEdit) "Edit Bill" else "Add Bill", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    TextButton(onClick = {
                        nameError = name.isBlank(); amountError = amount.toDoubleOrNull() == null
                        if (!nameError && !amountError) {
                            val bill = Bill(
                                id = if (isEdit) billId else 0, name = name.trim(),
                                amount = amount.toDouble(), dueDay = dueDay.toIntOrNull()?.coerceIn(1, 31) ?: 1,
                                categoryName = selectedCategory, categoryIcon = selectedIcon, categoryColor = selectedColor,
                                reminderEnabled = reminderEnabled, reminderEmail = reminderEmail.trim(),
                                note = note.trim(), isRecurring = isRecurring
                            )
                            scope.launch { if (isEdit) vm.updateBill(bill) else vm.insertBill(bill); onBack() }
                        }
                    }) { Text("Save", color = BillOrange, fontWeight = FontWeight.SemiBold) }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)
                .padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Amount
            Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = BillOrange.copy(alpha = 0.05f))) {
                Column(modifier = Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Bill Amount", style = MaterialTheme.typography.labelLarge, color = BillOrange.copy(alpha = 0.7f))
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("$", fontSize = 28.sp, color = BillOrange, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.width(4.dp))
                        BasicAmountField(value = amount, onValueChange = { amount = it; amountError = false }, isError = amountError)
                    }
                    if (amountError) Text("Enter a valid amount", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }
            }

            OutlinedTextField(
                value = name, onValueChange = { name = it; nameError = false },
                label = { Text("Bill Name *") }, leadingIcon = { Icon(Icons.Default.Receipt, null) },
                isError = nameError, supportingText = { if (nameError) Text("Name is required") },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
            )

            // Category
            Text("Category", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(billCategories) { (icon, catName, color) ->
                    val isSelected = selectedCategory == catName
                    val catColor = runCatching { Color(android.graphics.Color.parseColor(color)) }.getOrDefault(BillOrange)
                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) catColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface)
                            .border(if (isSelected) 2.dp else 1.dp, if (isSelected) catColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .clickable { selectedIcon = icon; selectedCategory = catName; selectedColor = color }
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(icon, fontSize = 24.sp)
                        Spacer(Modifier.height(4.dp))
                        Text(catName, fontSize = 11.sp, fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal, color = if (isSelected) catColor else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }

            // Due Day
            OutlinedTextField(
                value = dueDay, onValueChange = { if (it.length <= 2 && it.all { c -> c.isDigit() }) dueDay = it },
                label = { Text("Due Day (1-31)") }, leadingIcon = { Icon(Icons.Default.CalendarMonth, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
            )

            // Email reminder
            OutlinedTextField(
                value = reminderEmail, onValueChange = { reminderEmail = it },
                label = { Text("Reminder Email") }, leadingIcon = { Icon(Icons.Default.Email, null) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), singleLine = true
            )

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Enable Reminder", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Get notified before due date", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Switch(checked = reminderEnabled, onCheckedChange = { reminderEnabled = it })
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Recurring", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Text("Repeat every month", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                }
                Switch(checked = isRecurring, onCheckedChange = { isRecurring = it })
            }

            OutlinedTextField(
                value = note, onValueChange = { note = it },
                label = { Text("Note (optional)") }, leadingIcon = { Icon(Icons.Default.Notes, null) },
                modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), maxLines = 3
            )

            Button(
                onClick = {
                    nameError = name.isBlank(); amountError = amount.toDoubleOrNull() == null
                    if (!nameError && !amountError) {
                        val bill = Bill(
                            id = if (isEdit) billId else 0, name = name.trim(),
                            amount = amount.toDouble(), dueDay = dueDay.toIntOrNull()?.coerceIn(1, 31) ?: 1,
                            categoryName = selectedCategory, categoryIcon = selectedIcon, categoryColor = selectedColor,
                            reminderEnabled = reminderEnabled, reminderEmail = reminderEmail.trim(),
                            note = note.trim(), isRecurring = isRecurring
                        )
                        scope.launch { if (isEdit) vm.updateBill(bill) else vm.insertBill(bill); onBack() }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BillOrange)
            ) {
                Icon(if (isEdit) Icons.Default.Save else Icons.Default.Add, null)
                Spacer(Modifier.width(8.dp))
                Text(if (isEdit) "Update Bill" else "Add Bill", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
            }
        }
    }
}
