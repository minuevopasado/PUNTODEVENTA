package com.inventarioapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventarioapp.data.entities.Product
import com.inventarioapp.data.repository.ProductRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class InventoryState(
    val products: List<Product> = emptyList(),
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _inventoryState = MutableStateFlow(InventoryState())
    val inventoryState: StateFlow<InventoryState> = _inventoryState.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            _inventoryState.value = _inventoryState.value.copy(isLoading = true)
            
            try {
                productRepository.getAllActiveProducts()
                    .collect { products ->
                        val lowStockProducts = products.filter { it.stock <= it.minStock }
                        _inventoryState.value = InventoryState(
                            products = products,
                            totalProducts = products.size,
                            lowStockCount = lowStockProducts.size,
                            isLoading = false
                        )
                    }
            } catch (e: Exception) {
                _inventoryState.value = _inventoryState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun searchProducts(query: String) {
        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    loadProducts()
                } else {
                    productRepository.searchProducts(query)
                        .collect { products ->
                            val lowStockProducts = products.filter { it.stock <= it.minStock }
                            _inventoryState.value = _inventoryState.value.copy(
                                products = products,
                                totalProducts = products.size,
                                lowStockCount = lowStockProducts.size
                            )
                        }
                }
            } catch (e: Exception) {
                _inventoryState.value = _inventoryState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun searchByBarcode(barcode: String) {
        viewModelScope.launch {
            try {
                val product = productRepository.getProductByBarcode(barcode)
                if (product != null) {
                    _inventoryState.value = _inventoryState.value.copy(
                        products = listOf(product)
                    )
                }
            } catch (e: Exception) {
                _inventoryState.value = _inventoryState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun addProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.insertProduct(product)
                loadProducts()
            } catch (e: Exception) {
                _inventoryState.value = _inventoryState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun editProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.updateProduct(product)
                loadProducts()
            } catch (e: Exception) {
                _inventoryState.value = _inventoryState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun deleteProduct(product: Product) {
        viewModelScope.launch {
            try {
                productRepository.deleteProduct(product)
                loadProducts()
            } catch (e: Exception) {
                _inventoryState.value = _inventoryState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun updateStock(productId: Long, newStock: Int) {
        viewModelScope.launch {
            try {
                val currentProduct = _inventoryState.value.products.find { it.id == productId }
                currentProduct?.let { product ->
                    val stockDifference = newStock - product.stock
                    productRepository.updateStock(productId, stockDifference)
                    loadProducts()
                }
            } catch (e: Exception) {
                _inventoryState.value = _inventoryState.value.copy(
                    error = e.message
                )
            }
        }
    }

    fun clearError() {
        _inventoryState.value = _inventoryState.value.copy(error = null)
    }
}