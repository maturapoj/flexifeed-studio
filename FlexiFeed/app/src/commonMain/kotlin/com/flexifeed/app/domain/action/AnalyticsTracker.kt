package com.flexifeed.app.domain.action

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface PlatformAnalyticsLogger {
    fun logEvent(name: String, parameters: Map<String, Any?>)
}

class AnalyticsTracker(
    private val platformLogger: PlatformAnalyticsLogger? = null
) {
    private val _events = MutableStateFlow<List<AnalyticsEvent>>(emptyList())
    val events: StateFlow<List<AnalyticsEvent>> = _events.asStateFlow()

    fun logEvent(eventName: String, parameters: Map<String, Any?> = emptyMap()) {
        val event = AnalyticsEvent(eventName, parameters)
        println("SDUIAnalytics: Event logged: '$eventName' with params: $parameters")

        try {
            platformLogger?.logEvent(eventName, parameters)
        } catch (t: Throwable) {
            println("SDUIAnalytics: Failed to log event to platform: ${t.message}")
        }

        _events.value = listOf(event) + _events.value.take(49)
    }

    fun clearEvents() {
        _events.value = emptyList()
    }
}
