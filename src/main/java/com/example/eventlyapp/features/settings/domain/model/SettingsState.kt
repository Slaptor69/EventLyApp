package com.example.eventlyapp.features.settings.domain.model

data class SettingsState(
    val themePreference: ThemePreference = ThemePreference.LIGHT,
    val showClearNewsCacheDialog: Boolean = false,
    val isClearingNewsCache: Boolean = false
)
