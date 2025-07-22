package com.inventarioapp.ui.components

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inventarioapp.data.entities.Document
import com.inventarioapp.data.entities.DocumentType
import com.inventarioapp.data.service.FileService
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentManager(
    documents: List<Document>,
    onDocumentAdded: (Document) -> Unit,
    onDocumentDeleted: (Document) -> Unit,
    fileService: FileService,
    modifier: Modifier = Modifier
) {
    var showAddDocument by remember { mutableStateOf(false) }
    var selectedDocumentType by remember { mutableStateOf(DocumentType.OTHER) }
    
    val context = LocalContext.current
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    
    val documentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val document = fileService.saveDocumentFromUri(
                    selectedUri,
                    selectedDocumentType
                )
                onDocumentAdded(document)
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
    
    Column(modifier = modifier) {
        // Header con botón agregar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Documentos (${documents.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Button(
                onClick = { showAddDocument = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Agregar")
            }
        }
        
        // Lista de documentos
        if (documents.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Description,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No hay documentos",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(documents) { document ->
                    DocumentCard(
                        document = document,
                        onView = {
                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                data = fileService.getDocumentUri(document)
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(intent)
                        },
                        onShare = {
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = document.mimeType
                                putExtra(Intent.EXTRA_STREAM, fileService.getDocumentUri(document))
                                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                            }
                            context.startActivity(Intent.createChooser(intent, "Compartir documento"))
                        },
                        onDelete = {
                            fileService.deleteDocument(document)
                            onDocumentDeleted(document)
                        },
                        dateFormat = dateFormat
                    )
                }
            }
        }
    }
    
    // Dialog para agregar documento
    if (showAddDocument) {
        AlertDialog(
            onDismissRequest = { showAddDocument = false },
            title = { Text("Agregar Documento") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "Tipo de documento:",
                        style = MaterialTheme.typography.titleSmall
                    )
                    
                    // Opciones de tipo de documento
                    DocumentType.values().forEach { docType ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedDocumentType = docType
                                }
                                .padding(vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDocumentType == docType,
                                onClick = { selectedDocumentType = docType }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = when (docType) {
                                    DocumentType.RECEIPT -> "Recibo"
                                    DocumentType.INVOICE -> "Factura"
                                    DocumentType.EXPENSE_RECEIPT -> "Recibo de Gasto"
                                    DocumentType.REPORT -> "Reporte"
                                    DocumentType.CONTRACT -> "Contrato"
                                    DocumentType.OTHER -> "Otro"
                                }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        documentLauncher.launch("*/*")
                        showAddDocument = false
                    }
                ) {
                    Text("Seleccionar Archivo")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDocument = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun DocumentCard(
    document: Document,
    onView: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit,
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
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = when (document.documentType) {
                                DocumentType.RECEIPT -> Icons.Default.Receipt
                                DocumentType.INVOICE -> Icons.Default.Description
                                DocumentType.EXPENSE_RECEIPT -> Icons.Default.Receipt
                                DocumentType.REPORT -> Icons.Default.Assessment
                                DocumentType.CONTRACT -> Icons.Default.Description
                                DocumentType.OTHER -> Icons.Default.AttachFile
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        
                        Text(
                            text = document.fileName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Medium
                        )
                        
                        if (document.isGenerated) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondary,
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "GENERADO",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = dateFormat.format(Date(document.createdAt)),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    if (!document.description.isNullOrEmpty()) {
                        Text(
                            text = document.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = "Tamaño: ${formatFileSize(document.fileSize)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        IconButton(
                            onClick = onView,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Visibility,
                                contentDescription = "Ver documento",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = "Compartir documento",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        
                        IconButton(
                            onClick = onDelete,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Eliminar documento",
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatFileSize(size: Long): String {
    return when {
        size < 1024 -> "$size B"
        size < 1024 * 1024 -> "${size / 1024} KB"
        size < 1024 * 1024 * 1024 -> "${size / (1024 * 1024)} MB"
        else -> "${size / (1024 * 1024 * 1024)} GB"
    }
}