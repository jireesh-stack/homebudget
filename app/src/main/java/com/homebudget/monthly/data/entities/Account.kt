package com.homebudget.monthly.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val balance: Double,
    val type: AccountType,
    val icon: String,
    val color: String
)

enum class AccountType { SAVINGS, CHECKING, CASH, INVESTMENT }
