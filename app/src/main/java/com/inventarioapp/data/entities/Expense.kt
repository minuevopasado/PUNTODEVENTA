package com.inventarioapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("userId"), Index("category"), Index("date")]
)
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val description: String,
    val amount: Double,
    val category: ExpenseCategory,
    val date: Long = System.currentTimeMillis(),
    val userId: Long? = null,
    val receiptPath: String? = null,
    val notes: String? = null,
    val isRecurring: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

enum class ExpenseCategory {
    RENT,
    UTILITIES,
    SALARIES,
    SUPPLIES,
    MARKETING,
    MAINTENANCE,
    INSURANCE,
    TAXES,
    OTHER
}