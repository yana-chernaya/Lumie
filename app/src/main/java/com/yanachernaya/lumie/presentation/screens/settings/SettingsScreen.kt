package com.yanachernaya.lumie.presentation.screens.settings

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.presentation.mapper.toDisplayName
import com.yanachernaya.lumie.presentation.ui.theme.ErrorScreen
import com.yanachernaya.lumie.presentation.ui.theme.PresetBackgroundSelector
import com.yanachernaya.lumie.presentation.ui.theme.SetStatusBarAppearance
import com.yanachernaya.lumie.presentation.utils.ObserveAsEvents
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {

    val state = viewModel.state.collectAsStateWithLifecycle()
    val currentState = state.value

    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var activeSheet by rememberSaveable { mutableStateOf(SettingsBottomSheet.NONE) }

    SetStatusBarAppearance(isImageBackgroundScreen = false)

    ObserveAsEvents(viewModel.events) { event ->
        when (event) {
            is SettingsEvent.ShowMessage -> {
                scope.launch {
                    snackbarHostState.showSnackbar(message = event.message.asString(context))
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = {
            viewModel.processCommand(SettingsCommand.SetNotificationsEnabled(it))
        }
    )

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
                title = {
                    Text(
                        text = stringResource(R.string.title_settings_screen),
                        style = MaterialTheme.typography.headlineMedium
                    )
                },
                navigationIcon = {
                    IconButton(
                        modifier = Modifier.size(48.dp),
                        onClick = onBackClick
                    ) {
                        Icon(
                            modifier = Modifier.size(32.dp),
                            painter = painterResource(R.drawable.ic_back),
                            contentDescription = stringResource(R.string.cd_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->

        when (currentState) {
            is SettingsState.Content -> {

                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

                if (activeSheet != SettingsBottomSheet.NONE) {
                    ModalBottomSheet(
                        onDismissRequest = { activeSheet = SettingsBottomSheet.NONE },
                        sheetState = sheetState
                    ) {

                        when (activeSheet) {
                            SettingsBottomSheet.GENDER -> {
                                SettingsSelectionSheet(
                                    title = stringResource(R.string.title_gender),
                                    subtitle = stringResource(R.string.subtitle_gender),
                                    selectedValue = currentState.userGender,
                                    options = currentState.genders,
                                    itemLabel = { it.toDisplayName() },
                                    onCloseClick = {
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            if (!sheetState.isVisible) {
                                                activeSheet = SettingsBottomSheet.NONE
                                            }
                                        }
                                    },
                                    onChooseClick = { userGender ->
                                        viewModel.processCommand(
                                            SettingsCommand.SelectUserGender(userGender)
                                        )
                                    }
                                )
                            }

                            SettingsBottomSheet.INTERVAL -> {
                                SettingsSelectionSheet(
                                    title = stringResource(R.string.title_interval),
                                    subtitle = stringResource(R.string.subtitle_interval),
                                    selectedValue = currentState.interval,
                                    options = currentState.intervals,
                                    itemLabel = { it.toDisplayName() },
                                    onCloseClick = {
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            if (!sheetState.isVisible) {
                                                activeSheet = SettingsBottomSheet.NONE
                                            }
                                        }
                                    },
                                    onChooseClick = { interval ->
                                        viewModel.processCommand(
                                            SettingsCommand.SelectInterval(
                                                interval
                                            )
                                        )
                                    }
                                )
                            }

                            SettingsBottomSheet.THEME -> {
                                SettingsSelectionSheet(
                                    title = stringResource(R.string.title_theme),
                                    subtitle = stringResource(R.string.subtitle_theme),
                                    selectedValue = currentState.appTheme,
                                    options = currentState.themes,
                                    itemLabel = { it.toDisplayName() },
                                    onCloseClick = {
                                        scope.launch { sheetState.hide() }.invokeOnCompletion {
                                            if (!sheetState.isVisible) {
                                                activeSheet = SettingsBottomSheet.NONE
                                            }
                                        }
                                    },
                                    onChooseClick = { theme ->
                                        viewModel.processCommand(
                                            SettingsCommand.SelectAppTheme(
                                                theme
                                            )
                                        )
                                    }
                                )
                            }

                            SettingsBottomSheet.NONE -> {}
                        }
                    }
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                ) {
                    item {
                        SettingsClickableItem(
                            title = stringResource(R.string.title_gender),
                            currentValue = currentState.userGender.toDisplayName(),
                            onItemClick = { activeSheet = SettingsBottomSheet.GENDER }
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    item {
                        SettingsClickableItem(
                            title = stringResource(R.string.title_interval),
                            currentValue = currentState.interval.toDisplayName(),
                            onItemClick = { activeSheet = SettingsBottomSheet.INTERVAL }
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    item {
                        SettingsClickableItem(
                            title = stringResource(R.string.title_theme),
                            currentValue = currentState.appTheme.toDisplayName(),
                            onItemClick = { activeSheet = SettingsBottomSheet.THEME }
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    item {
                        SettingsSwitchItem(
                            title = stringResource(R.string.title_mood_background),
                            subtitle = stringResource(R.string.subtitle_mood_background),
                            isChecked = currentState.isMoodBackgroundEnabled,
                            onCheckedChange = { enabled ->
                                viewModel.processCommand(
                                    SettingsCommand.SetMoodBackground(enabled)
                                )
                            }
                        )
                    }

                    if (!currentState.isMoodBackgroundEnabled) {
                        item {
                            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                        }

                        item {
                            Column(
                                modifier
                                    .padding(vertical = 16.dp)
                                    .fillMaxWidth()
                            ) {
                                Text(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    text = stringResource(R.string.title_preset_background),
                                    style = MaterialTheme.typography.titleLarge
                                )

                                PresetBackgroundSelector(
                                    backgrounds = currentState.presets,
                                    selectedBackground = currentState.presetBackground,
                                    onBackgroundSelected = { presetBackground ->
                                        viewModel.processCommand(
                                            SettingsCommand.SelectPresetBackground(
                                                presetBackground
                                            )
                                        )
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp)
                                )
                            }
                        }
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    item {
                        SettingsSwitchItem(
                            title = stringResource(R.string.title_wifi_only),
                            subtitle = stringResource(R.string.subtitle_wifi_only),
                            isChecked = currentState.isWifiOnly,
                            onCheckedChange = { enabled ->
                                viewModel.processCommand(
                                    SettingsCommand.SetWifiOnly(enabled)
                                )
                            }
                        )
                    }

                    item {
                        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                    }

                    item {
                        SettingsSwitchItem(
                            title = stringResource(R.string.title_notifications),
                            subtitle = stringResource(R.string.subtitle_notifications),
                            isChecked = currentState.isNotificationsEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    viewModel.processCommand(
                                        SettingsCommand.SetNotificationsEnabled(enabled)
                                    )
                                }
                            }
                        )
                    }
                }
            }

            is SettingsState.Error ->
                ErrorScreen(
                    title = stringResource(R.string.title_error_screen),
                    message = currentState.message.asString(),
                    buttonText = stringResource(R.string.retry_button),
                    isRetryInProgress = currentState.isRetryInProgress,
                    onRetryClick = { viewModel.processCommand(SettingsCommand.RetryLoading) },
                    modifier = Modifier.padding(innerPadding)
                )

            SettingsState.Initial ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.background)
                )
        }
    }
}

@Composable
private fun SettingsClickableItem(
    title: String,
    currentValue: String,
    onItemClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .fillMaxWidth()
            .clickable { onItemClick() }
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = title,
            style = MaterialTheme.typography.titleLarge
        )
        Text(
            modifier = Modifier,
            text = currentValue,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(0.7f)
        )
    }
}

@Composable
private fun <T> SettingsSelectionSheet(
    title: String,
    subtitle: String,
    selectedValue: T,
    options: List<T>,
    itemLabel: @Composable (T) -> String,
    onCloseClick: () -> Unit,
    onChooseClick: (T) -> Unit,
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
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = subtitle,
            color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
            style = MaterialTheme.typography.bodyLarge
        )
        Spacer(modifier = Modifier.height(20.dp))
        SettingsSegmentedControl(
            selectedValue = selectedValue,
            options = options,
            itemLabel = itemLabel,
            onChooseClick = onChooseClick
        )
    }
}

@Composable
private fun <T> SettingsSegmentedControl(
    selectedValue: T,
    options: List<T>,
    itemLabel: @Composable (T) -> String,
    onChooseClick: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(CircleShape)
            .padding(start = 16.dp, end = 16.dp, bottom = 24.dp)
            .border(
                width = 1.dp,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.outlineVariant
            )
    ) {
        options.forEach { option ->

            val colorBg by animateColorAsState(
                targetValue = if (selectedValue == option) MaterialTheme.colorScheme.primary else Color.Transparent,
                label = "colorBg"
            )
            val colorContent by animateColorAsState(
                targetValue = if (selectedValue == option) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary,
                label = "colorContent"
            )

            Button(
                modifier = Modifier
                    .height(48.dp)
                    .weight(1f),
                onClick = { onChooseClick(option) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorBg,
                    contentColor = colorContent
                )
            ) {
                Text(text = itemLabel(option))
            }
        }
    }
}

@Composable
private fun SettingsSwitchItem(
    title: String,
    subtitle: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier
            .clickable { onCheckedChange(!isChecked) }
            .padding(horizontal = 16.dp, vertical = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            Text(
                modifier = Modifier,
                text = title,
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = subtitle,
                color = MaterialTheme.colorScheme.onBackground.copy(0.7f),
                style = MaterialTheme.typography.bodyLarge
            )
        }
        Switch(
            checked = isChecked,
            onCheckedChange = onCheckedChange
        )
    }
}

enum class SettingsBottomSheet {
    NONE, GENDER, INTERVAL, THEME
}