package com.yanachernaya.lumie.domain.usecase.affirmation

import com.yanachernaya.lumie.domain.repository.AffirmationRepository
import javax.inject.Inject

class ToggleFavoriteUseCase @Inject constructor(
    private val affirmationRepository: AffirmationRepository
) {

    suspend operator fun invoke(id: Int) =
        affirmationRepository.toggleFavorite(id)
}