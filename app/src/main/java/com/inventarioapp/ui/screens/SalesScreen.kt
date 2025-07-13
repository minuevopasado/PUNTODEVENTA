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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.inventarioapp.ui.viewmodels.SalesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesScreen(
    navController: NavController,
    viewModel: SalesViewModel = hiltViewModel()
) {
    val salesState by viewModel.salesState.collectAsState()
    var showProductSearch by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ventas") },
                actions = {
                    IconButton(onClick = { showProductSearch = true }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar producto")
                    }
                    IconButton(onClick = { viewModel.clearCart() }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpiar carrito")
                    }
                }
            )
        },
        bottomBar = {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
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
                            text = "Total: $${salesState.total}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${salesState.cartItems.size} productos",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Button(
                        onClick = { viewModel.completeSale() },
                        enabled = salesState.cartItems.isNotEmpty()
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Completar Venta")
                    }
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(salesState.cartItems) { cartItem ->
                CartItemCard(
                    cartItem = cartItem,
                    onQuantityChange = { quantity -> viewModel.updateQuantity(cartItem.product.id, quantity) },
                    onRemove = { viewModel.removeFromCart(cartItem.product.id) }
                )
            }
        }

        if (showProductSearch) {
            ProductSearchDialog(
                onDismiss = { showProductSearch = false },
                onProductSelected = { product ->
                    viewModel.addToCart(product)
                    showProductSearch = false
                }
            )
        }
    }
}

@Composable
fun CartItemCard(
    cartItem: CartItem,
    onQuantityChange: (Int) -> Unit,
    onRemove: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = cartItem.product.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$${cartItem.product.price} c/u",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Subtotal: $${cartItem.subtotal}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                IconButton(
                    onClick = { if (cartItem.quantity > 1) onQuantityChange(cartItem.quantity - 1) }
                ) {
                    Icon(Icons.Default.Remove, contentDescription = "Reducir")
                }
                Text(
                    text = "${cartItem.quantity}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )
                IconButton(
                    onClick = { onQuantityChange(cartItem.quantity + 1) }
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Aumentar")
                }
                IconButton(onClick = onRemove) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

data class CartItem(
    val product: Product,
    val quantity: Int,
    val subtotal: Double = product.price * quantity
)