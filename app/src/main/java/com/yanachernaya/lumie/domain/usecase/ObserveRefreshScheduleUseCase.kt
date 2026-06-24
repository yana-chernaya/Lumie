package com.yanachernaya.lumie.domain.usecase

import com.yanachernaya.lumie.domain.mapper.toRefreshScheduleConfig
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import com.yanachernaya.lumie.domain.scheduler.BackgroundRefreshScheduler
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ObserveRefreshScheduleUseCase @Inject constructor(
    private val backgroundRefreshScheduler: BackgroundRefreshScheduler,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke() {
        settingsRepository.getSettings()
            .map { it.toRefreshScheduleConfig() }
            .distinctUntilChanged()
            .collect { config ->
                backgroundRefreshScheduler.scheduleBackgroundRefresh(config)
            }
    }
}