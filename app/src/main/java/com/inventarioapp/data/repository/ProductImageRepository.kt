package com.inventarioapp.data.repository

import android.graphics.Bitmap
import android.net.Uri
import com.inventarioapp.data.database.dao.ProductImageDao
import com.inventarioapp.data.entities.ProductImage
import com.inventarioapp.data.service.FileService
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProductImageRepository @Inject constructor(
    private val productImageDao: ProductImageDao,
    private val fileService: FileService
) {
    
    fun getImagesByProduct(productId: Long): Flow<List<ProductImage>> =
        productImageDao.getImagesByProduct(productId)
    
    suspend fun getPrimaryImage(productId: Long): ProductImage? =
        productImageDao.getPrimaryImage(productId)
    
    suspend fun getImageById(id: Long): ProductImage? =
        productImageDao.getImageById(id)
    
    suspend fun insertImage(image: ProductImage): Long =
        productImageDao.insertImage(image)
    
    suspend fun updateImage(image: ProductImage) =
        productImageDao.updateImage(image)
    
    suspend fun deleteImage(image: ProductImage) {
        fileService.deleteProductImage(image.imagePath)
        productImageDao.deleteImage(image)
    }
    
    suspend fun deleteImagesByProduct(productId: Long) {
        val images = productImageDao.getImagesByProduct(productId).first()
        images.forEach { image ->
            fileService.deleteProductImage(image.imagePath)
        }
        productImageDao.deleteImagesByProduct(productId)
    }
    
    suspend fun setPrimaryImage(imageId: Long) {
        val image = productImageDao.getImageById(imageId)
        image?.let {
            productImageDao.clearPrimaryImage(it.productId)
            productImageDao.setPrimaryImage(imageId)
        }
    }
    
    suspend fun saveProductImage(bitmap: Bitmap, productId: Long, isPrimary: Boolean = false): ProductImage {
        val productImage = fileService.saveProductImage(bitmap, productId, isPrimary)
        val imageId = productImageDao.insertImage(productImage)
        return productImage.copy(id = imageId)
    }
    
    suspend fun saveProductImageFromUri(uri: Uri, productId: Long, isPrimary: Boolean = false): ProductImage {
        val productImage = fileService.saveProductImageFromUri(uri, productId, isPrimary)
        val imageId = productImageDao.insertImage(productImage)
        return productImage.copy(id = imageId)
    }
    
    suspend fun loadProductImage(imagePath: String): Bitmap? =
        fileService.loadProductImage(imagePath)
    
    suspend fun getImageCount(productId: Long): Int =
        productImageDao.getImageCount(productId)
}