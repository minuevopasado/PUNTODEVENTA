package com.inventarioapp.data.database.dao

import androidx.room.*
import com.inventarioapp.data.entities.Document
import com.inventarioapp.data.entities.DocumentType
import kotlinx.coroutines.flow.Flow

@Dao
interface DocumentDao {
    
    @Query("SELECT * FROM documents WHERE transactionId = :transactionId ORDER BY createdAt DESC")
    fun getDocumentsByTransaction(transactionId: Long): Flow<List<Document>>
    
    @Query("SELECT * FROM documents WHERE expenseId = :expenseId ORDER BY createdAt DESC")
    fun getDocumentsByExpense(expenseId: Long): Flow<List<Document>>
    
    @Query("SELECT * FROM documents WHERE documentType = :documentType ORDER BY createdAt DESC")
    fun getDocumentsByType(documentType: DocumentType): Flow<List<Document>>
    
    @Query("SELECT * FROM documents WHERE id = :id")
    suspend fun getDocumentById(id: Long): Document?
    
    @Query("SELECT * FROM documents WHERE isGenerated = 1 ORDER BY createdAt DESC")
    fun getGeneratedDocuments(): Flow<List<Document>>
    
    @Query("SELECT * FROM documents WHERE fileName LIKE '%' || :query || '%' ORDER BY createdAt DESC")
    fun searchDocuments(query: String): Flow<List<Document>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDocument(document: Document): Long
    
    @Update
    suspend fun updateDocument(document: Document)
    
    @Delete
    suspend fun deleteDocument(document: Document)
    
    @Query("DELETE FROM documents WHERE transactionId = :transactionId")
    suspend fun deleteDocumentsByTransaction(transactionId: Long)
    
    @Query("DELETE FROM documents WHERE expenseId = :expenseId")
    suspend fun deleteDocumentsByExpense(expenseId: Long)
    
    @Query("SELECT COUNT(*) FROM documents WHERE transactionId = :transactionId")
    suspend fun getDocumentCountByTransaction(transactionId: Long): Int
    
    @Query("SELECT COUNT(*) FROM documents WHERE expenseId = :expenseId")
    suspend fun getDocumentCountByExpense(expenseId: Long): Int
    
    @Query("SELECT SUM(fileSize) FROM documents")
    suspend fun getTotalDocumentSize(): Long?
}