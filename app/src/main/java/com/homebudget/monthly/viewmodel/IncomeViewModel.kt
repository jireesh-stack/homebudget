package com.homebudget.monthly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.homebudget.monthly.data.entities.Category
import com.homebudget.monthly.data.entities.CategoryType
import com.homebudget.monthly.data.entities.Income
import com.homebudget.monthly.data.repository.BudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class IncomeViewModel(private val repository: BudgetRepository) : ViewModel() {

    val incomes: StateFlow<List<Income>> = repository
        .getAllIncomes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val incomeCategories: StateFlow<List<Category>> = repository
        .getCategoriesByType(CategoryType.INCOME)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun insertIncome(income: Income) = viewModelScope.launch { repository.insertIncome(income) }
    fun updateIncome(income: Income) = viewModelScope.launch { repository.updateIncome(income) }
    fun deleteIncome(income: Income) = viewModelScope.launch { repository.deleteIncome(income) }
    fun insertCategory(category: Category) = viewModelScope.launch { repository.insertCategory(category) }
    fun updateCategory(category: Category) = viewModelScope.launch { repository.updateCategory(category) }
    fun deleteCategory(category: Category) = viewModelScope.launch { repository.deleteCategory(category) }
    suspend fun getIncomeById(id: Long) = repository.getIncomeById(id)
}

class IncomeViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(IncomeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return IncomeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
