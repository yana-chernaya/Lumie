package com.yanachernaya.lumie.presentation.screens.favorites

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.domain.entity.Affirmation
import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.presentation.mapper.toDisplayName
import com.yanachernaya.lumie.presentation.ui.theme.ErrorScreen
import com.yanachernaya.lumie.presentation.ui.theme.LumieBottomNavigationBar
import com.yanachernaya.lumie.presentation.ui.theme.LumieTab
import com.yanachernaya.lumie.presentation.ui.theme.SetStatusBarAppearance
import com.yanachernaya.lumie.presentation.ui.theme.affirmationBackground
import com.yanachernaya.lumie.presentation.utils.ObserveAsEvents
import kotlinx.coroutines.launch

private const val ScrollShortcutThreshold = 10
private const val ScrollShortcutTargetIndex = 6

@Composable
fun FavoritesScreen(
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToDetails: (Int) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FavoritesViewModel = hiltViewModel()
) {
    val state = viewModel.state.collectAsStateWithLifecycle()
    val currentState = state.value

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    SetStatusBarAppearance(isImageBackgroundScreen = false)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is FavoritesEvent.NavigateToAffirmationDetails -> onNavigateToDetails(event.id)
            is FavoritesEvent.ShowMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(event.message.asString(context))
                }
            }
        }
    }

    val gridState = rememberLazyGridState()
    val showBottomBar =
        currentState is FavoritesState.Content || currentState is FavoritesState.Empty

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                FavoritesBottomNavigationBar(
                    gridState = gridState,
                    canScrollToTop = currentState is FavoritesState.Content,
                    onNavigateToHome = onNavigateToHome,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
        }
    ) { innerPadding ->
        when (currentState) {
            is FavoritesState.Content -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = innerPadding.calculateTopPadding())
                ) {
                    Text(
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp),
                        text = stringResource(R.string.title_favorites_screen),
                        style = MaterialTheme.typography.headlineMedium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    CategoryFilterRow(
                        availableCategories = currentState.categories,
                        selectedCategory = currentState.selectedCategory,
                        onCategoryClick = { category ->
                            viewModel.processCommand(FavoritesCommand.FilterByCategory(category))
                        }
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    AffirmationsGrid(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        state = gridState,
                        affirmations = currentState.affirmations,
                        contentPaddingBottom = innerPadding.calculateBottomPadding() + 16.dp,
                        onRemoveClick = { id ->
                            viewModel.processCommand(FavoritesCommand.RemoveFromFavorites(id))
                        },
                        onAffirmationClick = { id ->
                            viewModel.processCommand(FavoritesCommand.OpenAffirmationDetails(id))
                        }
                    )
                }
            }

            is FavoritesState.Empty -> {
                EmptyFavoritesContent(
                    title = stringResource(R.string.title_empty_state),
                    message = stringResource(R.string.message_empty_state),
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is FavoritesState.Error -> {
                ErrorScreen(
                    title = stringResource(R.string.title_error_screen),
                    message = currentState.message.asString(),
                    buttonText = stringResource(R.string.retry_button),
                    isRetryInProgress = currentState.isRetryInProgress,
                    onRetryClick = { viewModel.processCommand(FavoritesCommand.RetryLoading) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            is FavoritesState.Initial -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
            }
        }
    }
}

@Composable
private fun FavoritesBottomNavigationBar(
    gridState: LazyGridState,
    canScrollToTop: Boolean,
    onNavigateToHome: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        LumieBottomNavigationBar(
            currentTab = LumieTab.FAVORITES,
            onTabClick = { tab ->
                when (tab) {
                    LumieTab.HOME -> onNavigateToHome()
                    LumieTab.FAVORITES -> {
                        if (canScrollToTop && !gridState.isScrollInProgress && gridState.firstVisibleItemIndex > 0) {
                            scope.launch {
                                if (gridState.firstVisibleItemIndex > ScrollShortcutThreshold) {
                                    gridState.scrollToItem(ScrollShortcutTargetIndex)
                                }
                                gridState.animateScrollToItem(index = 0)
                            }
                        }
                    }

                    LumieTab.SETTINGS -> onNavigateToSettings()
                }
            }
        )
    }
}

@Composable
private fun EmptyFavoritesContent(
    title: String,
    message: String,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.empty_meditation)
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        val alpha by animateFloatAsState(
            targetValue = if (composition != null) 1f else 0f,
            animationSpec = tween(durationMillis = 500),
            label = "LottieAlpha"
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .alpha(alpha),
            contentAlignment = Alignment.Center
        ) {
            LottieAnimation(
                modifier = Modifier.size(300.dp),
                composition = composition,
                iterations = LottieConstants.IterateForever
            )
        }
    }
}

@Composable
private fun AffirmationsGrid(
    state: LazyGridState,
    affirmations: List<Affirmation>,
    contentPaddingBottom: Dp,
    onRemoveClick: (Int) -> Unit,
    onAffirmationClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyVerticalGrid(
        modifier = modifier,
        state = state,
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            bottom = contentPaddingBottom
        ),
        columns = GridCells.Fixed(2),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(
            items = affirmations,
            key = { affirmation ->
                affirmation.id
            }) { affirmation ->
            FavoriteAffirmationItem(
                affirmation = affirmation,
                onLongClick = { onRemoveClick(affirmation.id) },
                onClick = { onAffirmationClick(affirmation.id) }
            )
        }
    }
}

@Composable
private fun FavoriteAffirmationItem(
    affirmation: Affirmation,
    onLongClick: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    Box(
        modifier = modifier
            .aspectRatio(9f / 16f)
            .clip(MaterialTheme.shapes.extraLarge)
            .background(MaterialTheme.colorScheme.affirmationBackground)
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(affirmation.imageUrl)
                .crossfade(500)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Spacer(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.6f),
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent
                        )
                    )
                )
        )
        ExpandableText(
            text = affirmation.text,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
        )
    }
}

@Composable
private fun ExpandableText(
    text: String,
    modifier: Modifier = Modifier,
    collapsedMaxLines: Int = 4,
    expandedMaxLines: Int = 10,
    color: Color = Color.White
) {
    var isExpanded by remember { mutableStateOf(false) }

    Text(
        modifier = modifier
            .animateContentSize()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { isExpanded = !isExpanded },
        text = text,
        style = MaterialTheme.typography.bodyLarge,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = if (isExpanded) expandedMaxLines else collapsedMaxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun CategoryFilterRow(
    availableCategories: List<Category>,
    selectedCategory: Category?,
    onCategoryClick: (Category?) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            CategoryChip(
                isSelected = selectedCategory == null,
                onClick = { onCategoryClick(null) }
            )
        }
        items(
            items = availableCategories,
            key = { category ->
                category.id
            }
        ) { category ->
            val isSelected = category.id == selectedCategory?.id

            CategoryChip(
                category = category,
                isSelected = isSelected,
                onClick = { onCategoryClick(category) }
            )
        }
    }
}

@Composable
private fun CategoryChip(
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    category: Category? = null
) {
    FilterChip(
        modifier = modifier.heightIn(min = 48.dp),
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = category?.toDisplayName() ?: "Все",
                style = MaterialTheme.typography.labelSmall
            )
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            selectedContainerColor = MaterialTheme.colorScheme.primary,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.outlineVariant,
            borderWidth = 1.dp
        )
    )
}