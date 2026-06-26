package com.example.eventlyapp.features.settings.domain

import com.example.eventlyapp.features.settings.domain.model.ThemePreference

sealed interface SettingsEffect {
    data class ThemeApplied(val themePreference: ThemePreference) : SettingsEffect
    data object NewsCacheCleared : SettingsEffect
    data class NewsCacheClearFailed(val message: String) : SettingsEffect
}
