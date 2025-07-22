package com.inventarioapp.data.database.dao

import androidx.room.*
import com.inventarioapp.data.entities.Transaction
import com.inventarioapp.data.entities.TransactionType
import kotlinx.coroutines.flow.Flow
import java.util.*

@Dao
interface TransactionDao {
    
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAllTransactions(): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE type = :type ORDER BY date DESC")
    fun getTransactionsByType(type: TransactionType): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getTransactionsByDateRange(startDate: Long, endDate: Long): Flow<List<Transaction>>
    
    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransactionById(id: Long): Transaction?
    
    @Query("SELECT * FROM transactions WHERE userId = :userId ORDER BY date DESC")
    fun getTransactionsByUser(userId: Long): Flow<List<Transaction>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: Transaction): Long
    
    @Update
    suspend fun updateTransaction(transaction: Transaction)
    
    @Delete
    suspend fun deleteTransaction(transaction: Transaction)
    
    @Query("SELECT SUM(total) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getTotalByTypeAndDateRange(type: TransactionType, startDate: Long, endDate: Long): Double?
    
    @Query("SELECT COUNT(*) FROM transactions WHERE type = :type AND date BETWEEN :startDate AND :endDate")
    suspend fun getTransactionCountByTypeAndDateRange(type: TransactionType, startDate: Long, endDate: Long): Int
    
    @Query("SELECT * FROM transactions WHERE isCompleted = 0 ORDER BY date DESC")
    fun getPendingTransactions(): Flow<List<Transaction>>
    
    @Query("UPDATE transactions SET isCompleted = 1 WHERE id = :id")
    suspend fun completeTransaction(id: Long)
}