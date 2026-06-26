package com.example.eventlyapp.features.settings.domain

import com.example.eventlyapp.features.settings.domain.model.ThemePreference

sealed interface SettingsMsg {
    data class ThemeSelected(val themePreference: ThemePreference) : SettingsMsg
    data object ClearNewsCacheClicked : SettingsMsg
    data object ClearNewsCacheDismissed : SettingsMsg
    data object ClearNewsCacheConfirmed : SettingsMsg
    data object NewsCacheCleared : SettingsMsg
    data class NewsCacheClearFailed(val message: String) : SettingsMsg
}
