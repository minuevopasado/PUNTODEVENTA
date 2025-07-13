package com.inventarioapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import com.inventarioapp.ui.viewmodels.ReportsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportsScreen(
    navController: NavController,
    viewModel: ReportsViewModel = hiltViewModel()
) {
    val reportsState by viewModel.reportsState.collectAsState()
    var selectedReportType by remember { mutableStateOf("sales") }
    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Reportes") },
                actions = {
                    IconButton(onClick = { viewModel.exportReport(selectedReportType, startDate, endDate) }) {
                        Icon(Icons.Default.Download, contentDescription = "Exportar")
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
            item {
                Text(
                    text = "Configuración del Reporte",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Tipo de Reporte",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = selectedReportType == "sales",
                                onClick = { selectedReportType = "sales" },
                                label = { Text("Ventas") }
                            )
                            FilterChip(
                                selected = selectedReportType == "inventory",
                                onClick = { selectedReportType = "inventory" },
                                label = { Text("Inventario") }
                            )
                            FilterChip(
                                selected = selectedReportType == "expenses",
                                onClick = { selectedReportType = "expenses" },
                                label = { Text("Gastos") }
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = startDate,
                                onValueChange = { startDate = it },
                                label = { Text("Fecha Inicio") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = endDate,
                                onValueChange = { endDate = it },
                                label = { Text("Fecha Fin") },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { viewModel.generateReport(selectedReportType, startDate, endDate) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Assessment, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Generar")
                            }
                            Button(
                                onClick = { viewModel.exportReport(selectedReportType, startDate, endDate) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Download, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Exportar")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Resumen",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Ventas")
                            Text(
                                "$${reportsState.totalSales}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total Gastos")
                            Text(
                                "$${reportsState.totalExpenses}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Ganancia Neta")
                            Text(
                                "$${reportsState.netProfit}",
                                fontWeight = FontWeight.Bold,
                                color = if (reportsState.netProfit >= 0) 
                                    MaterialTheme.colorScheme.primary 
                                else MaterialTheme.colorScheme.error
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Productos Vendidos")
                            Text(
                                "${reportsState.productsSold}",
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Gráficos",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Ventas por Período",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        // TODO: Add chart component here
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("Gráfico de ventas")
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(
    selected: Boolean,
    onClick: () -> Unit,
    label: @Composable () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = label
    )
}