package com.yanachernaya.lumie.presentation.mapper

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.yanachernaya.lumie.R
import com.yanachernaya.lumie.domain.entity.Category

@Composable
fun Category.toDisplayName(): String = when (this) {
    Category.SELF_LOVE -> stringResource(R.string.category_self_love)
    Category.MOTIVATION -> stringResource(R.string.category_motivation)
    Category.HAPPINESS -> stringResource(R.string.category_happiness)
    Category.CALM -> stringResource(R.string.category_calm)
    Category.HEALTH -> stringResource(R.string.category_health)
    Category.SUCCESS -> stringResource(R.string.category_success)
    Category.RELATIONSHIP -> stringResource(R.string.category_relationship)
    Category.CONFIDENCE -> stringResource(R.string.category_confidence)
    Category.ENERGY -> stringResource(R.string.category_energy)
    Category.GRATITUDE -> stringResource(R.string.category_gratitude)
}