package com.yanachernaya.lumie.data.mapper

import com.yanachernaya.lumie.domain.entity.Interval
import com.yanachernaya.lumie.domain.entity.Settings

fun Int.toInterval(): Interval =
    Interval.entries.find { it.minutes == this } ?: Settings.DEFAULT_INTERVAL