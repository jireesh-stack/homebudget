package com.homebudget.monthly.data.dao

import androidx.room.*
import com.homebudget.monthly.data.entities.Income
import kotlinx.coroutines.flow.Flow

@Dao
interface IncomeDao {
    @Query("SELECT * FROM incomes ORDER BY date DESC")
    fun getAllIncomes(): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getIncomesByDateRange(startDate: Long, endDate: Long): Flow<List<Income>>

    @Query("SELECT * FROM incomes WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    suspend fun getIncomesByDateRangeSync(startDate: Long, endDate: Long): List<Income>

    @Query("SELECT SUM(amount) FROM incomes WHERE date BETWEEN :startDate AND :endDate")
    fun getTotalIncomeForPeriod(startDate: Long, endDate: Long): Flow<Double?>

    @Query("SELECT SUM(amount) FROM incomes WHERE date BETWEEN :startDate AND :endDate")
    suspend fun getTotalIncomeForPeriodSync(startDate: Long, endDate: Long): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(income: Income): Long

    @Update
    suspend fun update(income: Income)

    @Delete
    suspend fun delete(income: Income)

    @Query("SELECT * FROM incomes WHERE id = :id")
    suspend fun getById(id: Long): Income?
}
