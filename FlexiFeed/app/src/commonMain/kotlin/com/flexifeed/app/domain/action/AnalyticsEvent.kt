package com.flexifeed.app.domain.action

import com.flexifeed.app.util.formatTimestamp

data class AnalyticsEvent(
    val eventName: String,
    val parameters: Map<String, Any?>,
    val timestamp: String = formatTimestamp()
)
