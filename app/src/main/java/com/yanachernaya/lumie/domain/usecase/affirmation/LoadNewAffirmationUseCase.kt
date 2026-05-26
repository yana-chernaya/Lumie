package com.yanachernaya.lumie.domain.usecase.affirmation

import com.yanachernaya.lumie.domain.entity.Affirmation
import com.yanachernaya.lumie.domain.repository.AffirmationRepository
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class LoadNewAffirmationUseCase @Inject constructor(
    private val affirmationRepository: AffirmationRepository,
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(): Affirmation {
        val settings = settingsRepository.getSettings().first()
        val presetBackground =
            if (!settings.isMoodBackgroundEnabled) settings.presetBackground else null

        return affirmationRepository.loadNewAffirmation(
            category = settings.category,
            userGender = settings.userGender,
            presetBackground = presetBackground
        )
    }
}