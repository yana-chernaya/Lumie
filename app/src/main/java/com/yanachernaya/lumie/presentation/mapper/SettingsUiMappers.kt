package com.yanachernaya.lumie.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.domain.entity.AppTheme
import com.yanachernaya.lumie.domain.entity.Interval
import com.yanachernaya.lumie.domain.entity.UserGender

@Composable
fun UserGender.toDisplayName(): String = when (this) {
    UserGender.FEMALE -> stringResource(R.string.user_gender_female)
    UserGender.MALE -> stringResource(R.string.user_gender_male)
}

@Composable
fun Interval.toDisplayName(): String = when (this) {
    Interval.HOURS_6 -> stringResource(R.string.interval_6_hours)
    Interval.HOURS_12 -> stringResource(R.string.interval_12_hours)
    Interval.HOURS_24 -> stringResource(R.string.interval_24_hours)
}

@Composable
fun AppTheme.toDisplayName(): String = when (this) {
    AppTheme.LIGHT -> stringResource(R.string.theme_light)
    AppTheme.DARK -> stringResource(R.string.theme_dark)
    AppTheme.SYSTEM -> stringResource(R.string.theme_system)
}