package com.yanachernaya.lumie.domain.usecase.settings

import com.yanachernaya.lumie.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateNotificationsEnabledUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(enabled: Boolean) =
        settingsRepository.updateNotificationsEnabled(enabled)
}