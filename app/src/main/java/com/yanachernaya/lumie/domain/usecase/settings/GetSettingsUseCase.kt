package com.yanachernaya.lumie.domain.usecase.settings

import com.yanachernaya.lumie.domain.entity.Settings
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(): Flow<Settings> =
        settingsRepository.getSettings()
}