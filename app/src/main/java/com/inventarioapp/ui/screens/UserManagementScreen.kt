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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.inventarioapp.data.entities.User
import com.inventarioapp.data.entities.UserRole
import com.inventarioapp.ui.viewmodels.UserManagementViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserManagementScreen(
    navController: NavController,
    viewModel: UserManagementViewModel = hiltViewModel()
) {
    val state by viewModel.userManagementState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gestión de Usuarios") },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.openAddDialog() }) {
                        Icon(Icons.Default.Add, contentDescription = "Agregar Usuario")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.users) { user ->
                    UserCard(
                        user = user,
                        onEdit = { viewModel.openEditDialog(user) },
                        onDelete = { viewModel.openDeleteDialog(user) },
                        onToggleActive = { 
                            if (user.isActive) {
                                viewModel.openDeleteDialog(user)
                            } else {
                                viewModel.activateUser(user)
                            }
                        }
                    )
                }
            }

            if (state.isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }

    // Diálogos
    if (state.isAddDialogOpen) {
        AddUserDialog(
            onDismiss = { viewModel.closeAddDialog() },
            onConfirm = { user ->
                viewModel.addUser(user) {
                    // Usuario agregado exitosamente
                }
            }
        )
    }

    if (state.isEditDialogOpen && state.selectedUser != null) {
        EditUserDialog(
            user = state.selectedUser!!,
            onDismiss = { viewModel.closeEditDialog() },
            onConfirm = { user ->
                viewModel.updateUser(user) {
                    // Usuario actualizado exitosamente
                }
            }
        )
    }

    if (state.isDeleteDialogOpen && state.selectedUser != null) {
        AlertDialog(
            onDismissRequest = { viewModel.closeDeleteDialog() },
            title = { Text("Confirmar acción") },
            text = { 
                Text(
                    if (state.selectedUser!!.isActive) 
                        "¿Desactivar usuario ${state.selectedUser!!.username}?" 
                    else 
                        "¿Activar usuario ${state.selectedUser!!.username}?"
                ) 
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteUser(state.selectedUser!!) {
                            // Usuario desactivado
                        }
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.closeDeleteDialog() }) {
                    Text("Cancelar")
                }
            }
        )
    }

    // Mostrar errores
    state.error?.let { error ->
        LaunchedEffect(error) {
            // Aquí podrías mostrar un SnackBar
            viewModel.clearError()
        }
    }
}

@Composable
fun UserCard(
    user: User,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onToggleActive: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
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
                        text = user.username,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = user.email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = user.role.name,
                        style = MaterialTheme.typography.bodySmall,
                        color = when (user.role) {
                            UserRole.ADMIN -> Color.Red
                            UserRole.MANAGER -> Color.Blue
                            UserRole.EMPLOYEE -> Color.Green
                            UserRole.VIEWER -> Color.Gray
                        }
                    )
                }
                
                Row {
                    IconButton(onClick = onEdit) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    IconButton(onClick = onToggleActive) {
                        Icon(
                            if (user.isActive) Icons.Default.PersonOff else Icons.Default.Person,
                            contentDescription = if (user.isActive) "Desactivar" else "Activar"
                        )
                    }
                }
            }
            
            if (!user.isActive) {
                Text(
                    text = "INACTIVO",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Red,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Composable
fun AddUserDialog(
    onDismiss: () -> Unit,
    onConfirm: (User) -> Unit
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var selectedRole by remember { mutableStateOf(UserRole.EMPLOYEE) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Agregar Usuario") },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de Usuario") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Contraseña") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedRole.name,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = {
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (username.isNotBlank() && email.isNotBlank() && password.isNotBlank()) {
                        onConfirm(
                            User(
                                username = username.trim(),
                                email = email.trim(),
                                password = password,
                                role = selectedRole
                            )
                        )
                    }
                }
            ) {
                Text("Agregar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}

@Composable
fun EditUserDialog(
    user: User,
    onDismiss: () -> Unit,
    onConfirm: (User) -> Unit
) {
    var username by remember { mutableStateOf(user.username) }
    var email by remember { mutableStateOf(user.email) }
    var selectedRole by remember { mutableStateOf(user.role) }
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Editar Usuario") },
        text = {
            Column {
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Nombre de Usuario") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = selectedRole.name,
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("Rol") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        UserRole.values().forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role.name) },
                                onClick = {
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (username.isNotBlank() && email.isNotBlank()) {
                        onConfirm(
                            user.copy(
                                username = username.trim(),
                                email = email.trim(),
                                role = selectedRole,
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    }
                }
            ) {
                Text("Actualizar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
