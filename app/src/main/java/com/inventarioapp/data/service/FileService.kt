package com.inventarioapp.data.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.inventarioapp.data.entities.Document
import com.inventarioapp.data.entities.DocumentType
import com.inventarioapp.data.entities.ProductImage
import java.io.*
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileService @Inject constructor(
    private val context: Context
) {
    
    private val gson = Gson()
    private val dateFormat = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
    
    // Directorios de la aplicación
    private val appDir = File(context.filesDir, "InventarioApp")
    private val imagesDir = File(appDir, "images")
    private val documentsDir = File(appDir, "documents")
    private val receiptsDir = File(documentsDir, "receipts")
    private val invoicesDir = File(documentsDir, "invoices")
    private val reportsDir = File(documentsDir, "reports")
    private val backupsDir = File(appDir, "backups")
    
    init {
        createDirectories()
    }
    
    private fun createDirectories() {
        appDir.mkdirs()
        imagesDir.mkdirs()
        documentsDir.mkdirs()
        receiptsDir.mkdirs()
        invoicesDir.mkdirs()
        reportsDir.mkdirs()
        backupsDir.mkdirs()
    }
    
    // ==================== MANEJO DE IMÁGENES ====================
    
    fun saveProductImage(bitmap: Bitmap, productId: Long, isPrimary: Boolean = false): ProductImage {
        val fileName = "product_${productId}_${dateFormat.format(Date())}.jpg"
        val file = File(imagesDir, fileName)
        
        try {
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
            outputStream.flush()
            outputStream.close()
            
            return ProductImage(
                productId = productId,
                imagePath = file.absolutePath,
                imageName = fileName,
                isPrimary = isPrimary
            )
        } catch (e: Exception) {
            throw IOException("Error al guardar imagen: ${e.message}")
        }
    }
    
    fun saveProductImageFromUri(uri: Uri, productId: Long, isPrimary: Boolean = false): ProductImage {
        val fileName = "product_${productId}_${dateFormat.format(Date())}.jpg"
        val file = File(imagesDir, fileName)
        
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            return ProductImage(
                productId = productId,
                imagePath = file.absolutePath,
                imageName = fileName,
                isPrimary = isPrimary
            )
        } catch (e: Exception) {
            throw IOException("Error al guardar imagen desde URI: ${e.message}")
        }
    }
    
    fun loadProductImage(imagePath: String): Bitmap? {
        return try {
            BitmapFactory.decodeFile(imagePath)
        } catch (e: Exception) {
            null
        }
    }
    
    fun deleteProductImage(imagePath: String): Boolean {
        return try {
            File(imagePath).delete()
        } catch (e: Exception) {
            false
        }
    }
    
    // ==================== MANEJO DE DOCUMENTOS ====================
    
    fun saveDocumentFromUri(uri: Uri, documentType: DocumentType, transactionId: Long? = null, expenseId: Long? = null): Document {
        val originalFileName = getFileNameFromUri(uri)
        val extension = getFileExtension(originalFileName)
        val fileName = "${documentType.name.lowercase()}_${dateFormat.format(Date())}.$extension"
        
        val targetDir = when (documentType) {
            DocumentType.RECEIPT -> receiptsDir
            DocumentType.INVOICE -> invoicesDir
            DocumentType.EXPENSE_RECEIPT -> receiptsDir
            DocumentType.REPORT -> reportsDir
            else -> documentsDir
        }
        
        val file = File(targetDir, fileName)
        
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            val outputStream = FileOutputStream(file)
            
            inputStream?.use { input ->
                outputStream.use { output ->
                    input.copyTo(output)
                }
            }
            
            return Document(
                documentType = documentType,
                fileName = fileName,
                filePath = file.absolutePath,
                fileSize = file.length(),
                mimeType = getMimeType(extension),
                transactionId = transactionId,
                expenseId = expenseId,
                isGenerated = false
            )
        } catch (e: Exception) {
            throw IOException("Error al guardar documento: ${e.message}")
        }
    }
    
    fun generateReceipt(transactionId: Long, transactionData: Map<String, Any>): Document {
        val fileName = "receipt_${transactionId}_${dateFormat.format(Date())}.pdf"
        val file = File(receiptsDir, fileName)
        
        try {
            // Aquí se generaría el PDF del recibo
            // Por ahora creamos un archivo de texto como ejemplo
            val receiptContent = generateReceiptContent(transactionData)
            file.writeText(receiptContent)
            
            return Document(
                documentType = DocumentType.RECEIPT,
                fileName = fileName,
                filePath = file.absolutePath,
                fileSize = file.length(),
                mimeType = "application/pdf",
                transactionId = transactionId,
                isGenerated = true,
                description = "Recibo generado automáticamente"
            )
        } catch (e: Exception) {
            throw IOException("Error al generar recibo: ${e.message}")
        }
    }
    
    fun generateInvoice(purchaseId: Long, purchaseData: Map<String, Any>): Document {
        val fileName = "invoice_${purchaseId}_${dateFormat.format(Date())}.pdf"
        val file = File(invoicesDir, fileName)
        
        try {
            // Aquí se generaría el PDF de la factura
            val invoiceContent = generateInvoiceContent(purchaseData)
            file.writeText(invoiceContent)
            
            return Document(
                documentType = DocumentType.INVOICE,
                fileName = fileName,
                filePath = file.absolutePath,
                fileSize = file.length(),
                mimeType = "application/pdf",
                transactionId = purchaseId,
                isGenerated = true,
                description = "Factura generada automáticamente"
            )
        } catch (e: Exception) {
            throw IOException("Error al generar factura: ${e.message}")
        }
    }
    
    fun generateReport(reportType: String, reportData: Map<String, Any>): Document {
        val fileName = "report_${reportType}_${dateFormat.format(Date())}.pdf"
        val file = File(reportsDir, fileName)
        
        try {
            // Aquí se generaría el PDF del reporte
            val reportContent = generateReportContent(reportType, reportData)
            file.writeText(reportContent)
            
            return Document(
                documentType = DocumentType.REPORT,
                fileName = fileName,
                filePath = file.absolutePath,
                fileSize = file.length(),
                mimeType = "application/pdf",
                isGenerated = true,
                description = "Reporte de $reportType"
            )
        } catch (e: Exception) {
            throw IOException("Error al generar reporte: ${e.message}")
        }
    }
    
    fun getDocumentUri(document: Document): Uri {
        return FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            File(document.filePath)
        )
    }
    
    fun deleteDocument(document: Document): Boolean {
        return try {
            File(document.filePath).delete()
        } catch (e: Exception) {
            false
        }
    }
    
    // ==================== RESPALDO Y RESTAURACIÓN ====================
    
    fun createBackup(): File {
        val backupFileName = "backup_${dateFormat.format(Date())}.json"
        val backupFile = File(backupsDir, backupFileName)
        
        try {
            val backupData = createBackupData()
            backupFile.writeText(gson.toJson(backupData))
            return backupFile
        } catch (e: Exception) {
            throw IOException("Error al crear respaldo: ${e.message}")
        }
    }
    
    fun restoreBackup(backupFile: File): Boolean {
        return try {
            val backupContent = backupFile.readText()
            val backupData = gson.fromJson(backupContent, Map::class.java)
            // Aquí se restaurarían los datos
            true
        } catch (e: Exception) {
            false
        }
    }
    
    // ==================== UTILIDADES ====================
    
    private fun getFileNameFromUri(uri: Uri): String {
        return context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex("_display_name")
            cursor.moveToFirst()
            cursor.getString(nameIndex)
        } ?: "unknown_file"
    }
    
    private fun getFileExtension(fileName: String): String {
        return if (fileName.contains(".")) {
            fileName.substringAfterLast(".")
        } else {
            "txt"
        }
    }
    
    private fun getMimeType(extension: String): String {
        return when (extension.lowercase()) {
            "pdf" -> "application/pdf"
            "jpg", "jpeg" -> "image/jpeg"
            "png" -> "image/png"
            "txt" -> "text/plain"
            "csv" -> "text/csv"
            "xlsx" -> "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            else -> "application/octet-stream"
        }
    }
    
    private fun generateReceiptContent(transactionData: Map<String, Any>): String {
        return """
            RECIBO DE VENTA
            ===============
            
            Fecha: ${transactionData["date"]}
            Número: ${transactionData["id"]}
            
            Productos:
            ${transactionData["items"]}
            
            Subtotal: $${transactionData["subtotal"]}
            Impuestos: $${transactionData["taxes"]}
            Total: $${transactionData["total"]}
            
            Gracias por su compra!
        """.trimIndent()
    }
    
    private fun generateInvoiceContent(purchaseData: Map<String, Any>): String {
        return """
            FACTURA DE COMPRA
            =================
            
            Fecha: ${purchaseData["date"]}
            Número: ${purchaseData["id"]}
            Proveedor: ${purchaseData["supplier"]}
            
            Productos:
            ${purchaseData["items"]}
            
            Subtotal: $${purchaseData["subtotal"]}
            Impuestos: $${purchaseData["taxes"]}
            Total: $${purchaseData["total"]}
        """.trimIndent()
    }
    
    private fun generateReportContent(reportType: String, reportData: Map<String, Any>): String {
        return """
            REPORTE DE $reportType.uppercase()
            =============================
            
            Fecha de generación: ${Date()}
            
            Datos del reporte:
            ${gson.toJson(reportData)}
        """.trimIndent()
    }
    
    private fun createBackupData(): Map<String, Any> {
        return mapOf(
            "timestamp" to System.currentTimeMillis(),
            "version" to "1.0",
            "data" to "backup_data_here"
        )
    }
}