package com.inventarioapp.data.database.dao

import androidx.room.*
import com.inventarioapp.data.entities.AppSettings
import kotlinx.coroutines.flow.Flow

@Dao
interface AppSettingsDao {
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    suspend fun getSettings(): AppSettings?
    
    @Query("SELECT * FROM app_settings WHERE id = 1")
    fun getSettingsFlow(): Flow<AppSettings?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSettings(settings: AppSettings)
    
    @Update
    suspend fun updateSettings(settings: AppSettings)
    
    @Query("UPDATE app_settings SET isDarkMode = :isDarkMode WHERE id = 1")
    suspend fun updateDarkMode(isDarkMode: Boolean)
    
    @Query("UPDATE app_settings SET currency = :currency, currencySymbol = :currencySymbol WHERE id = 1")
    suspend fun updateCurrency(currency: String, currencySymbol: String)
    
    @Query("UPDATE app_settings SET companyName = :name, companyAddress = :address, companyPhone = :phone, companyEmail = :email WHERE id = 1")
    suspend fun updateCompanyInfo(name: String, address: String, phone: String, email: String)
    
    @Query("UPDATE app_settings SET lastBackupDate = :date WHERE id = 1")
    suspend fun updateLastBackupDate(date: Long)
}
