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

    @Test
    fun clearNewsCacheClicked_showsConfirmationDialog() {
        val update = reducer.update(
            state = SettingsState(),
            msg = SettingsMsg.ClearNewsCacheClicked
        )

        assertEquals(true, update.state.showClearNewsCacheDialog)
        assertEquals(emptyList<SettingsCommand>(), update.commands)
        assertEquals(emptyList<SettingsEffect>(), update.effects)
    }

    @Test
    fun clearNewsCacheConfirmed_hidesDialogAndRequestsCacheCleanup() {
        val update = reducer.update(
            state = SettingsState(showClearNewsCacheDialog = true),
            msg = SettingsMsg.ClearNewsCacheConfirmed
        )

        assertEquals(false, update.state.showClearNewsCacheDialog)
        assertEquals(true, update.state.isClearingNewsCache)
        assertEquals(listOf(SettingsCommand.ClearNewsCache), update.commands)
        assertEquals(emptyList<SettingsEffect>(), update.effects)
    }

    @Test
    fun newsCacheCleared_stopsProgressAndEmitsEffect() {
        val update = reducer.update(
            state = SettingsState(isClearingNewsCache = true),
            msg = SettingsMsg.NewsCacheCleared
        )

        assertEquals(false, update.state.isClearingNewsCache)
        assertEquals(emptyList<SettingsCommand>(), update.commands)
        assertEquals(listOf(SettingsEffect.NewsCacheCleared), update.effects)
    }
}
