package com.example.eventlyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.eventlyapp.features.app.presentation.EventlyAppRoot
import com.example.eventlyapp.features.app.presentation.AppViewModel
import com.example.eventlyapp.features.app.presentation.PlannerViewModel
import com.example.eventlyapp.features.news.presentation.NewsViewModel
import com.example.eventlyapp.features.settings.domain.model.ThemePreference
import com.example.eventlyapp.features.settings.presentation.SettingsViewModel
import com.example.eventlyapp.ui.theme.EventLyAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val appComponent = (application as EventlyApplication).appComponent
        setContent {
            val appViewModel: AppViewModel = viewModel(
                factory = appComponent.appViewModelFactory()
            )
            val plannerViewModel: PlannerViewModel = viewModel(
                factory = appComponent.plannerViewModelFactory()
            )
            val newsViewModel: NewsViewModel = viewModel(
                factory = appComponent.newsViewModelFactory()
            )
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = appComponent.settingsViewModelFactory()
            )
            val settingsState by settingsViewModel.state.collectAsState()

            EventLyAppTheme(
                darkTheme = settingsState.themePreference == ThemePreference.DARK
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = androidx.compose.material3.MaterialTheme.colorScheme.background
                ) {
                    EventlyAppRoot(
                        appViewModel = appViewModel,
                        plannerViewModel = plannerViewModel,
                        newsViewModel = newsViewModel,
                        settingsViewModel = settingsViewModel
                    )
                }
            }
        }
    }
}
