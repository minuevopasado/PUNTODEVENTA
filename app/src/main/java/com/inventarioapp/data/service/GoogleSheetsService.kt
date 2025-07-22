package com.inventarioapp.data.service

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.sheets.v4.Sheets
import com.google.api.services.sheets.v4.SheetsScopes
import com.google.api.services.sheets.v4.model.*
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GoogleSheetsService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var sheetsService: Sheets? = null
    
    companion object {
        private const val SPREADSHEET_TITLE = "InventarioApp_Database"
        private val REQUIRED_SCOPES = listOf(
            SheetsScopes.SPREADSHEETS,
            SheetsScopes.DRIVE_FILE
        )
    }
    
    suspend fun initializeSheetsService(): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                setupSheetsService(account)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun setupSheetsService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            REQUIRED_SCOPES
        )
        credential.selectedAccount = account.account
        
        sheetsService = Sheets.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("InventarioApp")
            .build()
    }
    
    suspend fun createOrUpdateSpreadsheet(): SheetsResult = withContext(Dispatchers.IO) {
        try {
            val sheets = sheetsService ?: return@withContext SheetsResult.Error("Sheets service not initialized")
            
            // Buscar spreadsheet existente
            val existingSpreadsheet = findExistingSpreadsheet()
            
            val spreadsheetId = existingSpreadsheet ?: createNewSpreadsheet()
            
            // Configurar hojas
            setupSheets(spreadsheetId)
            
            SheetsResult.Success(spreadsheetId, "Spreadsheet configured successfully")
            
        } catch (e: Exception) {
            SheetsResult.Error("Error setting up spreadsheet: ${e.message}")
        }
    }
    
    private suspend fun findExistingSpreadsheet(): String? = withContext(Dispatchers.IO) {
        try {
            // Aquí deberías implementar búsqueda en Drive
            // Por simplicidad, retornamos null para crear uno nuevo
            null
        } catch (e: Exception) {
            null
        }
    }
    
    private suspend fun createNewSpreadsheet(): String = withContext(Dispatchers.IO) {
        val sheets = sheetsService!!
        
        val spreadsheet = Spreadsheet().apply {
            properties = SpreadsheetProperties().apply {
                title = SPREADSHEET_TITLE + "_" + SimpleDateFormat("yyyy_MM_dd", Locale.getDefault()).format(Date())
            }
        }
        
        val result = sheets.spreadsheets().create(spreadsheet).execute()
        result.spreadsheetId
    }
    
    private suspend fun setupSheets(spreadsheetId: String) = withContext(Dispatchers.IO) {
        val sheets = sheetsService!!
        
        // Definir hojas necesarias
        val requiredSheets = listOf(
            "Products" to getProductHeaders(),
            "Categories" to getCategoryHeaders(),
            "Transactions" to getTransactionHeaders(),
            "Transaction_Items" to getTransactionItemHeaders(),
            "Users" to getUserHeaders(),
            "Expenses" to getExpenseHeaders(),
            "Documents" to getDocumentHeaders()
        )
        
        // Obtener hojas existentes
        val existingSheets = sheets.spreadsheets().get(spreadsheetId).execute().sheets
        val existingSheetNames = existingSheets.map { it.properties.title }
        
        val requests = mutableListOf<Request>()
        
        // Crear hojas faltantes
        requiredSheets.forEach { (sheetName, headers) ->
            if (!existingSheetNames.contains(sheetName)) {
                requests.add(
                    Request().setAddSheet(
                        AddSheetRequest().setProperties(
                            SheetProperties().setTitle(sheetName)
                        )
                    )
                )
            }
        }
        
        // Ejecutar requests de creación de hojas
        if (requests.isNotEmpty()) {
            sheets.spreadsheets().batchUpdate(
                spreadsheetId,
                BatchUpdateSpreadsheetRequest().setRequests(requests)
            ).execute()
        }
        
        // Agregar headers a cada hoja
        requiredSheets.forEach { (sheetName, headers) ->
            updateSheetHeaders(spreadsheetId, sheetName, headers)
        }
    }
    
    private suspend fun updateSheetHeaders(
        spreadsheetId: String,
        sheetName: String,
        headers: List<String>
    ) = withContext(Dispatchers.IO) {
        val sheets = sheetsService!!
        
        val values = listOf(headers)
        val body = ValueRange().setValues(values)
        
        sheets.spreadsheets().values()
            .update(spreadsheetId, "$sheetName!A1", body)
            .setValueInputOption("RAW")
            .execute()
    }
    
    suspend fun syncProductsToSheet(
        spreadsheetId: String,
        products: List<com.inventarioapp.data.entities.Product>
    ): SheetsResult = withContext(Dispatchers.IO) {
        try {
            val sheets = sheetsService ?: return@withContext SheetsResult.Error("Sheets service not initialized")
            
            val values = products.map { product ->
                listOf(
                    product.id.toString(),
                    product.name,
                    product.description ?: "",
                    product.barcode ?: "",
                    product.sku ?: "",
                    product.categoryId?.toString() ?: "",
                    product.price.toString(),
                    product.cost.toString(),
                    product.stock.toString(),
                    product.minStock.toString(),
                    product.maxStock.toString(),
                    product.isActive.toString(),
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(product.createdAt)),
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(product.updatedAt))
                )
            }
            
            // Limpiar datos existentes (excepto headers)
            sheets.spreadsheets().values()
                .clear(spreadsheetId, "Products!A2:Z", ClearValuesRequest())
                .execute()
            
            // Insertar nuevos datos
            if (values.isNotEmpty()) {
                val body = ValueRange().setValues(values)
                sheets.spreadsheets().values()
                    .update(spreadsheetId, "Products!A2", body)
                    .setValueInputOption("RAW")
                    .execute()
            }
            
            SheetsResult.Success(spreadsheetId, "${products.size} products synced successfully")
            
        } catch (e: Exception) {
            SheetsResult.Error("Error syncing products: ${e.message}")
        }
    }
    
    suspend fun syncTransactionsToSheet(
        spreadsheetId: String,
        transactions: List<com.inventarioapp.data.entities.Transaction>
    ): SheetsResult = withContext(Dispatchers.IO) {
        try {
            val sheets = sheetsService ?: return@withContext SheetsResult.Error("Sheets service not initialized")
            
            val values = transactions.map { transaction ->
                listOf(
                    transaction.id.toString(),
                    transaction.type.name,
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(transaction.date)),
                    transaction.userId?.toString() ?: "",
                    transaction.total.toString(),
                    transaction.notes ?: "",
                    transaction.reference ?: "",
                    transaction.isCompleted.toString(),
                    SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(transaction.createdAt))
                )
            }
            
            // Limpiar y actualizar
            sheets.spreadsheets().values()
                .clear(spreadsheetId, "Transactions!A2:Z", ClearValuesRequest())
                .execute()
            
            if (values.isNotEmpty()) {
                val body = ValueRange().setValues(values)
                sheets.spreadsheets().values()
                    .update(spreadsheetId, "Transactions!A2", body)
                    .setValueInputOption("RAW")
                    .execute()
            }
            
            SheetsResult.Success(spreadsheetId, "${transactions.size} transactions synced successfully")
            
        } catch (e: Exception) {
            SheetsResult.Error("Error syncing transactions: ${e.message}")
        }
    }
    
    suspend fun readProductsFromSheet(spreadsheetId: String): List<com.inventarioapp.data.entities.Product> = withContext(Dispatchers.IO) {
        try {
            val sheets = sheetsService ?: return@withContext emptyList()
            
            val response = sheets.spreadsheets().values()
                .get(spreadsheetId, "Products!A2:Z")
                .execute()
            
            val values = response.getValues() ?: return@withContext emptyList()
            
            values.mapNotNull { row ->
                try {
                    if (row.size >= 11) {
                        com.inventarioapp.data.entities.Product(
                            id = if (row[0].toString().isNotEmpty()) row[0].toString().toLong() else 0L,
                            name = row[1].toString(),
                            description = row[2].toString().takeIf { it.isNotEmpty() },
                            barcode = row[3].toString().takeIf { it.isNotEmpty() },
                            sku = row[4].toString().takeIf { it.isNotEmpty() },
                            categoryId = row[5].toString().takeIf { it.isNotEmpty() }?.toLongOrNull(),
                            price = row[6].toString().toDoubleOrNull() ?: 0.0,
                            cost = row[7].toString().toDoubleOrNull() ?: 0.0,
                            stock = row[8].toString().toIntOrNull() ?: 0,
                            minStock = row[9].toString().toIntOrNull() ?: 0,
                            maxStock = row[10].toString().toIntOrNull() ?: 1000,
                            isActive = row.getOrNull(11)?.toString()?.toBoolean() ?: true
                        )
                    } else null
                } catch (e: Exception) {
                    null
                }
            }
            
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun getProductHeaders(): List<String> = listOf(
        "ID", "Name", "Description", "Barcode", "SKU", "Category ID", 
        "Price", "Cost", "Stock", "Min Stock", "Max Stock", "Is Active", 
        "Created At", "Updated At"
    )
    
    private fun getCategoryHeaders(): List<String> = listOf(
        "ID", "Name", "Description", "Color", "Is Active", "Created At", "Updated At"
    )
    
    private fun getTransactionHeaders(): List<String> = listOf(
        "ID", "Type", "Date", "User ID", "Total", "Notes", "Reference", 
        "Is Completed", "Created At"
    )
    
    private fun getTransactionItemHeaders(): List<String> = listOf(
        "ID", "Transaction ID", "Product ID", "Quantity", "Unit Price", 
        "Total Price", "Notes"
    )
    
    private fun getUserHeaders(): List<String> = listOf(
        "ID", "Username", "Email", "Role", "Is Active", "Created At", "Updated At"
    )
    
    private fun getExpenseHeaders(): List<String> = listOf(
        "ID", "Description", "Amount", "Category", "Date", "User ID", 
        "Receipt Path", "Notes", "Is Recurring", "Created At"
    )
    
    private fun getDocumentHeaders(): List<String> = listOf(
        "ID", "Document Type", "File Name", "File Path", "File Size", 
        "MIME Type", "Transaction ID", "Expense ID", "Is Generated", 
        "Description", "Created At"
    )
}

sealed class SheetsResult {
    data class Success(val spreadsheetId: String, val message: String) : SheetsResult()
    data class Error(val message: String) : SheetsResult()
}
