package com.inventarioapp.data.database.dao

import androidx.room.*
import com.inventarioapp.data.entities.ProductImage
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductImageDao {
    
    @Query("SELECT * FROM product_images WHERE productId = :productId ORDER BY isPrimary DESC, createdAt ASC")
    fun getImagesByProduct(productId: Long): Flow<List<ProductImage>>
    
    @Query("SELECT * FROM product_images WHERE productId = :productId AND isPrimary = 1 LIMIT 1")
    suspend fun getPrimaryImage(productId: Long): ProductImage?
    
    @Query("SELECT * FROM product_images WHERE id = :id")
    suspend fun getImageById(id: Long): ProductImage?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertImage(image: ProductImage): Long
    
    @Update
    suspend fun updateImage(image: ProductImage)
    
    @Delete
    suspend fun deleteImage(image: ProductImage)
    
    @Query("DELETE FROM product_images WHERE productId = :productId")
    suspend fun deleteImagesByProduct(productId: Long)
    
    @Query("UPDATE product_images SET isPrimary = 0 WHERE productId = :productId")
    suspend fun clearPrimaryImage(productId: Long)
    
    @Query("UPDATE product_images SET isPrimary = 1 WHERE id = :imageId")
    suspend fun setPrimaryImage(imageId: Long)
    
    @Query("SELECT COUNT(*) FROM product_images WHERE productId = :productId")
    suspend fun getImageCount(productId: Long): Int
}