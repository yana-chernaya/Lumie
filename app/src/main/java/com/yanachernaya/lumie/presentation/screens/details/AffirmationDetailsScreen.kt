package com.yanachernaya.lumie.presentation.screens.details

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.rememberGraphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.presentation.ui.theme.ActionIconButton
import com.yanachernaya.lumie.presentation.ui.theme.AffirmationPageItem
import com.yanachernaya.lumie.presentation.ui.theme.ErrorScreen
import com.yanachernaya.lumie.presentation.ui.theme.PresetBackgroundSelectionSheet
import com.yanachernaya.lumie.presentation.ui.theme.SetStatusBarAppearance
import com.yanachernaya.lumie.presentation.ui.theme.affirmationBackground
import com.yanachernaya.lumie.presentation.utils.ImageCaptureResult
import com.yanachernaya.lumie.presentation.utils.ObserveAsEvents
import com.yanachernaya.lumie.presentation.utils.rememberImageCapturer
import com.yanachernaya.lumie.presentation.utils.shareImage
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AffirmationDetailsScreen(
    modifier: Modifier = Modifier,
    id: Int,
    viewModel: AffirmationDetailsViewModel = hiltViewModel(
        creationCallback = { factory: AffirmationDetailsViewModel.Factory ->
            factory.create(id)
        }
    ),
    onBackClick: () -> Unit
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

    val handleCaptureResult: (ImageCaptureResult, (Bitmap) -> AffirmationDetailsCommand) -> Unit =
        { result: ImageCaptureResult, onSuccessCommand: (Bitmap) -> AffirmationDetailsCommand ->
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
        contract = ActivityResultContracts.RequestPermission()
    ) { isPermissionGranted ->
        if (isPermissionGranted) {
            captureAndProcessImage { result ->
                handleCaptureResult(result) { bitmap ->
                    AffirmationDetailsCommand.SaveAffirmationToGallery(bitmap)
                }
            }
        } else {
            scope.launch {
                snackbarHostState.showSnackbar(permissionDeniedMsg)
            }
        }
    }

    val isImageBackgroundScreen =
        currentState is AffirmationDetailsState.Content || currentState is AffirmationDetailsState.Initial

    SetStatusBarAppearance(isImageBackgroundScreen)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is AffirmationDetailsEvent.OpenShareSheet -> {
                shareImage(
                    context = context,
                    uri = event.uri
                ) {
                    viewModel.processCommand(AffirmationDetailsCommand.CompleteShareProcess)
                }
            }

            is AffirmationDetailsEvent.ShowMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(message = event.message.asString(context))
                }
            }
        }
    }

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
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier
                            .padding(start = 16.dp, top = 16.dp)
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        onClick = onBackClick,
                    ) {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    navigationIconContentColor = Color.White,
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->

        when (currentState) {
            is AffirmationDetailsState.Content -> {

                val currentAffirmation = currentState.affirmation
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.affirmationBackground)
                ) {
                    AffirmationPageItem(
                        modifier = Modifier.drawWithContent {
                            graphicsLayer.record { this@drawWithContent.drawContent() }
                            drawContent()
                        },
                        affirmation = currentAffirmation,
                        onToggleFavorite = {
                            viewModel.processCommand(AffirmationDetailsCommand.ToggleFavorite)
                        }
                    )

                    AffirmationDetailsIcons(
                        isFavorite = currentAffirmation.isFavorite,
                        onToggleFavorite = {
                            viewModel.processCommand(AffirmationDetailsCommand.ToggleFavorite)
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
                                        AffirmationDetailsCommand.SaveAffirmationToGallery(bitmap)
                                    }
                                }
                            } else {
                                permissionLauncher.launch(writePermission)
                            }
                        },
                        onShare = {
                            captureAndProcessImage { result ->
                                handleCaptureResult(result) { bitmap ->
                                    AffirmationDetailsCommand.ShareAffirmation(bitmap)
                                }
                            }
                        },
                        onChangeBackground = {
                            isBackgroundSheetOpen = true
                        },
                        modifier = Modifier
                            .padding(bottom = innerPadding.calculateBottomPadding() + 100.dp)
                            .align(Alignment.BottomCenter)
                    )

                    if (isBackgroundSheetOpen) {
                        ModalBottomSheet(
                            onDismissRequest = { isBackgroundSheetOpen = false },
                            sheetState = sheetState
                        ) {

                            PresetBackgroundSelectionSheet(
                                backgrounds = currentState.presets,
                                onChooseClick = { presetBackground ->
                                    viewModel.processCommand(
                                        AffirmationDetailsCommand.ChangeBackground(
                                            affirmationId = currentAffirmation.id,
                                            presetBackground = presetBackground
                                        )
                                    )
                                },
                                onCloseClick = {
                                    scope.launch { sheetState.hide() }.invokeOnCompletion {
                                        if (!sheetState.isVisible) {
                                            isBackgroundSheetOpen = false
                                        }
                                    }
                                }
                            )
                        }
                    }
                }
            }

            is AffirmationDetailsState.Error -> {
                ErrorScreen(
                    title = stringResource(R.string.title_error_screen),
                    message = currentState.message.asString(),
                    buttonText = stringResource(R.string.retry_button),
                    isRetryInProgress = currentState.isRetryInProgress,
                    onRetryClick = {
                        viewModel.processCommand(AffirmationDetailsCommand.RetryLoading)
                    },
                    modifier = Modifier.padding(innerPadding)
                )
            }

            AffirmationDetailsState.Initial -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.affirmationBackground)
                )
            }
        }
    }
}

@Composable
private fun AffirmationDetailsIcons(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSave: () -> Unit,
    onShare: () -> Unit,
    onChangeBackground: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val likeIcon =
            if (isFavorite) R.drawable.ic_heart_filled else R.drawable.ic_heart_outlined
        val likeDescription = stringResource(
            if (isFavorite) R.string.cd_remove_like else R.string.cd_like
        )

        ActionIconButton(
            icon = R.drawable.ic_share,
            description = stringResource(R.string.cd_share),
            onClick = onShare,
            iconSize = 40.dp,
            modifier = Modifier.size(56.dp)
        )
        ActionIconButton(
            icon = R.drawable.ic_download,
            description = stringResource(R.string.cd_download),
            onClick = onSave,
            iconSize = 40.dp,
            modifier = Modifier.size(56.dp)
        )
        ActionIconButton(
            icon = R.drawable.ic_change_background,
            description = stringResource(R.string.cd_change_background),
            onClick = onChangeBackground,
            iconSize = 40.dp,
            modifier = Modifier.size(56.dp)
        )
        ActionIconButton(
            icon = likeIcon,
            description = likeDescription,
            onClick = onToggleFavorite,
            iconSize = 40.dp,
            modifier = Modifier.size(56.dp)
        )
    }
}