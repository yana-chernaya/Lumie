package com.yanachernaya.lumie.presentation.screens.home

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.domain.entity.Affirmation
import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.entity.PresetBackground
import com.yanachernaya.lumie.presentation.mapper.toDisplayName
import com.yanachernaya.lumie.presentation.ui.theme.ActionIconButton
import com.yanachernaya.lumie.presentation.ui.theme.AffirmationPageItem
import com.yanachernaya.lumie.presentation.ui.theme.ErrorScreen
import com.yanachernaya.lumie.presentation.ui.theme.HomeChipsColors
import com.yanachernaya.lumie.presentation.ui.theme.LumieBottomNavigationBar
import com.yanachernaya.lumie.presentation.ui.theme.LumieTab
import com.yanachernaya.lumie.presentation.ui.theme.PresetBackgroundSelector
import com.yanachernaya.lumie.presentation.ui.theme.SetStatusBarAppearance
import com.yanachernaya.lumie.presentation.utils.ImageCaptureResult
import com.yanachernaya.lumie.presentation.utils.ObserveAsEvents
import com.yanachernaya.lumie.presentation.utils.ShareResult
import com.yanachernaya.lumie.presentation.utils.rememberImageCapturer
import com.yanachernaya.lumie.presentation.utils.shareImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {

    val state = viewModel.state.collectAsStateWithLifecycle()
    val currentState = state.value

    val context = LocalContext.current
    val graphicsLayer = rememberGraphicsLayer()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var isBackgroundSheetOpen by rememberSaveable { mutableStateOf(false) }

    val captureAndProcessImage = rememberImageCapturer(graphicsLayer)
    val permissionDeniedMsg = stringResource(R.string.permission_denied_msg)

    val handleCaptureResult: (ImageCaptureResult, (Bitmap) -> HomeCommand) -> Unit =
        { result: ImageCaptureResult, onSuccessCommand: (Bitmap) -> HomeCommand ->
            val messageRes = when (result) {
                ImageCaptureResult.Error -> R.string.error_image_capture
                ImageCaptureResult.ImageNotReady -> R.string.image_not_ready
                is ImageCaptureResult.Success -> {
                    viewModel.processCommand(onSuccessCommand(result.bitmap))
                    null
                }
            }

            if (messageRes != null) {
                scope.launch {
                    snackbarHostState.showSnackbar(context.getString(messageRes))
                }
            }
        }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { isPermissionGranted ->
        if (isPermissionGranted) {
            captureAndProcessImage { result ->
                handleCaptureResult(result) { bitmap ->
                    HomeCommand.SaveAffirmationToGallery(bitmap)
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(permissionDeniedMsg)
            }
        }
    }

    val isImageBackgroundScreen =
        currentState is HomeState.Content || currentState is HomeState.Initial

    SetStatusBarAppearance(isImageBackgroundScreen)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is HomeEvent.OpenShareSheet -> {
                val result = shareImage(
                    context = context,
                    uri = event.uri,
                    onFinish = { viewModel.processCommand(HomeCommand.CompleteShareProcess) }
                )
                val messageRes = when (result) {
                    ShareResult.Success -> null
                    ShareResult.NoAppFound -> R.string.error_no_sharing_app
                    ShareResult.Error -> R.string.error_share_process
                }

                if (messageRes != null) {
                    scope.launch {
                        snackbarHostState.showSnackbar(context.getString(messageRes))
                    }
                }
            }

            is HomeEvent.ShowMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(message = event.message.asString(context))
                }
            }
        }
    }

    val affirmations = (currentState as? HomeState.Content)?.affirmations ?: emptyList()
    val pagerState = rememberPagerState(pageCount = { affirmations.size })

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    shape = MaterialTheme.shapes.medium
                )
            }
        },
        bottomBar = {
            if (currentState is HomeState.Content) {
                HomeNavigationBottomBar(
                    pagerState = pagerState,
                    onNavigateToFavorites = onNavigateToFavorites,
                    onNavigateToSettings = onNavigateToSettings
                )
            }
        }
    ) { innerPadding ->

        when (currentState) {

            is HomeState.Content -> {
                val pullState = rememberPullToRefreshState()
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    PullToRefreshBox(
                        modifier = Modifier.fillMaxSize(),
                        state = pullState,
                        isRefreshing = currentState.isRefreshing,
                        onRefresh = {
                            viewModel.processCommand(HomeCommand.LoadNewAffirmation())
                        },
                        indicator = {
                            PullToRefreshDefaults.Indicator(
                                state = pullState,
                                isRefreshing = currentState.isRefreshing,
                                modifier = Modifier.align(Alignment.TopCenter),
                                containerColor = Color.White,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    ) {
                        VerticalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxSize()
                        ) { pageIndex ->

                            val pageAffirmation = affirmations[pageIndex]

                            AffirmationPageItem(
                                modifier = Modifier.drawWithContent {
                                    graphicsLayer.record { this@drawWithContent.drawContent() }
                                    drawContent()
                                },
                                affirmation = pageAffirmation,
                                onToggleFavorite = {
                                    viewModel.processCommand(
                                        HomeCommand.ToggleFavorite(pageAffirmation.id)
                                    )
                                }
                            )
                        }
                    }

                    CategoriesRow(
                        categories = currentState.categories,
                        selectedCategoryId = currentState.selectedCategory.id,
                        modifier = Modifier.padding(top = innerPadding.calculateTopPadding() + 24.dp)
                    ) { category ->
                        viewModel.processCommand(HomeCommand.SelectCategory(category = category))
                    }

                    val currentAffirmation = affirmations.getOrNull(pagerState.currentPage)

                    HomeSideIcons(
                        currentAffirmation = currentAffirmation,
                        isScrollInProgress = pagerState.isScrollInProgress,
                        onToggleFavorite = {
                            currentAffirmation?.let { affirmation ->
                                viewModel.processCommand(HomeCommand.ToggleFavorite(id = affirmation.id))
                            }
                        },
                        onSave = {
                            val writePermission = Manifest.permission.WRITE_EXTERNAL_STORAGE
                            val needsPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                            val hasWritePermission = ContextCompat.checkSelfPermission(
                                context,
                                writePermission
                            ) == PackageManager.PERMISSION_GRANTED

                            if (!needsPermission || hasWritePermission) {
                                captureAndProcessImage { result ->
                                    handleCaptureResult(result) { bitmap ->
                                        HomeCommand.SaveAffirmationToGallery(bitmap)
                                    }
                                }
                            } else {
                                permissionLauncher.launch(writePermission)
                            }
                        },
                        onShare = {
                            captureAndProcessImage { result ->
                                handleCaptureResult(result) { bitmap ->
                                    HomeCommand.ShareAffirmation(bitmap)
                                }
                            }
                        },
                        onChangeBackground = { isBackgroundSheetOpen = true },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(
                                bottom = innerPadding.calculateBottomPadding() + 4.dp,
                                end = 8.dp
                            )
                    )

                    if (isBackgroundSheetOpen) {
                        ModalBottomSheet(
                            onDismissRequest = { isBackgroundSheetOpen = false },
                            sheetState = sheetState
                        ) {
                            PresetBackgroundSelectionSheet(
                                title = stringResource(R.string.title_preset_background),
                                backgrounds = currentState.presets,
                                onChooseClick = { presetBackground ->
                                    currentAffirmation?.let { affirmation ->
                                        viewModel.processCommand(
                                            HomeCommand.ChangeBackground(
                                                affirmationId = affirmation.id,
                                                presetBackground = presetBackground
                                            )
                                        )
                                    }
                                },
                                onCloseClick = {
                                    scope.launch { sheetState.hide() }
                                        .invokeOnCompletion {
                                            if (!sheetState.isVisible) {
                                                isBackgroundSheetOpen = false
                                            }
                                        }
                                }
                            )
                        }
                    }

                    if (currentState.isWifiOnlyAlertVisible) {
                        CustomActionDialog(
                            title = stringResource(R.string.dialog_no_wifi_title),
                            message = stringResource(R.string.dialog_no_wifi_message),
                            confirmButtonText = stringResource(R.string.dialog_btn_download),
                            cancelButtonText = stringResource(R.string.dialog_btn_cancel),
                            onConfirm = {
                                viewModel.processCommand(HomeCommand.LoadNewAffirmation(forceLoad = true))
                            },
                            onCloseDialog = {
                                viewModel.processCommand(HomeCommand.DismissWifiDialog)
                            }
                        )
                    }
                }
            }

            is HomeState.Error -> {

                ErrorScreen(
                    title = stringResource(R.string.title_error_screen),
                    message = currentState.message.asString(),
                    buttonText = stringResource(R.string.retry_button),
                    isRetryInProgress = currentState.isRetryInProgress,
                    onRetryClick = { viewModel.processCommand(HomeCommand.RetryLoading) },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            HomeState.Initial -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }
    }
}

@Composable
private fun HomeNavigationBottomBar(
    pagerState: PagerState,
    onNavigateToFavorites: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter,
    ) {
        LumieBottomNavigationBar(
            currentTab = LumieTab.HOME,
            onTabClick = { tab ->
                when (tab) {
                    LumieTab.HOME -> {
                        if (!pagerState.isScrollInProgress && pagerState.currentPage > 0) {
                            scope.launch {
                                pagerState.animateScrollToPage(
                                    page = 0,
                                    animationSpec = tween(
                                        durationMillis = 1000,
                                        easing = CubicBezierEasing(
                                            0.25f, 0.0f,
                                            0.1f, 1.0f
                                        )
                                    )
                                )
                            }
                        }
                    }

                    LumieTab.FAVORITES -> onNavigateToFavorites()
                    LumieTab.SETTINGS -> onNavigateToSettings()
                }
            }
        )
    }
}

@Composable
private fun CategoriesRow(
    categories: List<Category>,
    selectedCategoryId: String,
    modifier: Modifier = Modifier,
    onCategoryClick: (Category) -> Unit
) {
    LazyRow(
        modifier = modifier,
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(
            items = categories,
            key = { category ->
                category.id
            }
        ) { category ->
            val isSelected = category.id == selectedCategoryId

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
    category: Category,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        modifier = modifier.heightIn(min = 48.dp),
        selected = isSelected,
        onClick = onClick,
        label = {
            Text(
                text = category.toDisplayName(),
                style = MaterialTheme.typography.labelSmall
            )
        },
        shape = CircleShape,
        colors = FilterChipDefaults.filterChipColors(
            containerColor = HomeChipsColors.ContainerColor,
            selectedContainerColor = HomeChipsColors.SelectedContainerColor,
            labelColor = HomeChipsColors.LabelColor,
            selectedLabelColor = HomeChipsColors.SelectedLabelColor
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = HomeChipsColors.BorderColor,
            selectedBorderColor = HomeChipsColors.SelectedBorderColor,
            borderWidth = 1.dp
        )
    )
}

@Composable
private fun HomeSideIcons(
    currentAffirmation: Affirmation?,
    isScrollInProgress: Boolean,
    onToggleFavorite: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onChangeBackground: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val isFavorite = currentAffirmation?.isFavorite == true
        val likeIcon =
            if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outlined
        val likeDescription = stringResource(
            if (isFavorite) R.string.cd_remove_like else R.string.cd_like
        )
        ActionIconButton(
            icon = likeIcon,
            description = likeDescription,
            onClick = onToggleFavorite,
            iconSize = 32.dp,
            modifier = Modifier.size(48.dp),
            enabled = !isScrollInProgress
        )
        ActionIconButton(
            icon = R.drawable.ic_download,
            description = stringResource(R.string.cd_download),
            onClick = onSave,
            iconSize = 32.dp,
            modifier = Modifier.size(48.dp),
            enabled = !isScrollInProgress
        )
        ActionIconButton(
            icon = R.drawable.ic_share,
            description = stringResource(R.string.cd_share),
            onClick = onShare,
            iconSize = 32.dp,
            modifier = Modifier.size(48.dp),
            enabled = !isScrollInProgress
        )
        ActionIconButton(
            icon = R.drawable.ic_change_background,
            description = stringResource(R.string.cd_change_background),
            onClick = onChangeBackground,
            iconSize = 32.dp,
            modifier = Modifier.size(48.dp),
            enabled = !isScrollInProgress
        )
    }
}

@Composable
private fun PresetBackgroundSelectionSheet(
    title: String,
    backgrounds: List<PresetBackground>,
    onCloseClick: () -> Unit,
    onChooseClick: (PresetBackground) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                modifier = Modifier,
                onClick = onCloseClick
            ) {
                Icon(
                    modifier = Modifier.size(26.dp),
                    painter = painterResource(R.drawable.ic_close),
                    contentDescription = stringResource(R.string.cd_close)
                )
            }
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall
            )
        }
        PresetBackgroundSelector(
            backgrounds = backgrounds,
            onBackgroundSelected = { onChooseClick(it) },
            modifier = Modifier.padding(bottom = 16.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        )
    }
}

@Composable
private fun CustomActionDialog(
    title: String,
    message: String,
    confirmButtonText: String,
    cancelButtonText: String,
    onConfirm: () -> Unit,
    onCloseDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    Dialog(
        onDismissRequest = onCloseDialog,
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true
        )
    ) {
        Surface(
            modifier = modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Text(
                    text = title,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = message,
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val buttonModifier = Modifier
                        .weight(1f)
                        .height(48.dp)

                    Button(
                        modifier = buttonModifier,
                        onClick = onConfirm
                    ) {
                        Text(
                            text = confirmButtonText,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }

                    OutlinedButton(
                        modifier = buttonModifier,
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        onClick = onCloseDialog
                    ) {
                        Text(
                            text = cancelButtonText,
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }
            }
        }
    }
}