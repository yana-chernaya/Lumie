package com.yanachernaya.lumie.domain.usecase.category

import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetSelectedCategoryUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    operator fun invoke(): Flow<Category> =
        settingsRepository.getSelectedCategory()
}