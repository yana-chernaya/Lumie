package com.yanachernaya.lumie.domain.usecase.settings

import com.yanachernaya.lumie.domain.entity.AppTheme
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateAppThemeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(theme: AppTheme) =
        settingsRepository.updateAppTheme(theme)
}