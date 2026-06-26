package com.example.eventlyapp.features.settings.domain

import com.example.eventlyapp.core.elm.ElmUpdate
import com.example.eventlyapp.core.elm.toElmUpdate
import com.example.eventlyapp.features.settings.domain.model.SettingsState
import javax.inject.Inject

class SettingsReducer @Inject constructor() {
    fun update(
        state: SettingsState,
        msg: SettingsMsg
    ): ElmUpdate<SettingsState, SettingsCommand, SettingsEffect> {
        return when (msg) {
            is SettingsMsg.ThemeSelected -> state.copy(
                themePreference = msg.themePreference
            ).toElmUpdate(
                commands = listOf(SettingsCommand.SaveThemePreference(msg.themePreference)),
                effects = listOf(SettingsEffect.ThemeApplied(msg.themePreference))
            )
            SettingsMsg.ClearNewsCacheClicked -> state.copy(
                showClearNewsCacheDialog = true
            ).toElmUpdate()
            SettingsMsg.ClearNewsCacheDismissed -> state.copy(
                showClearNewsCacheDialog = false
            ).toElmUpdate()
            SettingsMsg.ClearNewsCacheConfirmed -> state.copy(
                showClearNewsCacheDialog = false,
                isClearingNewsCache = true
            ).toElmUpdate(
                commands = listOf(SettingsCommand.ClearNewsCache)
            )
            SettingsMsg.NewsCacheCleared -> state.copy(
                isClearingNewsCache = false
            ).toElmUpdate(
                effects = listOf(SettingsEffect.NewsCacheCleared)
            )
            is SettingsMsg.NewsCacheClearFailed -> state.copy(
                isClearingNewsCache = false
            ).toElmUpdate(
                effects = listOf(SettingsEffect.NewsCacheClearFailed(msg.message))
            )
        }
    }
}
