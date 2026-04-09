package com.homebudget.monthly.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [ForeignKey(
        entity = Category::class,
        parentColumns = ["id"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val amount: Double,
    val categoryId: Long?,
    val categoryName: String,
    val categoryIcon: String,
    val categoryColor: String,
    val date: Long = System.currentTimeMillis(),
    val note: String = ""
)
