package com.homebudget.monthly.data.repository

import com.homebudget.monthly.data.dao.*
import com.homebudget.monthly.data.entities.*
import kotlinx.coroutines.flow.Flow
import java.util.Calendar

class BudgetRepository(
    private val categoryDao: CategoryDao,
    private val expenseDao: ExpenseDao,
    private val incomeDao: IncomeDao,
    private val billDao: BillDao,
    private val accountDao: AccountDao
) {
    // ---- Categories ----
    fun getCategoriesByType(type: CategoryType) = categoryDao.getCategoriesByType(type)
    fun getAllCategories() = categoryDao.getAllCategories()
    suspend fun insertCategory(category: Category) = categoryDao.insert(category)
    suspend fun updateCategory(category: Category) = categoryDao.update(category)
    suspend fun deleteCategory(category: Category) = categoryDao.delete(category)

    // ---- Expenses ----
    fun getAllExpenses() = expenseDao.getAllExpenses()
    fun getExpensesByDateRange(start: Long, end: Long) = expenseDao.getExpensesByDateRange(start, end)
    fun getTotalExpenseForPeriod(start: Long, end: Long) = expenseDao.getTotalExpenseForPeriod(start, end)
    suspend fun insertExpense(expense: Expense) = expenseDao.insert(expense)
    suspend fun updateExpense(expense: Expense) = expenseDao.update(expense)
    suspend fun deleteExpense(expense: Expense) = expenseDao.delete(expense)
    suspend fun getExpenseById(id: Long) = expenseDao.getById(id)
    suspend fun getExpensesByDateRangeSync(start: Long, end: Long) = expenseDao.getExpensesByDateRangeSync(start, end)
    suspend fun getTotalExpenseSync(start: Long, end: Long) = expenseDao.getTotalExpenseForPeriodSync(start, end)

    // ---- Incomes ----
    fun getAllIncomes() = incomeDao.getAllIncomes()
    fun getIncomesByDateRange(start: Long, end: Long) = incomeDao.getIncomesByDateRange(start, end)
    fun getTotalIncomeForPeriod(start: Long, end: Long) = incomeDao.getTotalIncomeForPeriod(start, end)
    suspend fun insertIncome(income: Income) = incomeDao.insert(income)
    suspend fun updateIncome(income: Income) = incomeDao.update(income)
    suspend fun deleteIncome(income: Income) = incomeDao.delete(income)
    suspend fun getIncomeById(id: Long) = incomeDao.getById(id)
    suspend fun getIncomesByDateRangeSync(start: Long, end: Long) = incomeDao.getIncomesByDateRangeSync(start, end)
    suspend fun getTotalIncomeSync(start: Long, end: Long) = incomeDao.getTotalIncomeForPeriodSync(start, end)

    // ---- Bills ----
    fun getAllBills() = billDao.getAllBills()
    fun getUnpaidBills() = billDao.getUnpaidBills()
    fun getTotalBills() = billDao.getTotalBills()
    fun getTotalUnpaidBills() = billDao.getTotalUnpaidBills()
    suspend fun insertBill(bill: Bill) = billDao.insert(bill)
    suspend fun updateBill(bill: Bill) = billDao.update(bill)
    suspend fun deleteBill(bill: Bill) = billDao.delete(bill)
    suspend fun getBillById(id: Long) = billDao.getById(id)
    suspend fun markBillPaid(id: Long, paid: Boolean) =
        billDao.updatePaidStatus(id, paid, if (paid) System.currentTimeMillis() else null)

    // ---- Accounts ----
    fun getAllAccounts() = accountDao.getAllAccounts()
    fun getTotalBalance() = accountDao.getTotalBalance()
    suspend fun insertAccount(account: Account) = accountDao.insert(account)
    suspend fun updateAccount(account: Account) = accountDao.update(account)
    suspend fun deleteAccount(account: Account) = accountDao.delete(account)

    // ---- Helpers ----
    fun currentMonthRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis
        return Pair(start, end)
    }

    fun currentWeekRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_WEEK, cal.firstDayOfWeek)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis
        cal.add(Calendar.DAY_OF_WEEK, 6)
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis
        return Pair(start, end)
    }
}
