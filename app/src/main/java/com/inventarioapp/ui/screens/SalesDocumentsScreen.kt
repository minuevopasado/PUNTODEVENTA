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
import com.inventarioapp.ui.viewmodels.SalesDocumentsViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SalesDocumentsScreen(
    navController: NavController,
    viewModel: SalesDocumentsViewModel = hiltViewModel()
) {
    val state by viewModel.salesDocumentsState.collectAsState()
    val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
    var selectedFilter by remember { mutableStateOf("all") }
    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Documentos de Ventas") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshDocuments() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Actualizar")
                    }
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.DateRange, contentDescription = "Filtrar por fecha")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Filtros
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Filtros",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            onClick = { 
                                selectedFilter = "all"
                                viewModel.filterDocuments("all")
                            },
                            label = { Text("Todos") },
                            selected = selectedFilter == "all"
                        )
                        FilterChip(
                            onClick = { 
                                selectedFilter = "today"
                                viewModel.filterDocuments("today")
                            },
                            label = { Text("Hoy") },
                            selected = selectedFilter == "today"
                        )
                        FilterChip(
                            onClick = { 
                                selectedFilter = "week"
                                viewModel.filterDocuments("week")
                            },
                            label = { Text("Esta semana") },
                            selected = selectedFilter == "week"
                        )
                        FilterChip(
                            onClick = { 
                                selectedFilter = "month"
                                viewModel.filterDocuments("month")
                            },
                            label = { Text("Este mes") },
                            selected = selectedFilter == "month"
                        )
                    }
                }
            }
            
            // Estadísticas rápidas
            if (state.transactions.isNotEmpty()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.transactions.size}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Ventas",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${state.documentsGenerated}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Text(
                                text = "Documentos",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$${String.format("%.2f", state.totalSales)}",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF4CAF50)
                            )
                            Text(
                                text = "Total",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Lista de transacciones
            if (state.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (state.transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No hay documentos de ventas para mostrar",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.transactions) { transactionWithItems ->
                        SalesTransactionCard(
                            transactionWithItems = transactionWithItems,
                            onViewDocument = { transaction ->
                                viewModel.viewDocument(transaction.id)
                            },
                            onRegenerateDocument = { transaction ->
                                viewModel.regenerateDocument(transaction)
                            },
                            onPrintDocument = { transaction ->
                                viewModel.printDocument(transaction.id)
                            },
                            dateFormat = dateFormat
                        )
                    }
                }
            }
        }
    }
    
    // Mostrar errores
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Aquí podrías mostrar un SnackBar
            kotlinx.coroutines.delay(3000)
            viewModel.clearError()
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
}

@Composable
fun SalesTransactionCard(
    transactionWithItems: com.inventarioapp.ui.viewmodels.TransactionWithItems,
    onViewDocument: (com.inventarioapp.data.entities.Transaction) -> Unit,
    onRegenerateDocument: (com.inventarioapp.data.entities.Transaction) -> Unit,
    onPrintDocument: (com.inventarioapp.data.entities.Transaction) -> Unit,
    dateFormat: SimpleDateFormat
) {
    val transaction = transactionWithItems.transaction
    val items = transactionWithItems.items
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Encabezado de la transacción
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Venta #${transaction.id}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = dateFormat.format(Date(transaction.date)),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    transaction.reference?.let { ref ->
                        Text(
                            text = "Ref: $ref",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Text(
                    text = "$${String.format("%.2f", transaction.total)}",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4CAF50)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Items de la transacción
            if (items.isNotEmpty()) {
                Text(
                    text = "Productos (${items.size}):",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                
                items.take(3).forEach { item ->
                    Text(
                        text = "• ${item.productName} x${item.quantity} = $${String.format("%.2f", item.totalPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                if (items.size > 3) {
                    Text(
                        text = "... y ${items.size - 3} más",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            // Acciones
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = { onViewDocument(transaction) }) {
                    Icon(Icons.Default.Visibility, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ver")
                }
                
                TextButton(onClick = { onRegenerateDocument(transaction) }) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Regenerar")
                }
                
                TextButton(onClick = { onPrintDocument(transaction) }) {
                    Icon(Icons.Default.Print, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Imprimir")
                }
            }
        }
    }
}
