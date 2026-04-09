package com.homebudget.monthly.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "incomes")
data class Income(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val categoryId: Long?,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val date: Long = System.currentTimeMillis(),
    val note: String = "",
    val isRecurring: Boolean = false
)
