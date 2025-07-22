package com.inventarioapp.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventarioapp.data.database.AppDatabase
import com.inventarioapp.data.repository.ProductRepository
import com.inventarioapp.data.repository.UserRepository
import com.inventarioapp.data.service.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

data class BackupSyncState(
    val isConnectedToGoogle: Boolean = false,
    val backupFiles: List<BackupFile> = emptyList(),
    val lastBackupDate: Long? = null,
    val spreadsheetId: String? = null,
    val isLoading: Boolean = false,
    val operationType: String? = null,
    val message: String? = null,
    val error: String? = null
)

@HiltViewModel
class BackupSyncViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val cloudBackupService: CloudBackupService,
    private val googleSheetsService: GoogleSheetsService,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository,
    private val database: AppDatabase
) : ViewModel() {

    private val _backupSyncState = MutableStateFlow(BackupSyncState())
    val backupSyncState: StateFlow<BackupSyncState> = _backupSyncState.asStateFlow()

    init {
        checkGoogleConnection()
    }

    private fun checkGoogleConnection() {
        viewModelScope.launch {
            val isConnected = cloudBackupService.initializeDriveService()
            _backupSyncState.value = _backupSyncState.value.copy(
                isConnectedToGoogle = isConnected
            )
            
            if (isConnected) {
                loadBackups()
                checkSheetsConnection()
            }
        }
    }
    
    private fun checkSheetsConnection() {
        viewModelScope.launch {
            googleSheetsService.initializeSheetsService()
        }
    }

    fun signInToGoogle() {
        viewModelScope.launch {
            try {
                val signInIntent = cloudBackupService.getSignInIntent()
                // Aquí deberías manejar el intent de sign-in
                // Por ahora, simularemos una conexión exitosa
                _backupSyncState.value = _backupSyncState.value.copy(
                    message = "Por favor, inicia sesión en Google desde la configuración del dispositivo"
                )
            } catch (e: Exception) {
                _backupSyncState.value = _backupSyncState.value.copy(
                    error = "Error al conectar con Google: ${e.message}"
                )
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                cloudBackupService.signOut()
                _backupSyncState.value = BackupSyncState()
                _backupSyncState.value = _backupSyncState.value.copy(
                    message = "Sesión cerrada exitosamente"
                )
            } catch (e: Exception) {
                _backupSyncState.value = _backupSyncState.value.copy(
                    error = "Error al cerrar sesión: ${e.message}"
                )
            }
        }
    }

    fun createBackup() {
        viewModelScope.launch {
            _backupSyncState.value = _backupSyncState.value.copy(
                isLoading = true,
                operationType = "backup"
            )
            
            try {
                // Obtener archivo de base de datos
                val dbFile = File(database.openHelper.writableDatabase.path)
                
                val result = cloudBackupService.backupDatabase(dbFile)
                
                when (result) {
                    is BackupResult.Success -> {
                        _backupSyncState.value = _backupSyncState.value.copy(
                            isLoading = false,
                            operationType = null,
                            lastBackupDate = result.createdTime,
                            message = "Backup creado exitosamente: ${result.fileName}"
                        )
                        loadBackups() // Actualizar lista
                    }
                    is BackupResult.Error -> {
                        _backupSyncState.value = _backupSyncState.value.copy(
                            isLoading = false,
                            operationType = null,
                            error = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _backupSyncState.value = _backupSyncState.value.copy(
                    isLoading = false,
                    operationType = null,
                    error = "Error creando backup: ${e.message}"
                )
            }
        }
    }

    fun loadBackups() {
        viewModelScope.launch {
            try {
                val backups = cloudBackupService.listBackups()
                _backupSyncState.value = _backupSyncState.value.copy(
                    backupFiles = backups
                )
            } catch (e: Exception) {
                _backupSyncState.value = _backupSyncState.value.copy(
                    error = "Error cargando backups: ${e.message}"
                )
            }
        }
    }

    fun restoreBackup(fileId: String) {
        viewModelScope.launch {
            _backupSyncState.value = _backupSyncState.value.copy(
                isLoading = true,
                operationType = "restore"
            )
            
            try {
                // Cerrar base de datos
                database.close()
                
                val dbFile = File(database.openHelper.writableDatabase.path)
                val result = cloudBackupService.restoreDatabase(fileId, dbFile)
                
                when (result) {
                    is RestoreResult.Success -> {
                        _backupSyncState.value = _backupSyncState.value.copy(
                            isLoading = false,
                            operationType = null,
                            message = "Base de datos restaurada exitosamente. Reinicia la app."
                        )
                    }
                    is RestoreResult.Error -> {
                        _backupSyncState.value = _backupSyncState.value.copy(
                            isLoading = false,
                            operationType = null,
                            error = result.message
                        )
                    }
                }
            } catch (e: Exception) {
                _backupSyncState.value = _backupSyncState.value.copy(
                    isLoading = false,
                    operationType = null,
                    error = "Error restaurando backup: ${e.message}"
                )
            }
        }
    }

    fun deleteBackup(fileId: String) {
        viewModelScope.launch {
            try {
                val success = cloudBackupService.deleteBackup(fileId)
                if (success) {
                    _backupSyncState.value = _backupSyncState.value.copy(
                        message = "Backup eliminado exitosamente"
                    )
                    loadBackups() // Actualizar lista
                } else {
                    _backupSyncState.value = _backupSyncState.value.copy(
                        error = "Error eliminando backup"
                    )
                }
            } catch (e: Exception) {
                _backupSyncState.value = _backupSyncState.value.copy(
                    error = "Error eliminando backup: ${e.message}"
                )
            }
        }
    }

    fun syncToSheets() {
        viewModelScope.launch {
            _backupSyncState.value = _backupSyncState.value.copy(
                isLoading = true,
                operationType = "sync_to_sheets"
            )
            
            try {
                // Crear o actualizar spreadsheet
                val sheetsResult = googleSheetsService.createOrUpdateSpreadsheet()
                
                when (sheetsResult) {
                    is SheetsResult.Success -> {
                        val spreadsheetId = sheetsResult.spreadsheetId
                        
                        // Obtener datos
                        val products = productRepository.getAllProducts().first()
                        
                        // Sincronizar productos
                        val syncResult = googleSheetsService.syncProductsToSheet(spreadsheetId, products)
                        
                        when (syncResult) {
                            is SheetsResult.Success -> {
                                _backupSyncState.value = _backupSyncState.value.copy(
                                    isLoading = false,
                                    operationType = null,
                                    spreadsheetId = spreadsheetId,
                                    message = "Datos sincronizados exitosamente con Google Sheets"
                                )
                            }
                            is SheetsResult.Error -> {
                                _backupSyncState.value = _backupSyncState.value.copy(
                                    isLoading = false,
                                    operationType = null,
                                    error = syncResult.message
                                )
                            }
                        }
                    }
                    is SheetsResult.Error -> {
                        _backupSyncState.value = _backupSyncState.value.copy(
                            isLoading = false,
                            operationType = null,
                            error = sheetsResult.message
                        )
                    }
                }
            } catch (e: Exception) {
                _backupSyncState.value = _backupSyncState.value.copy(
                    isLoading = false,
                    operationType = null,
                    error = "Error sincronizando con Sheets: ${e.message}"
                )
            }
        }
    }

    fun syncFromSheets() {
        viewModelScope.launch {
            _backupSyncState.value = _backupSyncState.value.copy(
                isLoading = true,
                operationType = "sync_from_sheets"
            )
            
            try {
                val spreadsheetId = _backupSyncState.value.spreadsheetId
                if (spreadsheetId == null) {
                    _backupSyncState.value = _backupSyncState.value.copy(
                        isLoading = false,
                        operationType = null,
                        error = "No hay un spreadsheet configurado"
                    )
                    return@launch
                }
                
                val products = googleSheetsService.readProductsFromSheet(spreadsheetId)
                
                // Actualizar productos en la base de datos local
                products.forEach { product ->
                    if (product.id == 0L) {
                        productRepository.insertProduct(product)
                    } else {
                        productRepository.updateProduct(product)
                    }
                }
                
                _backupSyncState.value = _backupSyncState.value.copy(
                    isLoading = false,
                    operationType = null,
                    message = "${products.size} productos importados desde Google Sheets"
                )
                
            } catch (e: Exception) {
                _backupSyncState.value = _backupSyncState.value.copy(
                    isLoading = false,
                    operationType = null,
                    error = "Error importando desde Sheets: ${e.message}"
                )
            }
        }
    }

    fun clearMessage() {
        _backupSyncState.value = _backupSyncState.value.copy(message = null)
    }

    fun clearError() {
        _backupSyncState.value = _backupSyncState.value.copy(error = null)
    }
}
