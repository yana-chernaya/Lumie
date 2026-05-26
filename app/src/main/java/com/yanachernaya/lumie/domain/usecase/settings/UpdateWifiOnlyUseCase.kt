package com.yanachernaya.lumie.domain.usecase.settings

import com.yanachernaya.lumie.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateWifiOnlyUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(wifiOnly: Boolean) =
        settingsRepository.updateWifiOnly(wifiOnly)
}