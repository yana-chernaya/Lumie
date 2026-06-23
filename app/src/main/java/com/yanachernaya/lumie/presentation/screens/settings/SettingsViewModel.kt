package com.yanachernaya.lumie.presentation.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.domain.entity.AppTheme
import com.yanachernaya.lumie.domain.entity.Interval
import com.yanachernaya.lumie.domain.entity.PresetBackground
import com.yanachernaya.lumie.domain.entity.Settings
import com.yanachernaya.lumie.domain.entity.UserGender
import com.yanachernaya.lumie.domain.usecase.settings.GetSettingsUseCase
import com.yanachernaya.lumie.domain.usecase.settings.UpdateAppThemeUseCase
import com.yanachernaya.lumie.domain.usecase.settings.UpdateIntervalUseCase
import com.yanachernaya.lumie.domain.usecase.settings.UpdateMoodBackgroundEnabledUseCase
import com.yanachernaya.lumie.domain.usecase.settings.UpdateNotificationsEnabledUseCase
import com.yanachernaya.lumie.domain.usecase.settings.UpdatePresetBackgroundUseCase
import com.yanachernaya.lumie.domain.usecase.settings.UpdateUserGenderUseCase
import com.yanachernaya.lumie.domain.usecase.settings.UpdateWifiOnlyUseCase
import com.yanachernaya.lumie.presentation.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "SettingsViewModel"

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase,
    private val updateAppThemeUseCase: UpdateAppThemeUseCase,
    private val updateIntervalUseCase: UpdateIntervalUseCase,
    private val updateMoodBackgroundEnabledUseCase: UpdateMoodBackgroundEnabledUseCase,
    private val updateNotificationsEnabledUseCase: UpdateNotificationsEnabledUseCase,
    private val updatePresetBackgroundUseCase: UpdatePresetBackgroundUseCase,
    private val updateUserGenderUseCase: UpdateUserGenderUseCase,
    private val updateWifiOnlyUseCase: UpdateWifiOnlyUseCase
) : ViewModel() {

    private val _events = Channel<SettingsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private val _retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<SettingsState> = _retryTrigger.flatMapLatest { triggerValue ->
        getSettingsUseCase()
            .map<Settings, SettingsState> { settings ->
                SettingsState.Content(
                    userGender = settings.userGender,
                    interval = settings.interval,
                    appTheme = settings.appTheme,
                    isMoodBackgroundEnabled = settings.isMoodBackgroundEnabled,
                    presetBackground = settings.presetBackground,
                    isWifiOnly = settings.isWifiOnly,
                    isNotificationsEnabled = settings.isNotificationsEnabled,
                )
            }
            .onStart {
                if (triggerValue > 0) {
                    emit(
                        SettingsState.Error(
                            message = UiText.StringResource(R.string.error_generic),
                            isRetryInProgress = true
                        )
                    )
                }
            }
            .catch { error ->
                Log.e(TAG, "Error subscribing to settings", error)
                emit(
                    SettingsState.Error(
                        UiText.StringResource(R.string.error_generic)
                    )
                )
            }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = SettingsState.Initial
        )

    fun processCommand(command: SettingsCommand) {
        when (command) {
            is SettingsCommand.SelectAppTheme ->
                executeUpdate(
                    block = { updateAppThemeUseCase(theme = command.appTheme) },
                    errorMessage = UiText.StringResource(R.string.error_updating_app_theme),
                    logMessage = "Error updating app theme"
                )

            is SettingsCommand.SelectInterval ->
                executeUpdate(
                    block = { updateIntervalUseCase(interval = command.interval) },
                    errorMessage = UiText.StringResource(R.string.error_updating_interval),
                    logMessage = "Error updating interval"
                )

            is SettingsCommand.SelectPresetBackground ->
                executeUpdate(
                    block = { updatePresetBackgroundUseCase(presetBackground = command.presetBackground) },
                    errorMessage = UiText.StringResource(R.string.error_updating_preset_background),
                    logMessage = "Error updating preset background"
                )

            is SettingsCommand.SelectUserGender ->
                executeUpdate(
                    block = { updateUserGenderUseCase(userGender = command.userGender) },
                    errorMessage = UiText.StringResource(R.string.error_updating_user_gender),
                    logMessage = "Error updating user gender"
                )

            is SettingsCommand.SetMoodBackground ->
                executeUpdate(
                    block = { updateMoodBackgroundEnabledUseCase(enabled = command.enabled) },
                    errorMessage = UiText.StringResource(R.string.error_updating_mood_background),
                    logMessage = "Error updating mood background"
                )

            is SettingsCommand.SetNotificationsEnabled ->
                executeUpdate(
                    block = { updateNotificationsEnabledUseCase(enabled = command.enabled) },
                    errorMessage = UiText.StringResource(R.string.error_updating_notifications_enabled),
                    logMessage = "Error updating notifications enabled"
                )

            is SettingsCommand.SetWifiOnly ->
                executeUpdate(
                    block = { updateWifiOnlyUseCase(wifiOnly = command.wifiOnly) },
                    errorMessage = UiText.StringResource(R.string.error_updating_wifi_only),
                    logMessage = "Error updating wifi only"
                )

            SettingsCommand.RetryLoading -> _retryTrigger.value++
        }
    }

    private fun sendMessage(message: UiText) {
        _events.trySend(SettingsEvent.ShowMessage(message))
    }

    private fun executeUpdate(
        block: suspend () -> Unit,
        errorMessage: UiText,
        logMessage: String
    ) {
        viewModelScope.launch {
            try {
                block()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                sendMessage(errorMessage)
                Log.e(TAG, logMessage, e)
            }
        }
    }
}

sealed interface SettingsState {
    data object Initial : SettingsState

    data class Content(
        val userGender: UserGender,
        val interval: Interval,
        val appTheme: AppTheme,
        val isMoodBackgroundEnabled: Boolean,
        val presetBackground: PresetBackground,
        val isWifiOnly: Boolean,
        val isNotificationsEnabled: Boolean,
        val genders: List<UserGender> = UserGender.entries,
        val intervals: List<Interval> = Interval.entries,
        val themes: List<AppTheme> = AppTheme.entries,
        val presets: List<PresetBackground> = PresetBackground.entries
    ) : SettingsState

    data class Error(
        val message: UiText,
        val isRetryInProgress: Boolean = false
    ) : SettingsState
}

sealed interface SettingsCommand {
    data class SelectUserGender(val userGender: UserGender) : SettingsCommand
    data class SelectInterval(val interval: Interval) : SettingsCommand
    data class SelectAppTheme(val appTheme: AppTheme) : SettingsCommand
    data class SetMoodBackground(val enabled: Boolean) : SettingsCommand
    data class SelectPresetBackground(val presetBackground: PresetBackground) : SettingsCommand
    data class SetWifiOnly(val wifiOnly: Boolean) : SettingsCommand
    data class SetNotificationsEnabled(val enabled: Boolean) : SettingsCommand
    data object RetryLoading : SettingsCommand
}

sealed interface SettingsEvent {
    data class ShowMessage(val message: UiText) : SettingsEvent
}