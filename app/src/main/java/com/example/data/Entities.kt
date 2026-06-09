package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val title: String,
    val description: String = "",
    val amount: Double,
    val type: String, // "INCOME" or "EXPENSE"
    val categoryName: String,
    val dateMillis: Long,
    val note: String = "",
    val imageUri: String? = null,
    val isDeleted: Boolean = false,
    val deletedDateMillis: Long? = null
)

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val type: String // "INCOME" or "EXPENSE"
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: String = "",
    val title: String,
    val description: String,
    val dateMillis: Long
)

