package com.flexifeed.app.domain.action

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class AnalyticsEvent(
    val eventName: String,
    val parameters: Map<String, Any?>,
    val timestamp: String = SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()).format(Date())
)

class AnalyticsTracker {
    private val tag = "SDUIAnalytics"
    private val _events = MutableStateFlow<List<AnalyticsEvent>>(emptyList())
    val events: StateFlow<List<AnalyticsEvent>> = _events.asStateFlow()

    fun logEvent(eventName: String, parameters: Map<String, Any?> = emptyMap()) {
        val event = AnalyticsEvent(eventName, parameters)
        try {
            Log.d(tag, "Event logged: '$eventName' with params: $parameters")
        } catch (e: Throwable) {
            println("[$tag] Event logged: '$eventName' with params: $parameters")
        }
        _events.value = listOf(event) + _events.value.take(49)
    }

    fun clearEvents() {
        _events.value = emptyList()
    }
}
