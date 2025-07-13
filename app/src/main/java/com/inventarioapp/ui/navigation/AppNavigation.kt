package com.inventarioapp.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.inventarioapp.ui.screens.*
import com.inventarioapp.ui.theme.ThemeManager

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : Screen("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Inventory : Screen("inventory", "Inventario", Icons.Default.Inventory)
    object Sales : Screen("sales", "Ventas", Icons.Default.ShoppingCart)
    object Purchases : Screen("purchases", "Compras", Icons.Default.ShoppingBasket)
    object Expenses : Screen("expenses", "Gastos", Icons.Default.Receipt)
    object Reports : Screen("reports", "Reportes", Icons.Default.Assessment)
    object Settings : Screen("settings", "Configuración", Icons.Default.Settings)
}

val screens = listOf(
    Screen.Dashboard,
    Screen.Inventory,
    Screen.Sales,
    Screen.Purchases,
    Screen.Expenses,
    Screen.Reports,
    Screen.Settings
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(
    navController: NavHostController = rememberNavController(),
    themeManager: ThemeManager
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar {
                screens.forEach { screen ->
                    NavigationBarItem(
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        label = { Text(screen.title) },
                        selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Dashboard.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Dashboard.route) {
                DashboardScreen(navController)
            }
            composable(Screen.Inventory.route) {
                InventoryScreen(navController)
            }
            composable(Screen.Sales.route) {
                SalesScreen(navController)
            }
            composable(Screen.Purchases.route) {
                PurchasesScreen(navController)
            }
            composable(Screen.Expenses.route) {
                ExpensesScreen(navController)
            }
            composable(Screen.Reports.route) {
                ReportsScreen(navController)
            }
            composable(Screen.Settings.route) {
                SettingsScreen(navController, themeManager)
            }
        }
    }
}