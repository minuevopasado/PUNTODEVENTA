package com.inventarioapp.ui.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventarioapp.data.repository.ProductRepository
import com.inventarioapp.data.repository.TransactionRepository
import com.inventarioapp.data.repository.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class DashboardState(
    val todaySales: Double = 0.0,
    val totalProducts: Int = 0,
    val lowStockCount: Int = 0,
    val inventoryValue: Double = 0.0,
    val weeklySales: List<SalesData> = emptyList(),
    val categoryData: List<CategoryData> = emptyList(),
    val recentTransactions: List<TransactionData> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

data class SalesData(
    val date: String,
    val amount: Double
)

data class CategoryData(
    val category: String,
    val count: Int,
    val percentage: Float
)

data class TransactionData(
    val id: Long,
    val type: String,
    val description: String,
    val amount: Double,
    val date: Long
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val productRepository: ProductRepository,
    private val transactionRepository: TransactionRepository,
    private val expenseRepository: ExpenseRepository
) : ViewModel() {

    private val _dashboardState = MutableStateFlow(DashboardState())
    val dashboardState: StateFlow<DashboardState> = _dashboardState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun refreshData() {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            _dashboardState.value = _dashboardState.value.copy(isLoading = true)
            
            try {
                // Load today's sales
                val today = Calendar.getInstance().apply {
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                
                val tomorrow = today + (24 * 60 * 60 * 1000)
                val todaySales = transactionRepository.getTodaySales(today, tomorrow)
                
                // Load product statistics
                val totalProducts = productRepository.getActiveProductCount()
                val lowStockProducts = productRepository.getLowStockProducts().first()
                val inventoryValue = productRepository.getTotalInventoryValue()
                
                // Load weekly sales data
                val weeklySales = generateWeeklySalesData()
                
                // Load category data
                val categoryData = generateCategoryData()
                
                // Load recent transactions
                val recentTransactions = transactionRepository.getRecentTransactions(10)
                
                _dashboardState.value = DashboardState(
                    todaySales = todaySales,
                    totalProducts = totalProducts,
                    lowStockCount = lowStockProducts.size,
                    inventoryValue = inventoryValue,
                    weeklySales = weeklySales,
                    categoryData = categoryData,
                    recentTransactions = recentTransactions,
                    isLoading = false
                )
            } catch (e: Exception) {
                _dashboardState.value = _dashboardState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private suspend fun generateWeeklySalesData(): List<SalesData> {
        val calendar = Calendar.getInstance()
        val weekData = mutableListOf<SalesData>()
        val dateFormat = java.text.SimpleDateFormat("dd/MM", Locale.getDefault())
        
        for (i in 6 downTo 0) {
            calendar.add(Calendar.DAY_OF_YEAR, -i)
            val startOfDay = calendar.timeInMillis
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            val endOfDay = calendar.timeInMillis
            
            val sales = transactionRepository.getSalesByDateRange(startOfDay, endOfDay)
            weekData.add(SalesData(
                date = dateFormat.format(Date(startOfDay)),
                amount = sales
            ))
        }
        
        return weekData
    }

    private suspend fun generateCategoryData(): List<CategoryData> {
        // This would typically come from the database
        // For now, returning mock data
        return listOf(
            CategoryData("Electrónicos", 45, 30f),
            CategoryData("Ropa", 32, 22f),
            CategoryData("Hogar", 28, 19f),
            CategoryData("Deportes", 25, 17f),
            CategoryData("Otros", 18, 12f)
        )
    }
}