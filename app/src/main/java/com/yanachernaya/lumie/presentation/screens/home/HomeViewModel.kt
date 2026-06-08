package com.yanachernaya.lumie.presentation.screens.home

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.domain.entity.Affirmation
import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.entity.PresetBackground
import com.yanachernaya.lumie.domain.usecase.affirmation.ChangeBackgroundUseCase
import com.yanachernaya.lumie.domain.usecase.affirmation.GetAllAffirmationsUseCase
import com.yanachernaya.lumie.domain.usecase.affirmation.LoadNewAffirmationUseCase
import com.yanachernaya.lumie.domain.usecase.affirmation.ToggleFavoriteUseCase
import com.yanachernaya.lumie.domain.usecase.category.GetSelectedCategoryUseCase
import com.yanachernaya.lumie.domain.usecase.category.SelectCategoryUseCase
import com.yanachernaya.lumie.domain.usecase.image.SaveToCacheUseCase
import com.yanachernaya.lumie.domain.usecase.image.SaveToGalleryUseCase
import com.yanachernaya.lumie.domain.usecase.settings.GetSettingsUseCase
import com.yanachernaya.lumie.presentation.utils.ConnectionType
import com.yanachernaya.lumie.presentation.utils.NetworkMonitor
import com.yanachernaya.lumie.presentation.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAllAffirmationsUseCase: GetAllAffirmationsUseCase,
    private val getSelectedCategoryUseCase: GetSelectedCategoryUseCase,
    private val loadNewAffirmationUseCase: LoadNewAffirmationUseCase,
    private val selectCategoryUseCase: SelectCategoryUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase,
    private val saveToGalleryUseCase: SaveToGalleryUseCase,
    private val saveToCacheUseCase: SaveToCacheUseCase,
    private val changeBackgroundUseCase: ChangeBackgroundUseCase,
    private val settings: GetSettingsUseCase,
    private val networkMonitor: NetworkMonitor
) : ViewModel() {

    private val _state = MutableStateFlow<HomeState>(HomeState.Initial)
    val state = _state.asStateFlow()

    private val _events = Channel<HomeEvent>(Channel.UNLIMITED)
    val events = _events.receiveAsFlow()

    private var dataJob: Job? = null
    private var loadJob: Job? = null
    private var categoryJob: Job? = null
    private var changeBackgroundJob: Job? = null
    private var isSaveToGalleryInProcess = false
    private var isSharingInProcess = false

    init {
        subscribeToData()
    }

    fun processCommand(command: HomeCommand) {
        when (command) {
            is HomeCommand.LoadNewAffirmation -> loadNewAffirmation(command.forceLoad)
            is HomeCommand.SelectCategory -> selectCategory(command.category)
            is HomeCommand.ToggleFavorite -> toggleFavorite(command.id)
            is HomeCommand.SaveAffirmationToGallery -> saveAffirmationToGallery(command.bitmap)
            is HomeCommand.ShareAffirmation -> saveAffirmationToCache(command.bitmap)
            HomeCommand.DismissWifiDialog -> updateIsWifiOnlyAlertVisible(false)
            HomeCommand.RetryLoading -> resubscribeToData()
            HomeCommand.CompleteShareProcess -> onShareProcessCompleted()
            is HomeCommand.ChangeBackground -> changeBackground(
                command.affirmationId,
                command.presetBackground
            )
        }
    }

    private fun subscribeToData() {
        dataJob?.cancel()

        dataJob = combine(
            getAllAffirmationsUseCase(),
            getSelectedCategoryUseCase()
        ) { affirmations, selectedCategory ->
            HomeState.Content(
                affirmations = affirmations,
                selectedCategory = selectedCategory
            )
        }
            .distinctUntilChanged()
            .catch { error ->
                Log.e(TAG, "Error subscribing to database", error)
                _state.update {
                    HomeState.Error(
                        UiText.StringResource(R.string.error_generic)
                    )
                }
            }
            .onEach { stateContent ->
                _state.update { stateContent }
            }
            .launchIn(viewModelScope)
    }

    private fun sendMessage(message: UiText) {
        _events.trySend(HomeEvent.ShowMessage(message))
    }

    private suspend fun handleLoadRequest(forceLoad: Boolean = false) {
        updateIsRefreshing(true)
        delay(REFRESH_DELAY)

        if (forceLoad) {
            updateIsWifiOnlyAlertVisible(false)
            performLoadNewAffirmation()
            return
        }

        val isWifiOnly = settings().map { it.isWifiOnly }.first()
        if (!isWifiOnly) {
            performLoadNewAffirmation()
            return
        }

        when (networkMonitor.getCurrentConnectionType()) {
            ConnectionType.WIFI -> performLoadNewAffirmation()

            ConnectionType.CELLULAR -> {
                updateIsRefreshing(false)
                updateIsWifiOnlyAlertVisible(true)
            }

            ConnectionType.NO_INTERNET -> {
                updateIsRefreshing(false)
                sendMessage(UiText.StringResource(R.string.error_no_internet))
            }

            ConnectionType.UNKNOWN -> performLoadNewAffirmation()
        }
    }

    private suspend fun performLoadNewAffirmation() {
        try {
            loadNewAffirmationUseCase()

        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            sendMessage(UiText.StringResource(R.string.error_loading_affirmation))
            Log.e(TAG, "Error loading a new affirmation", e)
        } finally {
            updateIsRefreshing(false)
        }
    }

    private fun updateIsRefreshing(isRefreshing: Boolean) {
        _state.update { previousState ->
            if (previousState is HomeState.Content) {
                previousState.copy(isRefreshing = isRefreshing)
            } else {
                previousState
            }
        }
    }

    private fun updateIsWifiOnlyAlertVisible(isWifiOnlyAlertVisible: Boolean) {
        _state.update { previousState ->
            if (previousState is HomeState.Content) {
                previousState.copy(isWifiOnlyAlertVisible = isWifiOnlyAlertVisible)
            } else {
                previousState
            }
        }
    }

    private fun loadNewAffirmation(forceLoad: Boolean) {
        if (loadJob?.isActive == true || categoryJob?.isActive == true) return

        loadJob = viewModelScope.launch {
            handleLoadRequest(forceLoad)
        }
    }

    private fun selectCategory(category: Category) {
        categoryJob?.cancel()
        loadJob?.cancel()

        categoryJob = viewModelScope.launch {
            try {
                selectCategoryUseCase(category)
            } catch (e: Exception) {
                sendMessage(UiText.StringResource(R.string.error_category_update))
                Log.e(TAG, "Error updating category", e)
                return@launch
            }
            handleLoadRequest()
        }
    }

    private fun toggleFavorite(id: Int) {
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

    private fun saveAffirmationToGallery(bitmap: Bitmap) {
        if (isSaveToGalleryInProcess) return

        isSaveToGalleryInProcess = true
        viewModelScope.launch {
            try {
                val isSuccess = saveToGalleryUseCase(bitmap = bitmap)
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
                    _events.send(HomeEvent.OpenShareSheet(uri = uri))
                    isHandoffSuccessful = true
                } else {
                    sendMessage(UiText.StringResource(R.string.error_save_to_cache))
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

    private fun updateIsRetryInProgress(isRetryInProgress: Boolean) {
        _state.update { previousState ->
            if (previousState is HomeState.Error) {
                previousState.copy(isRetryInProgress = isRetryInProgress)
            } else {
                previousState
            }
        }
    }

    private fun resubscribeToData() {
        updateIsRetryInProgress(true)
        subscribeToData()
    }

    private fun changeBackground(affirmationId: Int, presetBackground: PresetBackground) {
        changeBackgroundJob?.cancel()

        viewModelScope.launch {
            try {
                changeBackgroundUseCase(affirmationId, presetBackground)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                sendMessage(UiText.StringResource(R.string.error_changing_background))
                Log.e(TAG, "Error changing background", e)
            }
        }
    }
}

private const val TAG = "HomeViewModel"
private const val REFRESH_DELAY = 100L


sealed interface HomeCommand {
    data class SelectCategory(val category: Category) : HomeCommand
    data class LoadNewAffirmation(val forceLoad: Boolean = false) : HomeCommand
    data class ToggleFavorite(val id: Int) : HomeCommand
    data class SaveAffirmationToGallery(val bitmap: Bitmap) : HomeCommand
    data class ShareAffirmation(val bitmap: Bitmap) : HomeCommand
    data object DismissWifiDialog : HomeCommand
    data object RetryLoading : HomeCommand
    data object CompleteShareProcess : HomeCommand
    data class ChangeBackground(
        val affirmationId: Int,
        val presetBackground: PresetBackground
    ) : HomeCommand
}

sealed interface HomeState {
    data object Initial : HomeState
    data class Content(
        val affirmations: List<Affirmation>,
        val categories: List<Category> = Category.entries,
        val selectedCategory: Category,
        val isRefreshing: Boolean = false,
        val isWifiOnlyAlertVisible: Boolean = false,
        val presets: List<PresetBackground> = PresetBackground.entries
    ) : HomeState

    data class Error(
        val message: UiText,
        val isRetryInProgress: Boolean = false
    ) : HomeState
}

sealed interface HomeEvent {
    data class OpenShareSheet(val uri: Uri) : HomeEvent
    data class ShowMessage(val message: UiText) : HomeEvent
}
