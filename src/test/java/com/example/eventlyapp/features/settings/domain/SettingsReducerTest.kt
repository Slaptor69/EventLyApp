package com.example.eventlyapp.features.settings.domain

import com.example.eventlyapp.features.settings.domain.model.SettingsState
import com.example.eventlyapp.features.settings.domain.model.ThemePreference
import org.junit.Assert.assertEquals
import org.junit.Test

class SettingsReducerTest {
    private val reducer = SettingsReducer()

    @Test
    fun themeSelected_updatesStateAndRequestsPersistence() {
        val update = reducer.update(
            state = SettingsState(themePreference = ThemePreference.LIGHT),
            msg = SettingsMsg.ThemeSelected(ThemePreference.DARK)
        )

        assertEquals(ThemePreference.DARK, update.state.themePreference)
        assertEquals(
            listOf(SettingsCommand.SaveThemePreference(ThemePreference.DARK)),
            update.commands
        )
        assertEquals(
            listOf(SettingsEffect.ThemeApplied(ThemePreference.DARK)),
            update.effects
        )
    }
}
