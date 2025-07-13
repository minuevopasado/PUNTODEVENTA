package com.inventarioapp.ui.theme

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ThemeManager @Inject constructor() {
    
    private val _isDarkTheme = MutableStateFlow(false)
    val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()
    
    fun toggleTheme() {
        _isDarkTheme.value = !_isDarkTheme.value
    }
    
    fun setDarkTheme(isDark: Boolean) {
        _isDarkTheme.value = isDark
    }
}