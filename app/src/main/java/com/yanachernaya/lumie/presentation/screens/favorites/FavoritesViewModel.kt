package com.yanachernaya.lumie.presentation.screens.favorites

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.domain.entity.Affirmation
import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.usecase.affirmation.GetFavoritesUseCase
import com.yanachernaya.lumie.domain.usecase.affirmation.ToggleFavoriteUseCase
import com.yanachernaya.lumie.presentation.utils.UiText
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val TAG = "FavoritesViewModel"

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val getFavoritesUseCase: GetFavoritesUseCase,
    private val toggleFavoriteUseCase: ToggleFavoriteUseCase
) : ViewModel() {

    private val _events = Channel<FavoritesEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()
    private val _selectedCategory = MutableStateFlow<Category?>(null)
    private val _retryTrigger = MutableStateFlow(0)

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<FavoritesState> = _retryTrigger.flatMapLatest { triggerValue ->
        combine(
            getFavoritesUseCase(),
            _selectedCategory
        ) { allFavorites, currentCategory ->
            if (allFavorites.isEmpty()) {
                return@combine FavoritesState.Empty
            }

            val availableCategories = allFavorites.map { it.category }.distinct()
            val activeCategory =
                if (currentCategory != null && !availableCategories.contains(currentCategory)) {
                    null
                } else {
                    currentCategory
                }

            val displayAffirmations = if (activeCategory == null) {
                allFavorites
            } else {
                allFavorites.filter { it.category == activeCategory }
            }

            FavoritesState.Content(
                categories = availableCategories,
                selectedCategory = activeCategory,
                affirmations = displayAffirmations
            )
        }
            .onStart {
                if (triggerValue > 0) {
                    emit(
                        FavoritesState.Error(
                            message = UiText.StringResource(R.string.error_generic),
                            isRetryInProgress = true
                        )
                    )
                }
            }
            .onEach { newState ->
                if (newState is FavoritesState.Content && newState.selectedCategory == null && _selectedCategory.value != null) {
                    _selectedCategory.value = null
                    sendMessage(UiText.StringResource(R.string.empty_list_by_category))
                }
            }
            .catch { error ->
                Log.e(TAG, "Error subscribing to database", error)
                emit(
                    FavoritesState.Error(
                        message = UiText.StringResource(R.string.error_generic)
                    )
                )
            }
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = FavoritesState.Initial
        )


    fun processCommand(command: FavoritesCommand) {
        when (command) {
            is FavoritesCommand.FilterByCategory -> _selectedCategory.value = command.category
            FavoritesCommand.RetryLoading -> _retryTrigger.value++
            is FavoritesCommand.OpenAffirmationDetails -> openAffirmationDetails(id = command.id)
            is FavoritesCommand.RemoveFromFavorites -> removeFromFavorites(id = command.id)
        }
    }

    private fun sendMessage(message: UiText) {
        _events.trySend(FavoritesEvent.ShowMessage(message))
    }

    private fun openAffirmationDetails(id: Int) {
        viewModelScope.launch {
            _events.send(FavoritesEvent.NavigateToAffirmationDetails(id))
        }
    }

    private fun removeFromFavorites(id: Int) {
        viewModelScope.launch {
            try {
                toggleFavoriteUseCase(id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                sendMessage(
                    UiText.StringResource(R.string.error_remove_from_favorite)
                )
                Log.e(TAG, "Error removing from favorites", e)
            }
        }
    }
}

sealed interface FavoritesState {
    data object Initial : FavoritesState

    data class Content(
        val categories: List<Category>,
        val selectedCategory: Category? = null,
        val affirmations: List<Affirmation>
    ) : FavoritesState

    data object Empty : FavoritesState

    data class Error(
        val message: UiText,
        val isRetryInProgress: Boolean = false
    ) : FavoritesState
}

sealed interface FavoritesCommand {
    data class FilterByCategory(val category: Category?) : FavoritesCommand
    data class OpenAffirmationDetails(val id: Int) : FavoritesCommand
    data class RemoveFromFavorites(val id: Int) : FavoritesCommand
    data object RetryLoading : FavoritesCommand
}

sealed interface FavoritesEvent {
    data class ShowMessage(val message: UiText) : FavoritesEvent
    data class NavigateToAffirmationDetails(val id: Int) : FavoritesEvent
}
