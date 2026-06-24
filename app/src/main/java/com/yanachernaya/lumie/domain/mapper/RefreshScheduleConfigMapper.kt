package com.yanachernaya.lumie.domain.mapper

import com.yanachernaya.lumie.domain.entity.RefreshScheduleConfig
import com.yanachernaya.lumie.domain.entity.Settings


fun Settings.toRefreshScheduleConfig(): RefreshScheduleConfig =
    RefreshScheduleConfig(interval, isWifiOnly)