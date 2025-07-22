package com.inventarioapp.ui.viewmodels

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventarioapp.data.database.dao.*
import com.inventarioapp.data.entities.*
import com.inventarioapp.data.service.SalesDocumentService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import javax.inject.Inject

data class TransactionWithItems(
    val transaction: Transaction,
    val items: List<TransactionItemWithProduct>
)

data class TransactionItemWithProduct(
    val transactionItem: TransactionItem,
    val productName: String,
    val quantity: Int = transactionItem.quantity,
    val unitPrice: Double = transactionItem.unitPrice,
    val totalPrice: Double = transactionItem.totalPrice
)

data class SalesDocumentsState(
    val transactions: List<TransactionWithItems> = emptyList(),
    val allTransactions: List<TransactionWithItems> = emptyList(),
    val documentsGenerated: Int = 0,
    val totalSales: Double = 0.0,
    val isLoading: Boolean = false,
    val error: String? = null,
    val message: String? = null
)

@HiltViewModel
class SalesDocumentsViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val transactionDao: TransactionDao,
    private val transactionItemDao: TransactionItemDao,
    private val productDao: ProductDao,
    private val appSettingsDao: AppSettingsDao,
    private val documentDao: DocumentDao,
    private val salesDocumentService: SalesDocumentService
) : ViewModel() {

    private val _salesDocumentsState = MutableStateFlow(SalesDocumentsState())
    val salesDocumentsState: StateFlow<SalesDocumentsState> = _salesDocumentsState.asStateFlow()

    init {
        loadSalesDocuments()
    }

    fun loadSalesDocuments() {
        viewModelScope.launch {
            _salesDocumentsState.value = _salesDocumentsState.value.copy(isLoading = true)
            
            try {
                transactionDao.getTransactionsByType(TransactionType.SALE)
                    .collect { transactions ->
                        val transactionsWithItems = transactions.map { transaction ->
                            val items = transactionItemDao.getItemsByTransaction(transaction.id)
                            val itemsWithProducts = items.map { item ->
                                val product = productDao.getProductById(item.productId)
                                TransactionItemWithProduct(
                                    transactionItem = item,
                                    productName = product?.name ?: "Producto eliminado"
                                )
                            }
                            TransactionWithItems(transaction, itemsWithProducts)
                        }
                        
                        val totalSales = transactions.sumOf { it.total }
                        val documentsCount = documentDao.getDocumentCountByType(DocumentType.RECEIPT)
                        
                        _salesDocumentsState.value = _salesDocumentsState.value.copy(
                            transactions = transactionsWithItems,
                            allTransactions = transactionsWithItems,
                            totalSales = totalSales,
                            documentsGenerated = documentsCount,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _salesDocumentsState.value = _salesDocumentsState.value.copy(
                    isLoading = false,
                    error = "Error cargando documentos: ${e.message}"
                )
            }
        }
    }

    fun filterDocuments(filter: String) {
        viewModelScope.launch {
            try {
                val allTransactions = _salesDocumentsState.value.allTransactions
                val now = System.currentTimeMillis()
                val calendar = Calendar.getInstance()
                
                val filteredTransactions = when (filter) {
                    "today" -> {
                        calendar.timeInMillis = now
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        val startOfDay = calendar.timeInMillis
                        
                        allTransactions.filter { it.transaction.date >= startOfDay }
                    }
                    "week" -> {
                        calendar.timeInMillis = now
                        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        val startOfWeek = calendar.timeInMillis
                        
                        allTransactions.filter { it.transaction.date >= startOfWeek }
                    }
                    "month" -> {
                        calendar.timeInMillis = now
                        calendar.set(Calendar.DAY_OF_MONTH, 1)
                        calendar.set(Calendar.HOUR_OF_DAY, 0)
                        calendar.set(Calendar.MINUTE, 0)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        val startOfMonth = calendar.timeInMillis
                        
                        allTransactions.filter { it.transaction.date >= startOfMonth }
                    }
                    else -> allTransactions
                }
                
                val totalSales = filteredTransactions.sumOf { it.transaction.total }
                
                _salesDocumentsState.value = _salesDocumentsState.value.copy(
                    transactions = filteredTransactions,
                    totalSales = totalSales
                )
                
            } catch (e: Exception) {
                _salesDocumentsState.value = _salesDocumentsState.value.copy(
                    error = "Error filtrando documentos: ${e.message}"
                )
            }
        }
    }

    fun viewDocument(transactionId: Long) {
        viewModelScope.launch {
            try {
                val documents = salesDocumentService.getDocumentsByTransaction(transactionId)
                if (documents.isNotEmpty()) {
                    val latestDocument = documents.maxByOrNull { it.lastModified() }
                    latestDocument?.let { file ->
                        openPdfFile(file)
                    }
                } else {
                    _salesDocumentsState.value = _salesDocumentsState.value.copy(
                        message = "No hay documentos generados para esta venta. Regenera el documento."
                    )
                }
            } catch (e: Exception) {
                _salesDocumentsState.value = _salesDocumentsState.value.copy(
                    error = "Error abriendo documento: ${e.message}"
                )
            }
        }
    }

    fun regenerateDocument(transaction: Transaction) {
        viewModelScope.launch {
            try {
                _salesDocumentsState.value = _salesDocumentsState.value.copy(isLoading = true)
                
                val transactionItems = transactionItemDao.getItemsByTransaction(transaction.id)
                val products = transactionItems.map { item ->
                    productDao.getProductById(item.productId)
                }.filterNotNull()
                
                val appSettings = appSettingsDao.getSettings() ?: getDefaultSettings()
                
                val result = salesDocumentService.generateSalesReceipt(
                    transaction = transaction,
                    transactionItems = transactionItems,
                    products = products,
                    companyInfo = appSettings
                )
                
                when (result) {
                    is com.inventarioapp.data.service.DocumentResult.Success -> {
                        // Guardar registro del documento
                        val document = Document(
                            documentType = DocumentType.RECEIPT,
                            fileName = result.fileName,
                            filePath = result.filePath,
                            fileSize = result.fileSize,
                            mimeType = "application/pdf",
                            transactionId = transaction.id,
                            isGenerated = true,
                            description = "Recibo de venta regenerado"
                        )
                        documentDao.insertDocument(document)
                        
                        _salesDocumentsState.value = _salesDocumentsState.value.copy(
                            isLoading = false,
                            message = "Documento regenerado exitosamente"
                        )
                        
                        // Abrir el documento
                        openPdfFile(File(result.filePath))
                    }
                    is com.inventarioapp.data.service.DocumentResult.Error -> {
                        _salesDocumentsState.value = _salesDocumentsState.value.copy(
                            isLoading = false,
                            error = result.message
                        )
                    }
                }
                
            } catch (e: Exception) {
                _salesDocumentsState.value = _salesDocumentsState.value.copy(
                    isLoading = false,
                    error = "Error regenerando documento: ${e.message}"
                )
            }
        }
    }

    fun printDocument(transactionId: Long) {
        viewModelScope.launch {
            try {
                val documents = salesDocumentService.getDocumentsByTransaction(transactionId)
                if (documents.isNotEmpty()) {
                    val latestDocument = documents.maxByOrNull { it.lastModified() }
                    latestDocument?.let { file ->
                        val printIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "application/pdf"
                            val uri = FileProvider.getUriForFile(
                                context,
                                "${context.packageName}.fileprovider",
                                file
                            )
                            putExtra(Intent.EXTRA_STREAM, uri)
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }
                        
                        val shareIntent = Intent.createChooser(printIntent, "Imprimir documento")
                        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(shareIntent)
                        
                        _salesDocumentsState.value = _salesDocumentsState.value.copy(
                            message = "Abriendo opciones de impresión..."
                        )
                    }
                } else {
                    _salesDocumentsState.value = _salesDocumentsState.value.copy(
                        message = "No hay documentos para imprimir. Regenera el documento primero."
                    )
                }
            } catch (e: Exception) {
                _salesDocumentsState.value = _salesDocumentsState.value.copy(
                    error = "Error preparando impresión: ${e.message}"
                )
            }
        }
    }

    private fun openPdfFile(file: File) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            
            context.startActivity(intent)
        } catch (e: Exception) {
            _salesDocumentsState.value = _salesDocumentsState.value.copy(
                error = "No se pudo abrir el documento. Instala un lector de PDF."
            )
        }
    }

    private fun getDefaultSettings(): AppSettings {
        return AppSettings(
            companyName = "Mi Empresa",
            companyAddress = "Dirección de la empresa",
            companyPhone = "Teléfono",
            companyEmail = "email@empresa.com",
            taxRate = 0.0,
            currency = "USD",
            currencySymbol = "$"
        )
    }

    fun refreshDocuments() {
        loadSalesDocuments()
    }

    fun clearError() {
        _salesDocumentsState.value = _salesDocumentsState.value.copy(error = null)
    }

    fun clearMessage() {
        _salesDocumentsState.value = _salesDocumentsState.value.copy(message = null)
    }
}
