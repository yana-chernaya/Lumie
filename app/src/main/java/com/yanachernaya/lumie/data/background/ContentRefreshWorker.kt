package com.yanachernaya.lumie.data.background

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yanachernaya.lumie.domain.usecase.affirmation.LoadNewAffirmationUseCase
import com.yanachernaya.lumie.domain.usecase.settings.GetSettingsUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class ContentRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val loadNewAffirmationUseCase: LoadNewAffirmationUseCase,
    private val getSettingsUseCase: GetSettingsUseCase,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        val settings = getSettingsUseCase().first()
        val affirmation = loadNewAffirmationUseCase()
        if (settings.isNotificationsEnabled) {
            notificationHelper.showNewAffirmationNotification(affirmation.text)
        }
        return Result.success()
    }
}