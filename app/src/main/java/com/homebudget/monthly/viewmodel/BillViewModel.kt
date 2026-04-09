package com.homebudget.monthly.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.homebudget.monthly.data.entities.Bill
import com.homebudget.monthly.data.repository.BudgetRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class BillViewModel(private val repository: BudgetRepository) : ViewModel() {

    val bills: StateFlow<List<Bill>> = repository
        .getAllBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unpaidBills: StateFlow<List<Bill>> = repository
        .getUnpaidBills()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalBills: StateFlow<Double> = repository
        .getTotalBills()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val totalUnpaid: StateFlow<Double> = repository
        .getTotalUnpaidBills()
        .map { it ?: 0.0 }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    fun insertBill(bill: Bill) = viewModelScope.launch { repository.insertBill(bill) }
    fun updateBill(bill: Bill) = viewModelScope.launch { repository.updateBill(bill) }
    fun deleteBill(bill: Bill) = viewModelScope.launch { repository.deleteBill(bill) }
    fun markBillPaid(id: Long, paid: Boolean) = viewModelScope.launch { repository.markBillPaid(id, paid) }
    suspend fun getBillById(id: Long) = repository.getBillById(id)
}

class BillViewModelFactory(private val repository: BudgetRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(BillViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST") return BillViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
