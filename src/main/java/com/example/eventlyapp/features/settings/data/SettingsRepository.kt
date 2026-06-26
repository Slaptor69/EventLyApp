package com.example.eventlyapp.features.settings.data

import android.content.SharedPreferences
import com.example.eventlyapp.features.news.data.cache.NewsImageCacheService
import com.example.eventlyapp.features.news.data.cache.NewsMetadataCacheService
import com.example.eventlyapp.features.settings.domain.model.ThemePreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class SettingsRepository @Inject constructor(
    private val sharedPreferences: SharedPreferences,
    private val newsMetadataCacheService: NewsMetadataCacheService,
    private val newsImageCacheService: NewsImageCacheService
) {
    fun readThemePreference(): ThemePreference {
        return ThemePreference.fromStorageKey(sharedPreferences.getString(KEY_THEME, null))
    }

    fun saveThemePreference(themePreference: ThemePreference) {
        sharedPreferences.edit()
            .putString(KEY_THEME, themePreference.storageKey)
            .apply()
    }

    suspend fun clearNewsCache() = withContext(Dispatchers.IO) {
        newsMetadataCacheService.clearAll()
        newsImageCacheService.clearAllImages()
    }

    private companion object {
        const val KEY_THEME = "theme_preference"
    }
}
