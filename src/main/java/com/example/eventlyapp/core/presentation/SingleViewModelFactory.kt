package com.example.eventlyapp.core.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

abstract class SingleViewModelFactory<T : ViewModel>(
    private val viewModelClass: Class<T>
) : ViewModelProvider.Factory {
    final override fun <R : ViewModel> create(modelClass: Class<R>): R {
        if (modelClass.isAssignableFrom(viewModelClass)) {
            @Suppress("UNCHECKED_CAST")
            return createViewModel() as R
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }

    protected abstract fun createViewModel(): T
}
