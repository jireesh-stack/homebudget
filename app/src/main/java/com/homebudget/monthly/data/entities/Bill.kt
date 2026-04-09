package com.homebudget.monthly.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bills")
data class Bill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    val dueDay: Int,            // day of month (1-31)
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val isPaid: Boolean = false,
    val paidDate: Long? = null,
    val reminderEnabled: Boolean = true,
    val reminderEmail: String = "",
    val note: String = "",
    val isRecurring: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
