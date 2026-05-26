package com.yanachernaya.lumie.domain.usecase.settings

import com.yanachernaya.lumie.domain.entity.PresetBackground
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdatePresetBackgroundUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(presetBackground: PresetBackground) =
        settingsRepository.updatePresetBackground(presetBackground)
}