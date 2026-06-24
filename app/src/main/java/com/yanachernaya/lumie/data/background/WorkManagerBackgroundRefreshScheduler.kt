package com.yanachernaya.lumie.data.background

import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.yanachernaya.lumie.domain.entity.RefreshScheduleConfig
import com.yanachernaya.lumie.domain.scheduler.BackgroundRefreshScheduler
import java.util.concurrent.TimeUnit
import javax.inject.Inject

private const val UNIQUE_WORK_NAME = "content_refresh_work"

class WorkManagerBackgroundRefreshScheduler @Inject constructor(
    private val workManager: WorkManager
) : BackgroundRefreshScheduler {

    override fun scheduleBackgroundRefresh(refreshScheduleConfig: RefreshScheduleConfig) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(
                if (refreshScheduleConfig.isWifiOnly) {
                    NetworkType.UNMETERED
                } else {
                    NetworkType.CONNECTED
                }
            )
            .setRequiresBatteryNotLow(true)
            .build()

        val request = PeriodicWorkRequestBuilder<ContentRefreshWorker>(
            repeatInterval = refreshScheduleConfig.interval.minutes.toLong(),
            repeatIntervalTimeUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setInitialDelay(15L, TimeUnit.MINUTES)
            .build()

        workManager.enqueueUniquePeriodicWork(
            uniqueWorkName = UNIQUE_WORK_NAME,
            existingPeriodicWorkPolicy = ExistingPeriodicWorkPolicy.UPDATE,
            request = request
        )
    }
}