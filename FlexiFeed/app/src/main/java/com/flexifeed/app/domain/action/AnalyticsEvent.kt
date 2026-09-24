package com.flexifeed.app.domain.action

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AnalyticsEvent(
    val eventName: String,
    val parameters: Map<String, Any?>,
    val timestamp: String = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
)
