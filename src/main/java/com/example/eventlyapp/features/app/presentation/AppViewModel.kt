package com.example.eventlyapp.features.app.presentation

import androidx.lifecycle.ViewModel
import com.example.eventlyapp.core.presentation.SingleViewModelFactory
import com.example.eventlyapp.features.app.domain.AppMsg
import com.example.eventlyapp.features.app.domain.AppReducer
import com.example.eventlyapp.features.app.domain.AppState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class AppViewModel(
    private val reducer: AppReducer
) : ViewModel() {
    private val _state = MutableStateFlow(AppState())
    val state: StateFlow<AppState> = _state.asStateFlow()

    fun accept(msg: AppMsg) {
        val update = reducer.update(_state.value, msg)
        _state.value = update.state
    }
}

class AppViewModelFactory @Inject constructor(
    private val reducer: AppReducer
) : SingleViewModelFactory<AppViewModel>(AppViewModel::class.java) {
    override fun createViewModel(): AppViewModel = AppViewModel(reducer)
}
