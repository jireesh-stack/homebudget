package com.homebudget.monthly.data.dao

import androidx.room.*
import com.homebudget.monthly.data.entities.Bill
import kotlinx.coroutines.flow.Flow

@Dao
interface BillDao {
    @Query("SELECT * FROM bills ORDER BY dueDay ASC")
    fun getAllBills(): Flow<List<Bill>>

    @Query("SELECT * FROM bills WHERE isPaid = 0 ORDER BY dueDay ASC")
    fun getUnpaidBills(): Flow<List<Bill>>

    @Query("SELECT SUM(amount) FROM bills")
    fun getTotalBills(): Flow<Double?>

    @Query("SELECT SUM(amount) FROM bills WHERE isPaid = 0")
    fun getTotalUnpaidBills(): Flow<Double?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bill: Bill): Long

    @Update
    suspend fun update(bill: Bill)

    @Delete
    suspend fun delete(bill: Bill)

    @Query("SELECT * FROM bills WHERE id = :id")
    suspend fun getById(id: Long): Bill?

    @Query("UPDATE bills SET isPaid = :isPaid, paidDate = :paidDate WHERE id = :id")
    suspend fun updatePaidStatus(id: Long, isPaid: Boolean, paidDate: Long?)
}
