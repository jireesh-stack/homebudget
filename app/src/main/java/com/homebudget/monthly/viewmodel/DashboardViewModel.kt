package com.homebudget.monthly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.homebudget.monthly.data.entities.Expense
import com.homebudget.monthly.data.entities.Income
import com.homebudget.monthly.data.repository.BudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class DashboardViewModel(private val repository: BudgetRepository) : ViewModel() {

    private val monthRange = repository.currentMonthRange()
    private val weekRange = repository.currentWeekRange()

    val monthlyExpense: StateFlow<Double> = repository
        .getTotalExpenseForPeriod(monthRange.first, monthRange.second)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val monthlyIncome: StateFlow<Double> = repository
        .getTotalIncomeForPeriod(monthRange.first, monthRange.second)
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalBalance: StateFlow<Double> = repository
        .getTotalBalance()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalUnpaidBills: StateFlow<Double> = repository
        .getTotalUnpaidBills()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val recentExpenses: StateFlow<List<Expense>> = repository
        .getAllExpenses()
        .map { it.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentIncomes: StateFlow<List<Income>> = repository
        .getAllIncomes()
        .map { it.take(10) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyExpenses: StateFlow<List<Expense>> = repository
        .getExpensesByDateRange(weekRange.first, weekRange.second)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val weeklyIncomes: StateFlow<List<Income>> = repository
        .getIncomesByDateRange(weekRange.first, weekRange.second)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // category name -> (category color, total amount)
    val expensesByCategory: StateFlow<Map<Pair<String, String>, Double>> = repository
        .getExpensesByDateRange(monthRange.first, monthRange.second)
        .map { expenses ->
            expenses.groupBy { Pair(it.categoryName, it.categoryColor) }
                .mapValues { entry -> entry.value.sumOf { it.amount } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())
}

class DashboardViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DashboardViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return DashboardViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
