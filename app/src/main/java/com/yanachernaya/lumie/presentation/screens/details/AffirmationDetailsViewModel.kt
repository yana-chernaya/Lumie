package com.yanachernaya.lumie.presentation.screens.details

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.domain.entity.Affirmation
import com.yanachernaya.lumie.domain.entity.PresetBackground
import com.yanachernaya.lumie.domain.usecase.affirmation.ChangeBackgroundUseCase
import com.yanachernaya.lumie.domain.usecase.affirmation.GetAffirmationByIdUseCase
import com.yanachernaya.lumie.domain.usecase.affirmation.ToggleFavoriteUseCase
import com.yanachernaya.lumie.domain.usecase.image.SaveToCacheUseCase
import com.yanachernaya.lumie.domain.usecase.image.SaveToGalleryUseCase
import com.yanachernaya.lumie.presentation.utils.UiText
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
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

private const val TAG = "AffirmationDetailsViewModel"

@HiltViewModel(assistedFactory = AffirmationDetailsViewModel.Factory::class)
class AffirmationDetailsViewModel @AssistedInject constructor(
    private val getAffirmationByIdUseCase: GetAffirmationByIdUseCase,
    private val saveToCacheUseCase: SaveToCacheUseCase,
    private val saveToGalleryUseCase: SaveToGalleryUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val changeBackgroundUseCase: ChangeBackgroundUseCase,
    @Assisted("id") private val id: Int
) : ViewModel() {

    private val _retryTrigger = MutableStateFlow(0)
    private var isSaveToGalleryInProcess = false
    private var isSharingInProcess = false
    private var changeBackgroundJob: Job? = null
    private val _events = Channel<AffirmationDetailsEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<AffirmationDetailsState> = _retryTrigger.flatMapLatest { triggerValue ->
        getAffirmationByIdUseCase(id)
            .map<Affirmation, AffirmationDetailsState> { affirmation ->
                AffirmationDetailsState.Content(affirmation)
            }
            .onStart {
                if (triggerValue > 0) {
                    emit(
                        AffirmationDetailsState.Error(
                            message = UiText.StringResource(R.string.error_generic),
                            isRetryInProgress = true
                        )
                    )
                }
            }
            .catch { error ->
                Log.e(TAG, "Error subscribing to database", error)
                emit(
                    AffirmationDetailsState.Error(
                        UiText.StringResource(R.string.error_generic)
                    )
                )
            }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = AffirmationDetailsState.Initial
        )

    fun processCommand(command: AffirmationDetailsCommand) {
        when (command) {
            is AffirmationDetailsCommand.SaveAffirmationToGallery -> saveAffirmationToGallery(bitmap = command.bitmap)
            is AffirmationDetailsCommand.ShareAffirmation -> saveAffirmationToCache(bitmap = command.bitmap)
            AffirmationDetailsCommand.ToggleFavorite -> toggleFavorite()
            AffirmationDetailsCommand.CompleteShareProcess -> onShareProcessCompleted()
            AffirmationDetailsCommand.RetryLoading -> _retryTrigger.value++
            is AffirmationDetailsCommand.ChangeBackground -> changeBackground(
                command.affirmationId,
                command.presetBackground
            )
        }
    }

    private fun sendMessage(message: UiText) {
        _events.trySend(AffirmationDetailsEvent.ShowMessage(message))
    }

    private fun saveAffirmationToGallery(bitmap: Bitmap) {
        if (isSaveToGalleryInProcess) return

        isSaveToGalleryInProcess = true
        viewModelScope.launch {
            try {
                val isSuccess = saveToGalleryUseCase(bitmap)
                val message = if (isSuccess) {
                    UiText.StringResource(R.string.success_save_to_gallery)
                } else {
                    UiText.StringResource(R.string.error_save_to_gallery)
                }
                sendMessage(message)
            } finally {
                isSaveToGalleryInProcess = false
            }
        }
    }

    private fun saveAffirmationToCache(bitmap: Bitmap) {
        if (isSharingInProcess) return

        isSharingInProcess = true
        viewModelScope.launch {
            var isHandoffSuccessful = false
            try {
                val uri = saveToCacheUseCase(bitmap = bitmap)
                if (uri != null) {
                    _events.send(AffirmationDetailsEvent.OpenShareSheet(uri = uri))
                    isHandoffSuccessful =
                        true
                } else {
                    sendMessage(
                        UiText.StringResource(R.string.error_save_to_cache)
                    )
                }
            } finally {
                if (!isHandoffSuccessful) {
                    onShareProcessCompleted()
                }
            }
        }
    }

    private fun onShareProcessCompleted() {
        isSharingInProcess = false
    }

    private fun toggleFavorite() {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                sendMessage(UiText.StringResource(R.string.error_toggling_favorite))
                Log.e(TAG, "Error toggling favorite", e)
            }
        }
    }

    private fun changeBackground(affirmationId: Int, presetBackground: PresetBackground) {
        changeBackgroundJob?.cancel()

        changeBackgroundJob = viewModelScope.launch {
            try {
                changeBackgroundUseCase(id = affirmationId, presetBackground = presetBackground)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                sendMessage(
                    UiText.StringResource(R.string.error_changing_background)
                )
                Log.e(TAG, "Error changing background", e)
            }
        }
    }

    @AssistedFactory
    interface Factory {
        fun create(@Assisted("id") id: Int): AffirmationDetailsViewModel
    }
}

sealed interface AffirmationDetailsState {
    data object Initial : AffirmationDetailsState

    data class Content(
        val affirmation: Affirmation,
        val presets: List<PresetBackground> = PresetBackground.entries
    ) : AffirmationDetailsState

    data class Error(
        val message: UiText,
        val isRetryInProgress: Boolean = false
    ) : AffirmationDetailsState
}

sealed interface AffirmationDetailsCommand {
    data object ToggleFavorite : AffirmationDetailsCommand
    data class SaveAffirmationToGallery(val bitmap: Bitmap) : AffirmationDetailsCommand
    data class ShareAffirmation(val bitmap: Bitmap) : AffirmationDetailsCommand
    data object CompleteShareProcess : AffirmationDetailsCommand
    data object RetryLoading : AffirmationDetailsCommand
    data class ChangeBackground(
        val affirmationId: Int,
        val presetBackground: PresetBackground
    ) : AffirmationDetailsCommand
}

sealed interface AffirmationDetailsEvent {
    data class OpenShareSheet(val uri: Uri) : AffirmationDetailsEvent
    data class ShowMessage(val message: UiText) : AffirmationDetailsEvent
}