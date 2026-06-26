package com.example.eventlyapp.features.settings.domain

import com.example.eventlyapp.features.settings.domain.model.ThemePreference

sealed interface SettingsCommand {
    data class SaveThemePreference(val themePreference: ThemePreference) : SettingsCommand
    data object ClearNewsCache : SettingsCommand
}
