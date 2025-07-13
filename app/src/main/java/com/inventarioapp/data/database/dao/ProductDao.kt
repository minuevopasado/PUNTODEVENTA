package com.inventarioapp.data.database.dao

import androidx.room.*
import com.inventarioapp.data.entities.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    
    @Query("SELECT * FROM products WHERE isActive = 1 ORDER BY name")
    fun getAllActiveProducts(): Flow<List<Product>>
    
    @Query("SELECT * FROM products ORDER BY name")
    fun getAllProducts(): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE id = :id")
    suspend fun getProductById(id: Long): Product?
    
    @Query("SELECT * FROM products WHERE barcode = :barcode AND isActive = 1 LIMIT 1")
    suspend fun getProductByBarcode(barcode: String): Product?
    
    @Query("SELECT * FROM products WHERE sku = :sku AND isActive = 1 LIMIT 1")
    suspend fun getProductBySku(sku: String): Product?
    
    @Query("SELECT * FROM products WHERE categoryId = :categoryId AND isActive = 1 ORDER BY name")
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE stock <= minStock AND isActive = 1 ORDER BY stock")
    fun getLowStockProducts(): Flow<List<Product>>
    
    @Query("SELECT * FROM products WHERE name LIKE '%' || :query || '%' AND isActive = 1 ORDER BY name")
    fun searchProducts(query: String): Flow<List<Product>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product): Long
    
    @Update
    suspend fun updateProduct(product: Product)
    
    @Delete
    suspend fun deleteProduct(product: Product)
    
    @Query("UPDATE products SET stock = stock + :quantity WHERE id = :productId")
    suspend fun updateStock(productId: Long, quantity: Int)
    
    @Query("UPDATE products SET isActive = 0 WHERE id = :id")
    suspend fun deactivateProduct(id: Long)
    
    @Query("SELECT COUNT(*) FROM products WHERE isActive = 1")
    suspend fun getActiveProductCount(): Int
    
    @Query("SELECT SUM(stock * price) FROM products WHERE isActive = 1")
    suspend fun getTotalInventoryValue(): Double?
}