package com.example.eventlyapp.features.settings.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class ThemePreferenceTest {
    @Test
    fun fromStorageKey_returnsSavedTheme() {
        assertEquals(ThemePreference.DARK, ThemePreference.fromStorageKey("dark"))
    }

    @Test
    fun fromStorageKey_fallsBackToLightForUnknownValue() {
        assertEquals(ThemePreference.LIGHT, ThemePreference.fromStorageKey("system"))
        assertEquals(ThemePreference.LIGHT, ThemePreference.fromStorageKey(null))
    }
}
