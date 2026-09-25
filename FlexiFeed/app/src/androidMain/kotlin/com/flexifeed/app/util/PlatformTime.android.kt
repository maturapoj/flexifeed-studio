package com.flexifeed.app.util

import com.flexifeed.app.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

actual fun currentTimeMillis(): Long = System.currentTimeMillis()

actual fun formatTimestamp(): String =
    SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())

actual val platformEnvironment: String = BuildConfig.ENVIRONMENT
