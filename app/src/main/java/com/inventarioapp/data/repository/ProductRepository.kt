package com.inventarioapp.data.repository

import com.inventarioapp.data.database.dao.ProductDao
import com.inventarioapp.data.entities.Product
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductRepository @Inject constructor(
    private val productDao: ProductDao
) {
    
    fun getAllActiveProducts(): Flow<List<Product>> = productDao.getAllActiveProducts()
    
    fun getAllProducts(): Flow<List<Product>> = productDao.getAllProducts()
    
    suspend fun getProductById(id: Long): Product? = productDao.getProductById(id)
    
    suspend fun getProductByBarcode(barcode: String): Product? = productDao.getProductByBarcode(barcode)
    
    suspend fun getProductBySku(sku: String): Product? = productDao.getProductBySku(sku)
    
    fun getProductsByCategory(categoryId: Long): Flow<List<Product>> = 
        productDao.getProductsByCategory(categoryId)
    
    fun getLowStockProducts(): Flow<List<Product>> = productDao.getLowStockProducts()
    
    fun searchProducts(query: String): Flow<List<Product>> = productDao.searchProducts(query)
    
    suspend fun insertProduct(product: Product): Long = productDao.insertProduct(product)
    
    suspend fun updateProduct(product: Product) = productDao.updateProduct(product)
    
    suspend fun deleteProduct(product: Product) = productDao.deleteProduct(product)
    
    suspend fun updateStock(productId: Long, quantity: Int) = 
        productDao.updateStock(productId, quantity)
    
    suspend fun deactivateProduct(id: Long) = productDao.deactivateProduct(id)
    
    suspend fun getActiveProductCount(): Int = productDao.getActiveProductCount()
    
    suspend fun getTotalInventoryValue(): Double = productDao.getTotalInventoryValue() ?: 0.0
    
    suspend fun addProductToInventory(product: Product, quantity: Int) {
        val productId = insertProduct(product)
        updateStock(productId, quantity)
    }
    
    suspend fun removeProductFromInventory(productId: Long, quantity: Int) {
        val product = getProductById(productId)
        product?.let {
            val newStock = it.stock - quantity
            if (newStock >= 0) {
                updateStock(productId, -quantity)
            }
        }
    }
}