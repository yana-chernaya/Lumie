package com.yanachernaya.lumie.presentation.ui.theme

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.carousel.HorizontalMultiBrowseCarousel
import androidx.compose.material3.carousel.rememberCarouselState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.domain.entity.Affirmation
import com.yanachernaya.lumie.domain.entity.PresetBackground
import com.yanachernaya.lumie.presentation.mapper.toDrawableRes
import kotlinx.coroutines.delay


enum class LumieTab {
    HOME, FAVORITES, SETTINGS
}

@Composable
fun LumieBottomNavigationBar(
    currentTab: LumieTab,
    onTabClick: (LumieTab) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDarkTheme = LocalAppDarkTheme.current
    val isHome = currentTab == LumieTab.HOME
    val unselectedIconColor = if (isHome || isDarkTheme) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSecondary.copy(alpha = 0.6f)
    }
    Box(
        modifier = modifier
            .navigationBarsPadding()
            .padding(bottom = 24.dp)
            .background(
                color = MaterialTheme.colorScheme.secondaryContainer,
                shape = CircleShape
            )
            .border(
                width = 1.dp,
                shape = CircleShape,
                color = MaterialTheme.colorScheme.outline
            )
            .padding(horizontal = 8.dp, vertical = 8.dp)

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            LumieTab.entries.forEach { tab ->

                val isSelected = currentTab == tab

                val bgColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                    label = "${tab.name}_bg"
                )

                val tintColor by animateColorAsState(
                    targetValue = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer else unselectedIconColor,
                    label = "${tab.name}_tint"
                )

                val (icon, description) = when (tab) {
                    LumieTab.HOME -> (if (isSelected) R.drawable.ic_home_fill else
                        R.drawable.ic_home_outline) to R.string.cd_navigation_home

                    LumieTab.FAVORITES -> (if (isSelected) R.drawable.ic_favorite_fill else
                        R.drawable.ic_favorite_outline) to R.string.cd_navigation_favorite

                    LumieTab.SETTINGS -> (if (isSelected) R.drawable.ic_settings_fill else
                        R.drawable.ic_settings_outline) to R.string.cd_navigation_settings
                }
                LumieNavItem(
                    icon = icon,
                    description = description,
                    colorIcon = tintColor,
                    colorBackground = bgColor,
                    onClick = { onTabClick(tab) }
                )
            }
        }
    }
}

@Composable
private fun LumieNavItem(
    @DrawableRes icon: Int,
    @StringRes description: Int,
    colorIcon: Color,
    colorBackground: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .clip(CircleShape)
            .background(colorBackground)
            .size(48.dp)
    ) {
        Crossfade(targetState = icon, label = "IconCrossfade") { targetIcon ->
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = targetIcon),
                contentDescription = stringResource(description),
                tint = colorIcon
            )
        }
    }
}

@Composable
fun AffirmationPageItem(
    affirmation: Affirmation,
    modifier: Modifier = Modifier,
    onToggleFavorite: () -> Unit
) {
    val context = LocalContext.current
    var heartAnimationTrigger by remember { mutableStateOf(0) }

    LaunchedEffect(heartAnimationTrigger) {
        if (heartAnimationTrigger == 0) return@LaunchedEffect
        delay(1000)
        heartAnimationTrigger = 0
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.affirmationBackground)
            .combinedClickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
                onDoubleClick = {
                    val wasFavorite = affirmation.isFavorite
                    onToggleFavorite()
                    if (!wasFavorite) heartAnimationTrigger++
                },
            )
    ) {
        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(affirmation.imageUrl)
                .crossfade(500)
                .build(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        Spacer(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.7f),
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
                .padding(horizontal = 16.dp)
        )
        FloatingHeart(
            modifier = Modifier.fillMaxSize(),
            visible = heartAnimationTrigger > 0
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
        style = MaterialTheme.typography.displayLarge,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = if (isExpanded) expandedMaxLines else collapsedMaxLines,
        overflow = TextOverflow.Ellipsis
    )
}

@Composable
private fun FloatingHeart(
    visible: Boolean,
    modifier: Modifier = Modifier,
    tint: Color = Color.Red,
    iconSize: Dp = 100.dp
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        AnimatedVisibility(
            visible = visible,
            enter = scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioHighBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeIn(),
            exit = scaleOut(animationSpec = tween(200)) + fadeOut(animationSpec = tween(200))
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = tint,
                modifier = Modifier.size(iconSize)
            )
        }
    }
}

@Composable
fun ErrorScreen(
    title: String,
    message: String,
    buttonText: String,
    isRetryInProgress: Boolean,
    onRetryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val composition by rememberLottieComposition(
        spec = LottieCompositionSpec.RawRes(R.raw.error_robot)
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
        Spacer(modifier = Modifier.height(16.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            enabled = !isRetryInProgress,
            onClick = onRetryClick
        ) {
            if (isRetryInProgress) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                    color = LocalContentColor.current
                )
                Spacer(modifier = Modifier.width(12.dp))
            }
            Text(
                text = buttonText,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
fun ActionIconButton(
    icon: Int,
    description: String,
    onClick: () -> Unit,
    iconSize: Dp,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    IconButton(
        modifier = modifier,
        onClick = onClick,
        enabled = enabled,
        colors = IconButtonDefaults.iconButtonColors(
            contentColor = Color.White,
            disabledContentColor = Color.White
        )
    ) {
        Crossfade(targetState = icon, label = "IconCrossfade") { targetIcon ->
            Icon(
                modifier = Modifier.size(iconSize),
                painter = painterResource(id = targetIcon),
                contentDescription = description
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PresetBackgroundSelector(
    backgrounds: List<PresetBackground>,
    onBackgroundSelected: (PresetBackground) -> Unit,
    modifier: Modifier = Modifier,
    selectedBackground: PresetBackground? = null,
    contentPadding: PaddingValues = PaddingValues(0.dp),
) {
    HorizontalMultiBrowseCarousel(
        state = rememberCarouselState { backgrounds.size },
        modifier = modifier
            .fillMaxWidth()
            .height(221.dp),
        preferredItemWidth = 125.dp,
        contentPadding = contentPadding,
        itemSpacing = 8.dp
    ) { index ->

        val carouselItem = backgrounds[index]
        val isSelected = carouselItem == selectedBackground
        val borderSelectedItem by animateColorAsState(
            targetValue = if (isSelected) MaterialTheme.colorScheme.primary else
                Color.Transparent,
            label = "borderSelectedItem"
        )

        Image(
            painter = painterResource(carouselItem.toDrawableRes()),
            contentDescription = stringResource(
                R.string.cd_background_item,
                index + 1
            ),
            modifier = Modifier
                .height(205.dp)
                .maskClip(MaterialTheme.shapes.large)
                .maskBorder(
                    border = BorderStroke(
                        width = 2.dp,
                        color = borderSelectedItem
                    ),
                    shape = MaterialTheme.shapes.large
                )
                .selectable(
                    selected = isSelected,
                    onClick = { onBackgroundSelected(carouselItem) }
                ),
            contentScale = ContentScale.Crop
        )
    }
}