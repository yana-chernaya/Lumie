package com.yanachernaya.lumie.presentation.startup

import com.yanachernaya.lumie.di.ApplicationScope
import com.yanachernaya.lumie.domain.usecase.ObserveRefreshScheduleUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppStartupManager @Inject constructor(
    private val observeRefreshScheduleUseCase: ObserveRefreshScheduleUseCase,
    @ApplicationScope private val applicationScope: CoroutineScope
) {

    private var observeRefreshScheduleJob: Job? = null

    fun startObservingRefreshSchedule() {
        if (observeRefreshScheduleJob?.isActive == true) return

        observeRefreshScheduleJob = applicationScope.launch {
            observeRefreshScheduleUseCase()
        }
    }
}