package com.inventarioapp.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.inventarioapp.data.entities.Product
import com.inventarioapp.data.entities.ProductImage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductCard(
    product: Product,
    productImages: List<ProductImage> = emptyList(),
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onStockUpdate: (Int) -> Unit,
    onImageClick: () -> Unit = {}
) {
    var showStockDialog by remember { mutableStateOf(false) }
    var newStock by remember { mutableStateOf(product.stock.toString()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Imagen del producto
            if (productImages.isNotEmpty()) {
                val primaryImage = productImages.find { it.isPrimary } ?: productImages.first()
                val context = LocalContext.current
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clickable { onImageClick() },
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(primaryImage.imagePath)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Imagen del producto",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = product.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    if (!product.description.isNullOrEmpty()) {
                        Text(
                            text = product.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        if (!product.barcode.isNullOrEmpty()) {
                            Text(
                                text = "Código: ${product.barcode}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (!product.sku.isNullOrEmpty()) {
                            Text(
                                text = "SKU: ${product.sku}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = "$${product.price}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Costo: $${product.cost}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Stock: ${product.stock}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    if (product.stock <= product.minStock) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = "Stock bajo",
                            tint = Color(0xFFFF9800),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = { showStockDialog = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar stock",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onEdit,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Editar producto",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Eliminar producto",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showStockDialog) {
        AlertDialog(
            onDismissRequest = { showStockDialog = false },
            title = { Text("Actualizar Stock") },
            text = {
                OutlinedTextField(
                    value = newStock,
                    onValueChange = { newStock = it },
                    label = { Text("Nuevo stock") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val stock = newStock.toIntOrNull() ?: product.stock
                        onStockUpdate(stock)
                        showStockDialog = false
                    }
                ) {
                    Text("Actualizar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStockDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}