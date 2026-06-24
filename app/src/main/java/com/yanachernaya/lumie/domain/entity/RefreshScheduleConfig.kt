package com.yanachernaya.lumie.domain.entity

data class RefreshScheduleConfig(
    val interval: Interval,
    val isWifiOnly: Boolean
)