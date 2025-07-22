package com.inventarioapp.data.repository

import com.inventarioapp.data.database.dao.UserDao
import com.inventarioapp.data.entities.User
import com.inventarioapp.data.entities.UserRole
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val userDao: UserDao
) {
    
    fun getAllUsers(): Flow<List<User>> = userDao.getAllUsers()
    
    fun getAllActiveUsers(): Flow<List<User>> = userDao.getAllActiveUsers()
    
    suspend fun getUserById(id: Long): User? = userDao.getUserById(id)
    
    suspend fun getUserByUsername(username: String): User? = userDao.getUserByUsername(username)
    
    suspend fun getUserByEmail(email: String): User? = userDao.getUserByEmail(email)
    
    fun getUsersByRole(role: UserRole): Flow<List<User>> = userDao.getUsersByRole(role)
    
    suspend fun validateLogin(username: String, password: String): User? {
        val hashedPassword = hashPassword(password)
        return userDao.validateLogin(username, hashedPassword)
    }
    
    suspend fun insertUser(user: User): Long {
        val hashedPassword = hashPassword(user.password)
        val userWithHashedPassword = user.copy(password = hashedPassword)
        return userDao.insertUser(userWithHashedPassword)
    }
    
    suspend fun updateUser(user: User) {
        userDao.updateUser(user)
    }
    
    suspend fun updateUserPassword(userId: Long, newPassword: String) {
        val hashedPassword = hashPassword(newPassword)
        userDao.updatePassword(userId, hashedPassword)
    }
    
    suspend fun deleteUser(user: User) = userDao.deleteUser(user)
    
    suspend fun deactivateUser(id: Long) = userDao.deactivateUser(id)
    
    suspend fun activateUser(id: Long) = userDao.activateUser(id)
    
    suspend fun getActiveUserCount(): Int = userDao.getActiveUserCount()
    
    suspend fun isUsernameExists(username: String): Boolean = userDao.isUsernameExists(username)
    
    suspend fun isEmailExists(email: String): Boolean = userDao.isEmailExists(email)
    
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    suspend fun createDefaultAdmin() {
        val existingAdmin = userDao.getUsersByRole(UserRole.ADMIN).firstOrNull()
        if (existingAdmin == null) {
            val defaultAdmin = User(
                username = "admin",
                email = "admin@inventarioapp.com",
                password = "admin123", // Se hasheará automáticamente
                role = UserRole.ADMIN,
                isActive = true
            )
            insertUser(defaultAdmin)
        }
    }
}
