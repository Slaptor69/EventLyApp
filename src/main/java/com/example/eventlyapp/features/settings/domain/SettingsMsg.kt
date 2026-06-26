package com.example.eventlyapp.features.settings.domain

import com.example.eventlyapp.features.settings.domain.model.ThemePreference

sealed interface SettingsMsg {
    data class ThemeSelected(val themePreference: ThemePreference) : SettingsMsg
}
