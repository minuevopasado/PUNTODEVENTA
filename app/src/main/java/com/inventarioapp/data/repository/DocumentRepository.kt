package com.inventarioapp.data.repository

import com.inventarioapp.data.database.dao.DocumentDao
import com.inventarioapp.data.entities.Document
import com.inventarioapp.data.entities.DocumentType
import com.inventarioapp.data.service.FileService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DocumentRepository @Inject constructor(
    private val documentDao: DocumentDao,
    private val fileService: FileService
) {
    
    fun getDocumentsByTransaction(transactionId: Long): Flow<List<Document>> =
        documentDao.getDocumentsByTransaction(transactionId)
    
    fun getDocumentsByExpense(expenseId: Long): Flow<List<Document>> =
        documentDao.getDocumentsByExpense(expenseId)
    
    fun getDocumentsByType(documentType: DocumentType): Flow<List<Document>> =
        documentDao.getDocumentsByType(documentType)
    
    fun getGeneratedDocuments(): Flow<List<Document>> =
        documentDao.getGeneratedDocuments()
    
    fun searchDocuments(query: String): Flow<List<Document>> =
        documentDao.searchDocuments(query)
    
    suspend fun getDocumentById(id: Long): Document? =
        documentDao.getDocumentById(id)
    
    suspend fun insertDocument(document: Document): Long =
        documentDao.insertDocument(document)
    
    suspend fun updateDocument(document: Document) =
        documentDao.updateDocument(document)
    
    suspend fun deleteDocument(document: Document) {
        fileService.deleteDocument(document)
        documentDao.deleteDocument(document)
    }
    
    suspend fun deleteDocumentsByTransaction(transactionId: Long) {
        val documents = documentDao.getDocumentsByTransaction(transactionId).first()
        documents.forEach { document ->
            fileService.deleteDocument(document)
        }
        documentDao.deleteDocumentsByTransaction(transactionId)
    }
    
    suspend fun deleteDocumentsByExpense(expenseId: Long) {
        val documents = documentDao.getDocumentsByExpense(expenseId).first()
        documents.forEach { document ->
            fileService.deleteDocument(document)
        }
        documentDao.deleteDocumentsByExpense(expenseId)
    }
    
    suspend fun generateReceipt(transactionId: Long, transactionData: Map<String, Any>): Document {
        val document = fileService.generateReceipt(transactionId, transactionData)
        val documentId = documentDao.insertDocument(document)
        return document.copy(id = documentId)
    }
    
    suspend fun generateInvoice(purchaseId: Long, purchaseData: Map<String, Any>): Document {
        val document = fileService.generateInvoice(purchaseId, purchaseData)
        val documentId = documentDao.insertDocument(document)
        return document.copy(id = documentId)
    }
    
    suspend fun generateReport(reportType: String, reportData: Map<String, Any>): Document {
        val document = fileService.generateReport(reportType, reportData)
        val documentId = documentDao.insertDocument(document)
        return document.copy(id = documentId)
    }
    
    suspend fun getDocumentCountByTransaction(transactionId: Long): Int =
        documentDao.getDocumentCountByTransaction(transactionId)
    
    suspend fun getDocumentCountByExpense(expenseId: Long): Int =
        documentDao.getDocumentCountByExpense(expenseId)
    
    suspend fun getTotalDocumentSize(): Long =
        documentDao.getTotalDocumentSize() ?: 0L
}