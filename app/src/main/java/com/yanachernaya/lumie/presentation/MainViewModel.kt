package com.yanachernaya.lumie.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanachernaya.lumie.domain.entity.AppTheme
import com.yanachernaya.lumie.domain.usecase.settings.GetSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {

    val appThemeState: StateFlow<AppTheme> =
        getSettingsUseCase()
            .map { it.appTheme }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = AppTheme.SYSTEM
            )
}