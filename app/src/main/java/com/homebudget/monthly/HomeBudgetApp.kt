package com.homebudget.monthly

import android.app.Application
import com.homebudget.monthly.data.AppDatabase
import com.homebudget.monthly.data.repository.BudgetRepository

class HomeBudgetApp : Application() {
    val database by lazy { AppDatabase.getDatabase(this) }
    val repository by lazy {
        BudgetRepository(
            database.categoryDao(),
            database.expenseDao(),
            database.incomeDao(),
            database.billDao(),
            database.accountDao()
        )
    }
}
