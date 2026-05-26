package com.yanachernaya.lumie.domain.usecase.affirmation

import com.yanachernaya.lumie.domain.entity.Affirmation
import com.yanachernaya.lumie.domain.repository.AffirmationRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAffirmationByIdUseCase @Inject constructor(
    private val affirmationRepository: AffirmationRepository
) {

    operator fun invoke(id: Int): Flow<Affirmation> {
        return affirmationRepository.getAffirmationById(id)
    }
}