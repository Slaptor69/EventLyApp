package com.example.eventlyapp.features.settings.presentation

import androidx.lifecycle.ViewModel
import com.example.eventlyapp.core.presentation.SingleViewModelFactory
import com.example.eventlyapp.features.settings.data.SettingsRepository
import com.example.eventlyapp.features.settings.domain.SettingsCommand
import com.example.eventlyapp.features.settings.domain.SettingsEffect
import com.example.eventlyapp.features.settings.domain.SettingsMsg
import com.example.eventlyapp.features.settings.domain.SettingsReducer
import com.example.eventlyapp.features.settings.domain.model.SettingsState
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import javax.inject.Inject

class SettingsViewModel(
    private val repository: SettingsRepository,
    private val reducer: SettingsReducer
) : ViewModel() {
    private val _state = MutableStateFlow(
        SettingsState(themePreference = repository.readThemePreference())
    )
    val state: StateFlow<SettingsState> = _state.asStateFlow()

    private val _effects = MutableSharedFlow<SettingsEffect>(extraBufferCapacity = 1)
    val effects: SharedFlow<SettingsEffect> = _effects

    fun accept(msg: SettingsMsg) {
        val update = reducer.update(_state.value, msg)
        _state.value = update.state
        update.commands.forEach { command -> execute(command) }
        update.effects.forEach { effect -> _effects.tryEmit(effect) }
    }

    private fun execute(command: SettingsCommand) {
        when (command) {
            is SettingsCommand.SaveThemePreference -> {
                repository.saveThemePreference(command.themePreference)
            }
            SettingsCommand.ClearNewsCache -> {
                viewModelScope.launch {
                    runCatching {
                        repository.clearNewsCache()
                    }.onSuccess {
                        accept(SettingsMsg.NewsCacheCleared)
                    }.onFailure { throwable ->
                        accept(
                            SettingsMsg.NewsCacheClearFailed(
                                throwable.message ?: "Не удалось очистить кэш новостей"
                            )
                        )
                    }
                }
            }
        }
    }
}

class SettingsViewModelFactory @Inject constructor(
    private val repository: SettingsRepository,
    private val reducer: SettingsReducer
) : SingleViewModelFactory<SettingsViewModel>(SettingsViewModel::class.java) {
    override fun createViewModel(): SettingsViewModel = SettingsViewModel(repository, reducer)
}
