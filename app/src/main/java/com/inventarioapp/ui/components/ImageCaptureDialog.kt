package com.inventarioapp.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.inventarioapp.data.entities.ProductImage
import com.inventarioapp.data.service.FileService

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCaptureDialog(
    productId: Long,
    currentImages: List<ProductImage>,
    onImagesUpdated: (List<ProductImage>) -> Unit,
    onDismiss: () -> Unit,
    fileService: FileService
) {
    var selectedImages by remember { mutableStateOf(currentImages) }
    var showImageOptions by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            // La imagen se guardó, actualizar la lista
            // Aquí se procesaría la imagen capturada
        }
    }
    
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { selectedUri ->
            try {
                val productImage = fileService.saveProductImageFromUri(
                    selectedUri, 
                    productId, 
                    selectedImages.isEmpty()
                )
                selectedImages = selectedImages + productImage
                onImagesUpdated(selectedImages)
            } catch (e: Exception) {
                // Manejar error
            }
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Imágenes del Producto") },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Imagen principal
                if (selectedImages.isNotEmpty()) {
                    val primaryImage = selectedImages.find { it.isPrimary } ?: selectedImages.first()
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize()
                        ) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(primaryImage.imagePath)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Imagen principal",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                            
                            if (primaryImage.isPrimary) {
                                Surface(
                                    modifier = Modifier
                                        .padding(8.dp)
                                        .align(Alignment.TopStart),
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = "PRINCIPAL",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
                
                // Lista de imágenes
                if (selectedImages.size > 1) {
                    Text(
                        text = "Todas las imágenes",
                        style = MaterialTheme.typography.titleSmall
                    )
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedImages) { image ->
                            Card(
                                modifier = Modifier
                                    .size(80.dp)
                                    .clickable {
                                        // Hacer esta imagen la principal
                                        val updatedImages = selectedImages.map { 
                                            it.copy(isPrimary = it.id == image.id) 
                                        }
                                        selectedImages = updatedImages
                                        onImagesUpdated(updatedImages)
                                    },
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Box(
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(image.imagePath)
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = "Imagen del producto",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    
                                    if (image.isPrimary) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Imagen principal",
                                            tint = Color.Yellow,
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(4.dp)
                                                .size(16.dp)
                                        )
                                    }
                                    
                                    // Botón eliminar
                                    IconButton(
                                        onClick = {
                                            val updatedImages = selectedImages.filter { it.id != image.id }
                                            selectedImages = updatedImages
                                            onImagesUpdated(updatedImages)
                                            fileService.deleteProductImage(image.imagePath)
                                        },
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .size(24.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar imagen",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                
                // Botones de acción
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Galería")
                    }
                    
                    Button(
                        onClick = { 
                            // Aquí se abriría la cámara
                            showImageOptions = true
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Cámara")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar")
            }
        }
    )
    
    if (showImageOptions) {
        AlertDialog(
            onDismissRequest = { showImageOptions = false },
            title = { Text("Capturar imagen") },
            text = { Text("¿Deseas abrir la cámara para tomar una foto?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showImageOptions = false
                        // Aquí se abriría la cámara
                    }
                ) {
                    Text("Abrir Cámara")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImageOptions = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}