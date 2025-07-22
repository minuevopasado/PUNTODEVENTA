package com.inventarioapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.inventarioapp.ui.viewmodels.BackupSyncViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupSyncScreen(
    navController: NavController,
    viewModel: BackupSyncViewModel = hiltViewModel()
) {
    val state by viewModel.backupSyncState.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Backup y Sincronización") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Estado de conexión
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (state.isConnectedToGoogle) 
                            MaterialTheme.colorScheme.primaryContainer 
                        else 
                            MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (state.isConnectedToGoogle) 
                                    "Conectado a Google" 
                                else 
                                    "No conectado",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (state.isConnectedToGoogle) 
                                    "Backup y sync disponibles" 
                                else 
                                    "Inicia sesión para usar backup",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        if (state.isConnectedToGoogle) {
                            IconButton(onClick = { viewModel.signOut() }) {
                                Icon(Icons.Default.Logout, contentDescription = "Cerrar sesión")
                            }
                        } else {
                            Button(onClick = { viewModel.signInToGoogle() }) {
                                Text("Conectar")
                            }
                        }
                    }
                }
            }
            
            // Opciones de backup
            if (state.isConnectedToGoogle) {
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Backup de Base de Datos",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.createBackup() },
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.isLoading
                                ) {
                                    if (state.isLoading && state.operationType == "backup") {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Crear Backup")
                                }
                                
                                Button(
                                    onClick = { viewModel.loadBackups() },
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.isLoading
                                ) {
                                    Icon(Icons.Default.Refresh, contentDescription = null)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Actualizar")
                                }
                            }
                            
                            state.lastBackupDate?.let { date ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Último backup: ${dateFormat.format(Date(date))}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Opciones de Google Sheets
                item {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Text(
                                text = "Sincronización con Google Sheets",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = { viewModel.syncToSheets() },
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.isLoading
                                ) {
                                    if (state.isLoading && state.operationType == "sync_to_sheets") {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.Upload, contentDescription = null)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Subir a Sheets")
                                }
                                
                                Button(
                                    onClick = { viewModel.syncFromSheets() },
                                    modifier = Modifier.weight(1f),
                                    enabled = !state.isLoading
                                ) {
                                    if (state.isLoading && state.operationType == "sync_from_sheets") {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(16.dp),
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(Icons.Default.Download, contentDescription = null)
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Descargar")
                                }
                            }
                            
                            state.spreadsheetId?.let { id ->
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Spreadsheet: $id",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Lista de backups
                if (state.backupFiles.isNotEmpty()) {
                    item {
                        Text(
                            text = "Backups Disponibles",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    items(state.backupFiles) { backup ->
                        BackupFileCard(
                            backup = backup,
                            onRestore = { viewModel.restoreBackup(backup.id) },
                            onDelete = { viewModel.deleteBackup(backup.id) },
                            isLoading = state.isLoading,
                            dateFormat = dateFormat
                        )
                    }
                }
            }
        }
    }
    
    // Mostrar mensajes
    state.message?.let { message ->
        LaunchedEffect(message) {
            // Aquí podrías mostrar un SnackBar
            kotlinx.coroutines.delay(3000)
            viewModel.clearMessage()
        }
    }
    
    // Mostrar errores
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Aquí podrías mostrar un SnackBar de error
            kotlinx.coroutines.delay(5000)
            viewModel.clearError()
        }
    }
}

@Composable
fun BackupFileCard(
    backup: com.inventarioapp.data.service.BackupFile,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    isLoading: Boolean,
    dateFormat: SimpleDateFormat
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = backup.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Creado: ${dateFormat.format(Date(backup.createdTime))}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tamaño: ${formatFileSize(backup.size)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Row {
                    IconButton(
                        onClick = onRestore,
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.CloudDownload,
                            contentDescription = "Restaurar",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    IconButton(
                        onClick = onDelete,
                        enabled = !isLoading
                    ) {
                        Icon(
                            Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint = Color.Red
                        )
                    }
                }
            }
        }
    }
}

private fun formatFileSize(bytes: Long): String {
    val kb = bytes / 1024.0
    val mb = kb / 1024.0
    
    return when {
        mb >= 1 -> "%.1f MB".format(mb)
        kb >= 1 -> "%.1f KB".format(kb)
        else -> "$bytes bytes"
    }
}
