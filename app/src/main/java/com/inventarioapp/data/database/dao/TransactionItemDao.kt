package com.inventarioapp.data.database.dao

import androidx.room.*
import com.inventarioapp.data.entities.TransactionItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionItemDao {
    
    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getItemsByTransaction(transactionId: Long): List<TransactionItem>
    
    @Query("SELECT * FROM transaction_items WHERE transactionId = :transactionId")
    fun getItemsByTransactionFlow(transactionId: Long): Flow<List<TransactionItem>>
    
    @Query("SELECT * FROM transaction_items WHERE productId = :productId")
    suspend fun getItemsByProduct(productId: Long): List<TransactionItem>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItem(item: TransactionItem): Long
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransactionItems(items: List<TransactionItem>)
    
    @Update
    suspend fun updateTransactionItem(item: TransactionItem)
    
    @Delete
    suspend fun deleteTransactionItem(item: TransactionItem)
    
    @Query("DELETE FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun deleteItemsByTransaction(transactionId: Long)
    
    @Query("SELECT SUM(totalPrice) FROM transaction_items WHERE transactionId = :transactionId")
    suspend fun getTotalByTransaction(transactionId: Long): Double?
}
