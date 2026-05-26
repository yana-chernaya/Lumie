package com.yanachernaya.lumie.domain.usecase.settings

import com.yanachernaya.lumie.domain.entity.UserGender
import com.yanachernaya.lumie.domain.repository.SettingsRepository
import javax.inject.Inject

class UpdateUserGenderUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {

    suspend operator fun invoke(userGender: UserGender) =
        settingsRepository.updateUserGender(userGender)
}