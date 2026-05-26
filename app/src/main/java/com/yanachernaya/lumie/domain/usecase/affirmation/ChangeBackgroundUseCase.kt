package com.yanachernaya.lumie.domain.usecase.affirmation

import com.yanachernaya.lumie.domain.entity.PresetBackground
import com.yanachernaya.lumie.domain.repository.AffirmationRepository
import javax.inject.Inject

class ChangeBackgroundUseCase @Inject constructor(
    private val affirmationRepository: AffirmationRepository
) {

    suspend operator fun invoke(
        id: Int,
        presetBackground: PresetBackground
    ) {
        affirmationRepository.changeBackground(id, presetBackground)
    }
}