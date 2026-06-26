package com.example.eventlyapp.features.settings.data

import android.content.SharedPreferences
import com.example.eventlyapp.features.settings.domain.model.ThemePreference
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences
) {
    fun readThemePreference(): ThemePreference {
        return ThemePreference.fromStorageKey(sharedPreferences.getString(KEY_THEME, null))
    }

    fun saveThemePreference(themePreference: ThemePreference) {
        sharedPreferences.edit()
            .putString(KEY_THEME, themePreference.storageKey)
            .apply()
    }

    private companion object {
        const val KEY_THEME = "theme_preference"
    }
}
