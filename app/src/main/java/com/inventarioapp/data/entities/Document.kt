package com.inventarioapp.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "documents",
    foreignKeys = [
        ForeignKey(
            entity = Transaction::class,
            parentColumns = ["id"],
            childColumns = ["transactionId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Expense::class,
            parentColumns = ["id"],
            childColumns = ["expenseId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("transactionId"), Index("expenseId"), Index("documentType")]
)
data class Document(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val documentType: DocumentType,
    val fileName: String,
    val filePath: String,
    val fileSize: Long,
    val mimeType: String,
    val transactionId: Long? = null,
    val expenseId: Long? = null,
    val isGenerated: Boolean = false, // true si es generado por la app, false si es cargado
    val description: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)

enum class DocumentType {
    RECEIPT,        // Recibo de venta
    INVOICE,        // Factura de compra
    EXPENSE_RECEIPT, // Recibo de gasto
    REPORT,         // Reporte generado
    CONTRACT,       // Contrato
    OTHER           // Otros documentos
}