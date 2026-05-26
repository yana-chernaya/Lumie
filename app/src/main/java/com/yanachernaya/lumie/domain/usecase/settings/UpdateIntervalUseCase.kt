package com.yanachernaya.lumie.domain.usecase.settings

import com.yanachernaya.lumie.domain.entity.Interval
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateIntervalUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(interval: Interval) =
        settingsRepository.updateInterval(interval)
}