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
import com.inventarioapp.ui.viewmodels.PurchasesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PurchasesScreen(
    navController: NavController,
    viewModel: PurchasesViewModel = hiltViewModel()
) {
    val purchasesState by viewModel.purchasesState.collectAsState()
    var showAddPurchase by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Compras") },
                actions = {
                    IconButton(onClick = { showAddPurchase = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Nueva compra")
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
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(purchasesState.purchases) { purchase ->
                PurchaseCard(
                    purchase = purchase,
                    onEdit = { viewModel.editPurchase(purchase) },
                    onDelete = { viewModel.deletePurchase(purchase) }
                )
            }
        }

        if (showAddPurchase) {
            AddPurchaseDialog(
                onDismiss = { showAddPurchase = false },
                onPurchaseAdded = { purchase ->
                    viewModel.addPurchase(purchase)
                    showAddPurchase = false
                }
            )
        }
    }
}

@Composable
fun PurchaseCard(
    purchase: Purchase,
    onEdit: () -> Unit,
    onDelete: () -> Unit
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
                    Text(
                        text = "Compra #${purchase.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = purchase.date,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (!purchase.notes.isNullOrEmpty()) {
                        Text(
                            text = purchase.notes,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "$${purchase.total}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${purchase.items.size} productos",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(onClick = onEdit) {
                    Icon(Icons.Default.Edit, contentDescription = "Editar")
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Eliminar")
                }
            }
        }
    }
}

data class Purchase(
    val id: Long,
    val date: String,
    val total: Double,
    val notes: String?,
    val items: List<PurchaseItem>
)

data class PurchaseItem(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val unitCost: Double,
    val totalCost: Double
)