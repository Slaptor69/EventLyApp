package com.example.eventlyapp.features.settings.domain.model

enum class ThemePreference(
    val storageKey: String,
    val title: String
) {
    LIGHT("light", "Светлая"),
    DARK("dark", "Темная");

    companion object {
        fun fromStorageKey(value: String?): ThemePreference {
            return entries.firstOrNull { theme -> theme.storageKey == value } ?: LIGHT
        }
    }
}
