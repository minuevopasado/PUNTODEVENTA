package com.inventarioapp.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Environment
import com.inventarioapp.data.entities.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SalesDocumentService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    companion object {
        private const val DOCUMENTS_FOLDER = "InventarioApp_Documents"
        private const val RECEIPTS_FOLDER = "Receipts"
        private const val INVOICES_FOLDER = "Invoices"
        private const val REPORTS_FOLDER = "Reports"
    }
    
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    private val numberFormat = NumberFormat.getCurrencyInstance(Locale("es", "ES"))
    
    init {
        createDirectories()
    }
    
    private fun createDirectories() {
        val documentsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), DOCUMENTS_FOLDER)
        File(documentsDir, RECEIPTS_FOLDER).mkdirs()
        File(documentsDir, INVOICES_FOLDER).mkdirs()
        File(documentsDir, REPORTS_FOLDER).mkdirs()
    }
    
    suspend fun generateSalesReceipt(
        transaction: Transaction,
        transactionItems: List<TransactionItem>,
        products: List<Product>,
        companyInfo: AppSettings
    ): DocumentResult = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 size
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            
            // Configurar documento
            var yPosition = 50f
            val leftMargin = 50f
            val rightMargin = 545f
            
            // Título del documento
            paint.textSize = 24f
            paint.isFakeBoldText = true
            canvas.drawText("RECIBO DE VENTA", leftMargin, yPosition, paint)
            yPosition += 40f
            
            // Información de la empresa
            paint.textSize = 14f
            paint.isFakeBoldText = false
            canvas.drawText("${companyInfo.companyName}", leftMargin, yPosition, paint)
            yPosition += 20f
            canvas.drawText("${companyInfo.companyAddress}", leftMargin, yPosition, paint)
            yPosition += 20f
            canvas.drawText("Tel: ${companyInfo.companyPhone}", leftMargin, yPosition, paint)
            yPosition += 20f
            canvas.drawText("Email: ${companyInfo.companyEmail}", leftMargin, yPosition, paint)
            yPosition += 30f
            
            // Línea separadora
            canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)
            yPosition += 30f
            
            // Información de la transacción
            paint.isFakeBoldText = true
            canvas.drawText("INFORMACIÓN DE LA VENTA", leftMargin, yPosition, paint)
            yPosition += 25f
            
            paint.isFakeBoldText = false
            canvas.drawText("No. Transacción: ${transaction.id}", leftMargin, yPosition, paint)
            canvas.drawText("Fecha: ${dateFormat.format(Date(transaction.date))}", 300f, yPosition, paint)
            yPosition += 20f
            
            transaction.reference?.let { ref ->
                canvas.drawText("Referencia: $ref", leftMargin, yPosition, paint)
                yPosition += 20f
            }
            
            yPosition += 20f
            
            // Encabezados de tabla
            paint.isFakeBoldText = true
            canvas.drawText("PRODUCTO", leftMargin, yPosition, paint)
            canvas.drawText("CANT.", 250f, yPosition, paint)
            canvas.drawText("PRECIO", 320f, yPosition, paint)
            canvas.drawText("TOTAL", 420f, yPosition, paint)
            yPosition += 5f
            
            // Línea bajo encabezados
            canvas.drawLine(leftMargin, yPosition, rightMargin, yPosition, paint)
            yPosition += 20f
            
            // Items de la transacción
            paint.isFakeBoldText = false
            var subtotal = 0.0
            
            transactionItems.forEach { item ->
                val product = products.find { it.id == item.productId }
                val productName = product?.name ?: "Producto no encontrado"
                
                // Truncar nombre si es muy largo
                val displayName = if (productName.length > 25) {
                    productName.substring(0, 22) + "..."
                } else {
                    productName
                }
                
                canvas.drawText(displayName, leftMargin, yPosition, paint)
                canvas.drawText(item.quantity.toString(), 250f, yPosition, paint)
                canvas.drawText(numberFormat.format(item.unitPrice), 300f, yPosition, paint)
                canvas.drawText(numberFormat.format(item.totalPrice), 420f, yPosition, paint)
                
                subtotal += item.totalPrice
                yPosition += 20f
            }
            
            yPosition += 10f
            
            // Línea antes del total
            canvas.drawLine(300f, yPosition, rightMargin, yPosition, paint)
            yPosition += 20f
            
            // Totales
            val tax = subtotal * (companyInfo.taxRate / 100)
            val total = subtotal + tax
            
            paint.isFakeBoldText = true
            canvas.drawText("Subtotal:", 320f, yPosition, paint)
            canvas.drawText(numberFormat.format(subtotal), 420f, yPosition, paint)
            yPosition += 20f
            
            if (companyInfo.taxRate > 0) {
                canvas.drawText("Impuestos (${companyInfo.taxRate}%):", 320f, yPosition, paint)
                canvas.drawText(numberFormat.format(tax), 420f, yPosition, paint)
                yPosition += 20f
            }
            
            paint.textSize = 16f
            canvas.drawText("TOTAL:", 320f, yPosition, paint)
            canvas.drawText(numberFormat.format(total), 420f, yPosition, paint)
            yPosition += 40f
            
            // Notas
            transaction.notes?.let { notes ->
                paint.textSize = 12f
                paint.isFakeBoldText = false
                canvas.drawText("Notas: $notes", leftMargin, yPosition, paint)
                yPosition += 30f
            }
            
            // Footer
            paint.textSize = 10f
            canvas.drawText("¡Gracias por su compra!", leftMargin, yPosition, paint)
            canvas.drawText("Documento generado el ${dateFormat.format(Date())}", leftMargin, yPosition + 15f, paint)
            
            pdfDocument.finishPage(page)
            
            // Guardar archivo
            val fileName = "recibo_${transaction.id}_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val file = File(File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$DOCUMENTS_FOLDER/$RECEIPTS_FOLDER"), fileName)
            
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            
            pdfDocument.close()
            
            DocumentResult.Success(
                filePath = file.absolutePath,
                fileName = fileName,
                fileSize = file.length()
            )
            
        } catch (e: Exception) {
            DocumentResult.Error("Error generando recibo: ${e.message}")
        }
    }
    
    suspend fun generateSalesReport(
        transactions: List<Transaction>,
        dateRange: Pair<Long, Long>,
        companyInfo: AppSettings
    ): DocumentResult = withContext(Dispatchers.IO) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create() // A4 landscape
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            
            var yPosition = 50f
            val leftMargin = 50f
            
            // Título
            paint.textSize = 20f
            paint.isFakeBoldText = true
            canvas.drawText("REPORTE DE VENTAS", leftMargin, yPosition, paint)
            yPosition += 30f
            
            // Información de la empresa
            paint.textSize = 12f
            paint.isFakeBoldText = false
            canvas.drawText("${companyInfo.companyName}", leftMargin, yPosition, paint)
            yPosition += 20f
            
            // Período del reporte
            paint.isFakeBoldText = true
            canvas.drawText("Período: ${dateFormat.format(Date(dateRange.first))} - ${dateFormat.format(Date(dateRange.second))}", leftMargin, yPosition, paint)
            yPosition += 30f
            
            // Estadísticas generales
            val totalSales = transactions.filter { it.type == TransactionType.SALE }.sumOf { it.total }
            val totalTransactions = transactions.size
            val averageTicket = if (totalTransactions > 0) totalSales / totalTransactions else 0.0
            
            paint.isFakeBoldText = false
            canvas.drawText("Total de Ventas: ${numberFormat.format(totalSales)}", leftMargin, yPosition, paint)
            canvas.drawText("Número de Transacciones: $totalTransactions", 300f, yPosition, paint)
            yPosition += 20f
            canvas.drawText("Ticket Promedio: ${numberFormat.format(averageTicket)}", leftMargin, yPosition, paint)
            yPosition += 40f
            
            // Encabezados de tabla
            paint.isFakeBoldText = true
            canvas.drawText("FECHA", leftMargin, yPosition, paint)
            canvas.drawText("ID", 150f, yPosition, paint)
            canvas.drawText("TIPO", 200f, yPosition, paint)
            canvas.drawText("TOTAL", 300f, yPosition, paint)
            canvas.drawText("REFERENCIA", 400f, yPosition, paint)
            canvas.drawText("NOTAS", 550f, yPosition, paint)
            yPosition += 5f
            
            canvas.drawLine(leftMargin, yPosition, 750f, yPosition, paint)
            yPosition += 15f
            
            // Listar transacciones
            paint.isFakeBoldText = false
            paint.textSize = 10f
            
            transactions.sortedByDescending { it.date }.forEach { transaction ->
                canvas.drawText(SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(transaction.date)), leftMargin, yPosition, paint)
                canvas.drawText(transaction.id.toString(), 150f, yPosition, paint)
                canvas.drawText(transaction.type.name, 200f, yPosition, paint)
                canvas.drawText(numberFormat.format(transaction.total), 300f, yPosition, paint)
                canvas.drawText(transaction.reference ?: "-", 400f, yPosition, paint)
                
                val notes = transaction.notes ?: "-"
                val truncatedNotes = if (notes.length > 20) notes.substring(0, 17) + "..." else notes
                canvas.drawText(truncatedNotes, 550f, yPosition, paint)
                
                yPosition += 15f
                
                if (yPosition > 550f) break // Evitar que el contenido se salga de la página
            }
            
            pdfDocument.finishPage(page)
            
            // Guardar archivo
            val fileName = "reporte_ventas_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.pdf"
            val file = File(File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$DOCUMENTS_FOLDER/$REPORTS_FOLDER"), fileName)
            
            FileOutputStream(file).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            
            pdfDocument.close()
            
            DocumentResult.Success(
                filePath = file.absolutePath,
                fileName = fileName,
                fileSize = file.length()
            )
            
        } catch (e: Exception) {
            DocumentResult.Error("Error generando reporte: ${e.message}")
        }
    }
    
    suspend fun getDocumentsByTransaction(transactionId: Long): List<File> = withContext(Dispatchers.IO) {
        try {
            val receiptsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$DOCUMENTS_FOLDER/$RECEIPTS_FOLDER")
            receiptsDir.listFiles { file ->
                file.name.contains("recibo_${transactionId}_")
            }?.toList() ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun getAllSalesDocuments(): List<File> = withContext(Dispatchers.IO) {
        try {
            val receiptsDir = File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), "$DOCUMENTS_FOLDER/$RECEIPTS_FOLDER")
            receiptsDir.listFiles { file ->
                file.extension.lowercase() == "pdf"
            }?.sortedByDescending { it.lastModified() } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun deleteDocument(filePath: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val file = File(filePath)
            file.delete()
        } catch (e: Exception) {
            false
        }
    }
}

sealed class DocumentResult {
    data class Success(
        val filePath: String,
        val fileName: String,
        val fileSize: Long
    ) : DocumentResult()
    
    data class Error(val message: String) : DocumentResult()
}
