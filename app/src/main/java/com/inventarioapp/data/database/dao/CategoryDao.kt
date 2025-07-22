package com.inventarioapp.data.database.dao

import androidx.room.*
import com.inventarioapp.data.entities.Category
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {
    
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY name")
    fun getAllActiveCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories ORDER BY name")
    fun getAllCategories(): Flow<List<Category>>
    
    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Long): Category?
    
    @Query("SELECT * FROM categories WHERE name = :name AND isActive = 1 LIMIT 1")
    suspend fun getCategoryByName(name: String): Category?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: Category): Long
    
    @Update
    suspend fun updateCategory(category: Category)
    
    @Delete
    suspend fun deleteCategory(category: Category)
    
    @Query("UPDATE categories SET isActive = 0 WHERE id = :id")
    suspend fun deactivateCategory(id: Long)
    
    @Query("SELECT COUNT(*) FROM categories WHERE isActive = 1")
    suspend fun getActiveCategoryCount(): Int
}
