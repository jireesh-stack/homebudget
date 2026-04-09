package com.homebudget.monthly.ui.screens.reports

import android.content.Context
import android.content.Intent
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import com.homebudget.monthly.data.entities.Expense
import com.homebudget.monthly.data.entities.Income
import com.homebudget.monthly.data.repository.BudgetRepository
import com.homebudget.monthly.ui.components.BarChart
import com.homebudget.monthly.ui.components.ChartSegment
import com.homebudget.monthly.ui.components.DonutChart
import com.homebudget.monthly.ui.theme.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    repository: BudgetRepository,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val fmt = NumberFormat.getCurrencyInstance()

    var selectedPeriod by remember { mutableStateOf(0) } // 0=Monthly 1=Weekly 2=Yearly
    var expenses by remember { mutableStateOf<List<Expense>>(emptyList()) }
    var incomes by remember { mutableStateOf<List<Income>>(emptyList()) }
    var isExporting by remember { mutableStateOf(false) }

    fun loadData() {
        scope.launch {
            val (start, end) = when (selectedPeriod) {
                1 -> repository.currentWeekRange()
                2 -> {
                    val cal = Calendar.getInstance()
                    cal.set(Calendar.DAY_OF_YEAR, 1)
                    cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
                    val s = cal.timeInMillis
                    cal.set(Calendar.DAY_OF_YEAR, cal.getActualMaximum(Calendar.DAY_OF_YEAR))
                    cal.set(Calendar.HOUR_OF_DAY, 23); cal.set(Calendar.MINUTE, 59)
                    Pair(s, cal.timeInMillis)
                }
                else -> repository.currentMonthRange()
            }
            expenses = repository.getExpensesByDateRangeSync(start, end)
            incomes = repository.getIncomesByDateRangeSync(start, end)
        }
    }

    LaunchedEffect(selectedPeriod) { loadData() }

    val totalExpense = expenses.sumOf { it.amount }
    val totalIncome = incomes.sumOf { it.amount }
    val netBalance = totalIncome - totalExpense

    val expenseByCategory = expenses.groupBy { it.categoryName }
        .mapValues { it.value.sumOf { e -> e.amount } }
        .entries.sortedByDescending { it.value }

    val weeklyBarData = buildWeeklyBarData(expenses)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reports", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, "Back") } },
                actions = {
                    if (isExporting) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    } else {
                        IconButton(onClick = {
                            isExporting = true
                            scope.launch {
                                exportToPdf(context, expenses, incomes, fmt, selectedPeriod)
                                isExporting = false
                            }
                        }) {
                            Icon(Icons.Default.Download, "Export PDF")
                        }
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(padding),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // Period tabs
            item {
                TabRow(selectedTabIndex = selectedPeriod, modifier = Modifier.padding(horizontal = 16.dp)) {
                    Tab(selected = selectedPeriod == 0, onClick = { selectedPeriod = 0 }, text = { Text("Monthly") })
                    Tab(selected = selectedPeriod == 1, onClick = { selectedPeriod = 1 }, text = { Text("Weekly") })
                    Tab(selected = selectedPeriod == 2, onClick = { selectedPeriod = 2 }, text = { Text("Yearly") })
                }
            }

            // Summary Cards
            item {
                Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    ReportStatCard("Income", fmt.format(totalIncome), IncomeGreen, "📈", Modifier.weight(1f))
                    ReportStatCard("Expenses", fmt.format(totalExpense), ExpenseRed, "📉", Modifier.weight(1f))
                }
                Spacer(Modifier.height(12.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (netBalance >= 0) IncomeGreen.copy(alpha = 0.1f) else ExpenseRed.copy(alpha = 0.1f)
                    )
                ) {
                    Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
                        Column {
                            Text("Net Balance", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                            Text(fmt.format(netBalance), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = if (netBalance >= 0) IncomeGreen else ExpenseRed)
                        }
                        Text(if (netBalance >= 0) "💚" else "❤️", fontSize = 36.sp)
                    }
                }
            }

            // Expense Donut Chart
            if (expenseByCategory.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Spending by Category", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(16.dp))
                            val segments = expenseByCategory.take(6).map { (name, amount) ->
                                val expense = expenses.find { it.categoryName == name }
                                val color = runCatching { Color(android.graphics.Color.parseColor(expense?.categoryColor ?: "#AEB6BF")) }.getOrDefault(Color.Gray)
                                ChartSegment(name, amount.toFloat(), color)
                            }
                            DonutChart(segments = segments, centerLabel = fmt.format(totalExpense), centerSubLabel = "Total Spent", modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            // Weekly Expense Bar Chart
            if (weeklyBarData.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Daily Spending (This Week)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(16.dp))
                            BarChart(bars = weeklyBarData, barColor = ExpenseRed, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            // Category Breakdown List
            if (expenseByCategory.isNotEmpty()) {
                item {
                    Card(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp), shape = RoundedCornerShape(20.dp), elevation = CardDefaults.cardElevation(4.dp)) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Category Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(12.dp))
                            expenseByCategory.forEach { (catName, catAmount) ->
                                val expense = expenses.find { it.categoryName == catName }
                                val pct = if (totalExpense > 0) (catAmount / totalExpense * 100).toInt() else 0
                                val color = runCatching { Color(android.graphics.Color.parseColor(expense?.categoryColor ?: "#AEB6BF")) }.getOrDefault(Color.Gray)
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(expense?.categoryIcon ?: "📦", fontSize = 18.sp)
                                    Spacer(Modifier.width(10.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                            Text(catName, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                            Text("$pct%", style = MaterialTheme.typography.bodyMedium, color = color)
                                        }
                                        Spacer(Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { pct / 100f },
                                            modifier = Modifier.fillMaxWidth().height(6.dp),
                                            color = color,
                                            trackColor = color.copy(alpha = 0.15f)
                                        )
                                        Text(fmt.format(catAmount), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Export Button
            item {
                Button(
                    onClick = {
                        isExporting = true
                        scope.launch {
                            exportToPdf(context, expenses, incomes, fmt, selectedPeriod)
                            isExporting = false
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp).height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    enabled = !isExporting
                ) {
                    Icon(Icons.Default.Download, null)
                    Spacer(Modifier.width(8.dp))
                    Text("Download Report (PDF)", fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ReportStatCard(label: String, value: String, color: Color, emoji: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f))) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(emoji, fontSize = 24.sp)
            Spacer(Modifier.height(8.dp))
            Text(label, style = MaterialTheme.typography.labelMedium, color = color)
            Text(value, fontWeight = FontWeight.Bold, color = color, style = MaterialTheme.typography.titleMedium)
        }
    }
}

private fun buildWeeklyBarData(expenses: List<Expense>): List<Pair<String, Float>> {
    val cal = Calendar.getInstance()
    val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    return days.mapIndexed { index, day ->
        val dayTotal = expenses.filter {
            val c = Calendar.getInstance().apply { timeInMillis = it.date }
            c.get(Calendar.DAY_OF_WEEK) == (index + Calendar.MONDAY) % 7 + 1
        }.sumOf { it.amount }.toFloat()
        Pair(day, dayTotal)
    }
}

private suspend fun exportToPdf(
    context: Context,
    expenses: List<Expense>,
    incomes: List<Income>,
    fmt: NumberFormat,
    period: Int
) = withContext(Dispatchers.IO) {
    try {
        val periodLabel = when (period) { 1 -> "Weekly"; 2 -> "Yearly"; else -> "Monthly" }
        val doc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = doc.startPage(pageInfo)
        val canvas = page.canvas
        val paint = Paint().apply { textSize = 12f; isAntiAlias = true }

        var y = 50f

        // Title
        paint.textSize = 24f; paint.isFakeBoldText = true; paint.color = android.graphics.Color.parseColor("#6C63FF")
        canvas.drawText("HomeBudget Monthly - $periodLabel Report", 40f, y, paint); y += 30f
        paint.textSize = 12f; paint.isFakeBoldText = false; paint.color = android.graphics.Color.GRAY
        canvas.drawText(SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(Date()), 40f, y, paint); y += 40f

        // Summary
        paint.textSize = 16f; paint.isFakeBoldText = true; paint.color = android.graphics.Color.BLACK
        canvas.drawText("Summary", 40f, y, paint); y += 25f
        paint.textSize = 12f; paint.isFakeBoldText = false
        val totalInc = incomes.sumOf { it.amount }
        val totalExp = expenses.sumOf { it.amount }
        paint.color = android.graphics.Color.parseColor("#2ECC71")
        canvas.drawText("Total Income: ${fmt.format(totalInc)}", 40f, y, paint); y += 20f
        paint.color = android.graphics.Color.parseColor("#E74C3C")
        canvas.drawText("Total Expenses: ${fmt.format(totalExp)}", 40f, y, paint); y += 20f
        paint.color = if (totalInc >= totalExp) android.graphics.Color.parseColor("#2ECC71") else android.graphics.Color.parseColor("#E74C3C")
        canvas.drawText("Net Balance: ${fmt.format(totalInc - totalExp)}", 40f, y, paint); y += 40f

        // Expenses
        paint.color = android.graphics.Color.BLACK; paint.textSize = 16f; paint.isFakeBoldText = true
        canvas.drawText("Expenses", 40f, y, paint); y += 25f
        paint.textSize = 11f; paint.isFakeBoldText = false
        expenses.take(20).forEach { exp ->
            if (y > 780f) return@forEach
            canvas.drawText("• ${exp.title} (${exp.categoryName}) - ${fmt.format(exp.amount)}", 50f, y, paint); y += 18f
        }
        y += 20f

        // Income
        paint.textSize = 16f; paint.isFakeBoldText = true
        canvas.drawText("Income", 40f, y, paint); y += 25f
        paint.textSize = 11f; paint.isFakeBoldText = false
        incomes.take(10).forEach { inc ->
            if (y > 780f) return@forEach
            canvas.drawText("• ${inc.title} (${inc.categoryName}) - ${fmt.format(inc.amount)}", 50f, y, paint); y += 18f
        }

        doc.finishPage(page)

        val fileName = "HomeBudget_${periodLabel}_${SimpleDateFormat("yyyyMMdd", Locale.getDefault()).format(Date())}.pdf"
        val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        downloadsDir.mkdirs()
        val file = File(downloadsDir, fileName)
        doc.writeTo(FileOutputStream(file))
        doc.close()

        withContext(Dispatchers.Main) {
            val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, "Open Report"))
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
}
