package com.inventarioapp.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
data class AppSettings(
    @PrimaryKey
    val id: Int = 1,
    val isDarkMode: Boolean = false,
    val currency: String = "USD",
    val currencySymbol: String = "$",
    val decimalPlaces: Int = 2,
    val companyName: String = "",
    val companyAddress: String = "",
    val companyPhone: String = "",
    val companyEmail: String = "",
    val taxRate: Double = 0.0,
    val lowStockThreshold: Int = 10,
    val backupEnabled: Boolean = true,
    val autoBackupDays: Int = 7,
    val lastBackupDate: Long? = null,
    val updatedAt: Long = System.currentTimeMillis()
)