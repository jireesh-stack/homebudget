package com.homebudget.monthly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.homebudget.monthly.data.entities.Category
import com.homebudget.monthly.data.entities.CategoryType
import com.homebudget.monthly.data.entities.Expense
import com.homebudget.monthly.data.repository.BudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: BudgetRepository) : ViewModel() {

    val expenses: StateFlow<List<Expense>> = repository
        .getAllExpenses()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val expenseCategories: StateFlow<List<Category>> = repository
        .getCategoriesByType(CategoryType.EXPENSE)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertExpense(expense: Expense) = viewModelScope.launch { repository.insertExpense(expense) }
    fun updateExpense(expense: Expense) = viewModelScope.launch { repository.updateExpense(expense) }
    fun deleteExpense(expense: Expense) = viewModelScope.launch { repository.deleteExpense(expense) }
    fun insertCategory(category: Category) = viewModelScope.launch { repository.insertCategory(category) }
    fun updateCategory(category: Category) = viewModelScope.launch { repository.updateCategory(category) }
    fun deleteCategory(category: Category) = viewModelScope.launch { repository.deleteCategory(category) }
    suspend fun getExpenseById(id: Long) = repository.getExpenseById(id)
}

class ExpenseViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ExpenseViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return ExpenseViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
