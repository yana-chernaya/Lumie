package com.yanachernaya.lumie.data.background

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yanachernaya.lumie.domain.usecase.affirmation.LoadNewAffirmationUseCase
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

@HiltWorker
class ContentRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParameters: WorkerParameters,
    private val loadNewAffirmationUseCase: LoadNewAffirmationUseCase
) : CoroutineWorker(context, workerParameters) {

    override suspend fun doWork(): Result {
        Log.d("ContentRefreshWorker", "Start")
        loadNewAffirmationUseCase()
        Log.d("ContentRefreshWorker", "Finish")
        return Result.success()
    }
}