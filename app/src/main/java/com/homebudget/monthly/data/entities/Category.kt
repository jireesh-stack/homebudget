package com.homebudget.monthly.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val icon: String,       // emoji or icon name
    val color: String,      // hex color string
    val type: CategoryType  // EXPENSE or INCOME
)

enum class CategoryType { EXPENSE, INCOME }
