package com.inventarioapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventarioapp.data.entities.User
import com.inventarioapp.data.entities.UserRole
import com.inventarioapp.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class UserManagementState(
    val users: List<User> = emptyList(),
    val selectedUser: User? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isAddDialogOpen: Boolean = false,
    val isEditDialogOpen: Boolean = false,
    val isDeleteDialogOpen: Boolean = false
)

@HiltViewModel
class UserManagementViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _userManagementState = MutableStateFlow(UserManagementState())
    val userManagementState: StateFlow<UserManagementState> = _userManagementState.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            _userManagementState.value = _userManagementState.value.copy(isLoading = true)
            
            try {
                userRepository.getAllUsers()
                    .collect { users ->
                        _userManagementState.value = _userManagementState.value.copy(
                            users = users,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _userManagementState.value = _userManagementState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun addUser(user: User, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                // Validar que el username y email no existan
                if (userRepository.isUsernameExists(user.username)) {
                    _userManagementState.value = _userManagementState.value.copy(
                        error = "El nombre de usuario ya existe"
                    )
                    return@launch
                }
                
                if (userRepository.isEmailExists(user.email)) {
                    _userManagementState.value = _userManagementState.value.copy(
                        error = "El email ya está registrado"
                    )
                    return@launch
                }
                
                userRepository.insertUser(user)
                _userManagementState.value = _userManagementState.value.copy(
                    isAddDialogOpen = false
                )
                onSuccess()
            } catch (e: Exception) {
                _userManagementState.value = _userManagementState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun updateUser(user: User, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                userRepository.updateUser(user)
                _userManagementState.value = _userManagementState.value.copy(
                    isEditDialogOpen = false,
                    selectedUser = null
                )
                onSuccess()
            } catch (e: Exception) {
                _userManagementState.value = _userManagementState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun deleteUser(user: User, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                userRepository.deactivateUser(user.id)
                _userManagementState.value = _userManagementState.value.copy(
                    isDeleteDialogOpen = false,
                    selectedUser = null
                )
                onSuccess()
            } catch (e: Exception) {
                _userManagementState.value = _userManagementState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun activateUser(user: User) {
        viewModelScope.launch {
            try {
                userRepository.activateUser(user.id)
            } catch (e: Exception) {
                _userManagementState.value = _userManagementState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun updateUserPassword(userId: Long, newPassword: String, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                userRepository.updateUserPassword(userId, newPassword)
                onSuccess()
            } catch (e: Exception) {
                _userManagementState.value = _userManagementState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun openAddDialog() {
        _userManagementState.value = _userManagementState.value.copy(isAddDialogOpen = true)
    }

    fun closeAddDialog() {
        _userManagementState.value = _userManagementState.value.copy(isAddDialogOpen = false)
    }

    fun openEditDialog(user: User) {
        _userManagementState.value = _userManagementState.value.copy(
            isEditDialogOpen = true,
            selectedUser = user
        )
    }

    fun closeEditDialog() {
        _userManagementState.value = _userManagementState.value.copy(
            isEditDialogOpen = false,
            selectedUser = null
        )
    }

    fun openDeleteDialog(user: User) {
        _userManagementState.value = _userManagementState.value.copy(
            isDeleteDialogOpen = true,
            selectedUser = user
        )
    }

    fun closeDeleteDialog() {
        _userManagementState.value = _userManagementState.value.copy(
            isDeleteDialogOpen = false,
            selectedUser = null
        )
    }

    fun clearError() {
        _userManagementState.value = _userManagementState.value.copy(error = null)
    }
}
