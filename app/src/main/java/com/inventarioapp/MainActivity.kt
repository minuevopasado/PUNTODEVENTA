package com.inventarioapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.inventarioapp.ui.navigation.AppNavigation
import com.inventarioapp.ui.theme.InventarioAppTheme
import com.inventarioapp.ui.theme.ThemeManager
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val themeManager = remember { ThemeManager() }
            val isDarkTheme by themeManager.isDarkTheme.collectAsState(initial = isSystemInDarkTheme())
            
            InventarioAppTheme(darkTheme = isDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    AppNavigation(navController = navController, themeManager = themeManager)
                }
            }
        }
    }
}