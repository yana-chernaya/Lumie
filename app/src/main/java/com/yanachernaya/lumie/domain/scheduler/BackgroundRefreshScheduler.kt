package com.yanachernaya.lumie.domain.scheduler

import com.yanachernaya.lumie.domain.entity.RefreshScheduleConfig

interface BackgroundRefreshScheduler {

    fun scheduleBackgroundRefresh(refreshScheduleConfig: RefreshScheduleConfig)
}