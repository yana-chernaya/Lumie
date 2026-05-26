package com.yanachernaya.lumie.domain.usecase.category

import com.yanachernaya.lumie.domain.entity.Category
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import javax.inject.Inject

class SelectCategoryUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(category: Category) =
        settingsRepository.selectCategory(category)
}