package com.yanachernaya.lumie.domain.usecase.settings

import com.yanachernaya.lumie.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateMoodBackgroundEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(enabled: Boolean) =
        settingsRepository.updateMoodBackgroundEnabled(enabled)
}