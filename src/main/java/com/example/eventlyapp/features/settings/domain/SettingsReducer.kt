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
        }
    }
}
