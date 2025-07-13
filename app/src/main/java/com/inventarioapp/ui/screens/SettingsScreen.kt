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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.inventarioapp.ui.theme.ThemeManager

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    themeManager: ThemeManager
) {
    var showBackupDialog by remember { mutableStateOf(false) }
    var showRestoreDialog by remember { mutableStateOf(false) }
    var currency by remember { mutableStateOf("USD") }
    var currencySymbol by remember { mutableStateOf("$") }
    var decimalPlaces by remember { mutableStateOf(2) }
    var companyName by remember { mutableStateOf("") }
    var companyAddress by remember { mutableStateOf("") }
    var companyPhone by remember { mutableStateOf("") }
    var companyEmail by remember { mutableStateOf("") }
    var taxRate by remember { mutableStateOf(0.0) }
    var lowStockThreshold by remember { mutableStateOf(10) }
    var autoBackupEnabled by remember { mutableStateOf(true) }
    var autoBackupDays by remember { mutableStateOf(7) }

    val isDarkTheme by themeManager.isDarkTheme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Configuración") }
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
                    text = "Apariencia",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            item {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = if (isDarkTheme) Icons.Default.DarkMode else Icons.Default.LightMode,
                                contentDescription = "Tema"
                            )
                            Text("Modo Oscuro")
                        }
                        Switch(
                            checked = isDarkTheme,
                            onCheckedChange = { themeManager.setDarkTheme(it) }
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Moneda y Formato",
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
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = currency,
                                onValueChange = { currency = it },
                                label = { Text("Moneda") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = currencySymbol,
                                onValueChange = { currencySymbol = it },
                                label = { Text("Símbolo") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        OutlinedTextField(
                            value = decimalPlaces.toString(),
                            onValueChange = { decimalPlaces = it.toIntOrNull() ?: 2 },
                            label = { Text("Decimales") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = taxRate.toString(),
                            onValueChange = { taxRate = it.toDoubleOrNull() ?: 0.0 },
                            label = { Text("Tasa de Impuesto (%)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Empresa",
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
                        OutlinedTextField(
                            value = companyName,
                            onValueChange = { companyName = it },
                            label = { Text("Nombre de la Empresa") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = companyAddress,
                            onValueChange = { companyAddress = it },
                            label = { Text("Dirección") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedTextField(
                                value = companyPhone,
                                onValueChange = { companyPhone = it },
                                label = { Text("Teléfono") },
                                modifier = Modifier.weight(1f)
                            )
                            OutlinedTextField(
                                value = companyEmail,
                                onValueChange = { companyEmail = it },
                                label = { Text("Email") },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Inventario",
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
                        OutlinedTextField(
                            value = lowStockThreshold.toString(),
                            onValueChange = { lowStockThreshold = it.toIntOrNull() ?: 10 },
                            label = { Text("Umbral de Stock Bajo") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            item {
                Text(
                    text = "Respaldo",
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
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Respaldo Automático")
                            Switch(
                                checked = autoBackupEnabled,
                                onCheckedChange = { autoBackupEnabled = it }
                            )
                        }
                        if (autoBackupEnabled) {
                            OutlinedTextField(
                                value = autoBackupDays.toString(),
                                onValueChange = { autoBackupDays = it.toIntOrNull() ?: 7 },
                                label = { Text("Días entre respaldos") },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Button(
                                onClick = { showBackupDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Backup,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Crear Respaldo")
                            }
                            Button(
                                onClick = { showRestoreDialog = true },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Restore,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Restaurar")
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Acerca de",
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
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "InventarioApp v1.0",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Text(
                            text = "Sistema de gestión de inventario completo",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Desarrollado con Kotlin y Jetpack Compose",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (showBackupDialog) {
            AlertDialog(
                onDismissRequest = { showBackupDialog = false },
                title = { Text("Crear Respaldo") },
                text = { Text("¿Deseas crear un respaldo completo de todos los datos?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // TODO: Implement backup functionality
                            showBackupDialog = false
                        }
                    ) {
                        Text("Crear")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showBackupDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }

        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false },
                title = { Text("Restaurar Respaldo") },
                text = { Text("¿Deseas restaurar desde un archivo de respaldo? Esta acción sobrescribirá todos los datos actuales.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            // TODO: Implement restore functionality
                            showRestoreDialog = false
                        }
                    ) {
                        Text("Restaurar")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreDialog = false }) {
                        Text("Cancelar")
                    }
                }
            )
        }
    }
}