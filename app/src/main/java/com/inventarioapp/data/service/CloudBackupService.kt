package com.inventarioapp.data.service

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes
import com.google.api.services.drive.model.File
import com.google.api.client.http.FileContent
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CloudBackupService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    
    private var driveService: Drive? = null
    private var googleSignInClient: GoogleSignInClient? = null
    
    companion object {
        private const val BACKUP_FOLDER_NAME = "InventarioApp_Backups"
        private const val REQUEST_SIGN_IN = 1000
    }
    
    init {
        setupGoogleSignIn()
    }
    
    private fun setupGoogleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(
                com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_FILE),
                com.google.android.gms.common.api.Scope(DriveScopes.DRIVE_APPDATA)
            )
            .build()
            
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }
    
    suspend fun initializeDriveService(): Boolean = withContext(Dispatchers.IO) {
        try {
            val account = GoogleSignIn.getLastSignedInAccount(context)
            if (account != null) {
                setupDriveService(account)
                true
            } else {
                false
            }
        } catch (e: Exception) {
            false
        }
    }
    
    private fun setupDriveService(account: GoogleSignInAccount) {
        val credential = GoogleAccountCredential.usingOAuth2(
            context,
            setOf(DriveScopes.DRIVE_FILE, DriveScopes.DRIVE_APPDATA)
        )
        credential.selectedAccount = account.account
        
        driveService = Drive.Builder(
            AndroidHttp.newCompatibleTransport(),
            GsonFactory(),
            credential
        )
            .setApplicationName("InventarioApp")
            .build()
    }
    
    suspend fun backupDatabase(databaseFile: java.io.File): BackupResult = withContext(Dispatchers.IO) {
        try {
            val drive = driveService ?: return@withContext BackupResult.Error("Drive service not initialized")
            
            // Crear carpeta de backup si no existe
            val folderId = getOrCreateBackupFolder(drive)
            
            // Preparar archivo
            val timestamp = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(Date())
            val fileName = "inventario_backup_$timestamp.db"
            
            val fileMetadata = File().apply {
                name = fileName
                parents = listOf(folderId)
            }
            
            val mediaContent = FileContent("application/octet-stream", databaseFile)
            
            val uploadedFile = drive.files().create(fileMetadata, mediaContent)
                .setFields("id, name, size, createdTime")
                .execute()
            
            BackupResult.Success(
                fileName = uploadedFile.name,
                fileId = uploadedFile.id,
                size = uploadedFile.size,
                createdTime = uploadedFile.createdTime?.value ?: System.currentTimeMillis()
            )
            
        } catch (e: Exception) {
            BackupResult.Error("Error creating backup: ${e.message}")
        }
    }
    
    suspend fun restoreDatabase(fileId: String, targetFile: java.io.File): RestoreResult = withContext(Dispatchers.IO) {
        try {
            val drive = driveService ?: return@withContext RestoreResult.Error("Drive service not initialized")
            
            val inputStream: InputStream = drive.files().get(fileId).executeMediaAsInputStream()
            targetFile.outputStream().use { output ->
                inputStream.copyTo(output)
            }
            
            RestoreResult.Success("Database restored successfully")
            
        } catch (e: Exception) {
            RestoreResult.Error("Error restoring backup: ${e.message}")
        }
    }
    
    suspend fun listBackups(): List<BackupFile> = withContext(Dispatchers.IO) {
        try {
            val drive = driveService ?: return@withContext emptyList()
            
            val folderId = getOrCreateBackupFolder(drive)
            
            val result = drive.files().list()
                .setQ("'$folderId' in parents and trashed=false")
                .setFields("files(id, name, size, createdTime, modifiedTime)")
                .setOrderBy("createdTime desc")
                .execute()
            
            result.files.map { file ->
                BackupFile(
                    id = file.id,
                    name = file.name,
                    size = file.size,
                    createdTime = file.createdTime?.value ?: 0L,
                    modifiedTime = file.modifiedTime?.value ?: 0L
                )
            }
            
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun deleteBackup(fileId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val drive = driveService ?: return@withContext false
            drive.files().delete(fileId).execute()
            true
        } catch (e: Exception) {
            false
        }
    }
    
    private fun getOrCreateBackupFolder(drive: Drive): String {
        // Buscar carpeta existente
        val result = drive.files().list()
            .setQ("name='$BACKUP_FOLDER_NAME' and mimeType='application/vnd.google-apps.folder' and trashed=false")
            .setFields("files(id, name)")
            .execute()
        
        return if (result.files.isNotEmpty()) {
            result.files[0].id
        } else {
            // Crear nueva carpeta
            val folderMetadata = File().apply {
                name = BACKUP_FOLDER_NAME
                mimeType = "application/vnd.google-apps.folder"
            }
            
            val folder = drive.files().create(folderMetadata)
                .setFields("id")
                .execute()
            
            folder.id
        }
    }
    
    fun getSignInIntent(): Intent? {
        return googleSignInClient?.signInIntent
    }
    
    suspend fun signOut() = withContext(Dispatchers.IO) {
        googleSignInClient?.signOut()
        driveService = null
    }
}

sealed class BackupResult {
    data class Success(
        val fileName: String,
        val fileId: String,
        val size: Long,
        val createdTime: Long
    ) : BackupResult()
    
    data class Error(val message: String) : BackupResult()
}

sealed class RestoreResult {
    data class Success(val message: String) : RestoreResult()
    data class Error(val message: String) : RestoreResult()
}

data class BackupFile(
    val id: String,
    val name: String,
    val size: Long,
    val createdTime: Long,
    val modifiedTime: Long
)
