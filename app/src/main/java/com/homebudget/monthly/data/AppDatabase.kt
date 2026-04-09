package com.homebudget.monthly.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.homebudget.monthly.data.dao.*
import com.homebudget.monthly.data.entities.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [Category::class, Expense::class, Income::class, Bill::class, Account::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun categoryDao(): CategoryDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun incomeDao(): IncomeDao
    abstract fun billDao(): BillDao
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "homebudget_db"
                ).addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        INSTANCE?.let { database ->
                            CoroutineScope(Dispatchers.IO).launch {
                                seedDefaultData(database)
                            }
                        }
                    }
                }).build()
                INSTANCE = instance
                instance
            }
        }

        private suspend fun seedDefaultData(db: AppDatabase) {
            // Default Expense Categories
            val expenseCategories = listOf(
                Category(name = "Food & Dining", icon = "🍔", color = "#FF6B6B", type = CategoryType.EXPENSE),
                Category(name = "Transport", icon = "🚗", color = "#4ECDC4", type = CategoryType.EXPENSE),
                Category(name = "Shopping", icon = "🛍️", color = "#45B7D1", type = CategoryType.EXPENSE),
                Category(name = "Entertainment", icon = "🎮", color = "#96CEB4", type = CategoryType.EXPENSE),
                Category(name = "Health", icon = "💊", color = "#FFEAA7", type = CategoryType.EXPENSE),
                Category(name = "Utilities", icon = "💡", color = "#DDA0DD", type = CategoryType.EXPENSE),
                Category(name = "Education", icon = "📚", color = "#98D8C8", type = CategoryType.EXPENSE),
                Category(name = "Travel", icon = "✈️", color = "#F7DC6F", type = CategoryType.EXPENSE),
                Category(name = "Housing", icon = "🏠", color = "#BB8FCE", type = CategoryType.EXPENSE),
                Category(name = "Others", icon = "📦", color = "#AEB6BF", type = CategoryType.EXPENSE)
            )

            // Default Income Categories
            val incomeCategories = listOf(
                Category(name = "Salary", icon = "💼", color = "#2ECC71", type = CategoryType.INCOME),
                Category(name = "Freelance", icon = "💻", color = "#27AE60", type = CategoryType.INCOME),
                Category(name = "Investment", icon = "📈", color = "#F39C12", type = CategoryType.INCOME),
                Category(name = "Business", icon = "🏢", color = "#E74C3C", type = CategoryType.INCOME),
                Category(name = "Gift", icon = "🎁", color = "#9B59B6", type = CategoryType.INCOME),
                Category(name = "Other Income", icon = "💰", color = "#1ABC9C", type = CategoryType.INCOME)
            )

            (expenseCategories + incomeCategories).forEach { db.categoryDao().insert(it) }

            // Default Accounts
            val accounts = listOf(
                Account(name = "Main Savings", balance = 0.0, type = AccountType.SAVINGS, icon = "🏦", color = "#2ECC71"),
                Account(name = "Checking", balance = 0.0, type = AccountType.CHECKING, icon = "💳", color = "#3498DB"),
                Account(name = "Cash Wallet", balance = 0.0, type = AccountType.CASH, icon = "👛", color = "#E67E22")
            )
            accounts.forEach { db.accountDao().insert(it) }
        }
    }
}
