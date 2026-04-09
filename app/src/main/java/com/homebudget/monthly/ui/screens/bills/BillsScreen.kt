package com.homebudget.monthly.ui.screens.bills

import android.content.Intent
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.homebudget.monthly.data.entities.Bill
import com.homebudget.monthly.data.repository.BudgetRepository
import com.homebudget.monthly.ui.theme.BillOrange
import com.homebudget.monthly.ui.theme.ExpenseRed
import com.homebudget.monthly.ui.theme.IncomeGreen
import com.homebudget.monthly.viewmodel.BillViewModel
import com.homebudget.monthly.viewmodel.BillViewModelFactory
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillsScreen(
    repository: BudgetRepository,
    onAddBill: () -> Unit,
    onEditBill: (Long) -> Unit,
    onBack: () -> Unit
) {
    val vm: BillViewModel = viewModel(factory = BillViewModelFactory(repository))
    val bills by vm.bills.collectAsState()
    val totalBills by vm.totalBills.collectAsState()
    val totalUnpaid by vm.totalUnpaid.collectAsState()
    val fmt = NumberFormat.getCurrencyInstance()
    var selectedTab by remember { mutableStateOf(0) }
    var showDeleteDialog by remember { mutableStateOf<Bill?>(null) }
    val context = LocalContext.current

    val displayed = when (selectedTab) {
        0 -> bills
        else -> bills.filter { !it.isPaid }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Bills", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = { IconButton(onClick = onAddBill) { Icon(Icons.Default.Add, "Add") } }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddBill, containerColor = BillOrange, contentColor = Color.White, shape = CircleShape) {
                Icon(Icons.Default.Add, "Add Bill")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding)
        ) {
            // Summary
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BillSummaryCard("Total Bills", fmt.format(totalBills), BillOrange, Modifier.weight(1f))
                BillSummaryCard("Unpaid", fmt.format(totalUnpaid), ExpenseRed, Modifier.weight(1f))
                BillSummaryCard("Paid", fmt.format(totalBills - totalUnpaid), IncomeGreen, Modifier.weight(1f))
            }

            // Tabs
            TabRow(selectedTabIndex = selectedTab, modifier = Modifier.padding(horizontal = 16.dp)) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("All Bills") })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Unpaid") })
            }

            Spacer(Modifier.height(8.dp))

            LazyColumn(contentPadding = PaddingValues(bottom = 80.dp)) {
                items(displayed, key = { it.id }) { bill ->
                    BillItem(
                        bill = bill,
                        fmt = fmt,
                        onEdit = { onEditBill(bill.id) },
                        onDelete = { showDeleteDialog = bill },
                        onTogglePaid = { vm.markBillPaid(bill.id, !bill.isPaid) },
                        onEmailBill = {
                            if (bill.reminderEmail.isNotEmpty()) {
                                val intent = Intent(Intent.ACTION_SEND).apply {
                                    type = "message/rfc822"
                                    putExtra(Intent.EXTRA_EMAIL, arrayOf(bill.reminderEmail))
                                    putExtra(Intent.EXTRA_SUBJECT, "Bill Reminder: ${bill.name}")
                                    putExtra(Intent.EXTRA_TEXT, buildBillEmailBody(bill, fmt))
                                }
                                context.startActivity(Intent.createChooser(intent, "Send bill reminder"))
                            }
                        }
                    )
                }
                if (displayed.isEmpty()) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth().padding(48.dp), contentAlignment = Alignment.Center) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📋", fontSize = 48.sp)
                                Spacer(Modifier.height(16.dp))
                                Text("No bills yet", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                Spacer(Modifier.height(8.dp))
                                Button(onClick = onAddBill) { Text("Add Bill") }
                            }
                        }
                    }
                }
            }
        }
    }

    showDeleteDialog?.let { bill ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Bill") },
            text = { Text("Are you sure you want to delete \"${bill.name}\"?") },
            confirmButton = {
                Button(onClick = { vm.deleteBill(bill); showDeleteDialog = null }, colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)) { Text("Delete") }
            },
            dismissButton = { TextButton(onClick = { showDeleteDialog = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun BillSummaryCard(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = color)
            Text(value, fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun BillItem(
    bill: Bill, fmt: NumberFormat,
    onEdit: () -> Unit, onDelete: () -> Unit,
    onTogglePaid: () -> Unit, onEmailBill: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val color = runCatching { Color(android.graphics.Color.parseColor(bill.categoryColor)) }.getOrDefault(BillOrange)
    val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
    val isOverdue = !bill.isPaid && bill.dueDay < currentDay

    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier.size(48.dp).clip(CircleShape)
                    .background(if (bill.isPaid) IncomeGreen.copy(alpha = 0.15f) else color.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) { Text(bill.categoryIcon, fontSize = 22.sp) }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(bill.name, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge)
                    if (isOverdue) {
                        Spacer(Modifier.width(6.dp))
                        Surface(shape = RoundedCornerShape(4.dp), color = ExpenseRed.copy(alpha = 0.15f)) {
                            Text("OVERDUE", modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = ExpenseRed)
                        }
                    }
                }
                Spacer(Modifier.height(2.dp))
                Row {
                    Surface(shape = RoundedCornerShape(6.dp), color = color.copy(alpha = 0.15f)) {
                        Text(bill.categoryName, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = color)
                    }
                    Spacer(Modifier.width(8.dp))
                    Text("Due: Day ${bill.dueDay}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f))
                }
                if (bill.note.isNotEmpty()) Text(bill.note, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f), maxLines = 1)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(fmt.format(bill.amount), fontWeight = FontWeight.Bold, color = if (bill.isPaid) IncomeGreen else ExpenseRed, style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = bill.isPaid, onCheckedChange = { onTogglePaid() }, modifier = Modifier.size(20.dp))
                    Text(if (bill.isPaid) "Paid" else "Unpaid", style = MaterialTheme.typography.labelSmall, color = if (bill.isPaid) IncomeGreen else ExpenseRed)
                    Spacer(Modifier.width(4.dp))
                    Box {
                        IconButton(onClick = { showMenu = true }, modifier = Modifier.size(20.dp)) {
                            Icon(Icons.Default.MoreVert, null, modifier = Modifier.size(14.dp))
                        }
                        DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                            DropdownMenuItem(text = { Text("Edit") }, leadingIcon = { Icon(Icons.Default.Edit, null) }, onClick = { showMenu = false; onEdit() })
                            DropdownMenuItem(text = { Text("Email Bill") }, leadingIcon = { Icon(Icons.Default.Email, null) }, onClick = { showMenu = false; onEmailBill() })
                            DropdownMenuItem(text = { Text("Delete", color = ExpenseRed) }, leadingIcon = { Icon(Icons.Default.Delete, null, tint = ExpenseRed) }, onClick = { showMenu = false; onDelete() })
                        }
                    }
                }
            }
        }
    }
}

private fun buildBillEmailBody(bill: Bill, fmt: NumberFormat): String {
    return """
        Bill Reminder

        Bill: ${bill.name}
        Amount: ${fmt.format(bill.amount)}
        Due: Day ${bill.dueDay} of every month
        Category: ${bill.categoryName}
        ${if (bill.note.isNotEmpty()) "Note: ${bill.note}" else ""}

        Please ensure payment is made before the due date.

        Sent from HomeBudget Monthly
    """.trimIndent()
}
