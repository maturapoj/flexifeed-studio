package com.flexifeed.app.domain.action

import android.os.Bundle
import android.util.Log
import com.google.firebase.analytics.FirebaseAnalytics
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

class AnalyticsTracker(
    private val firebaseAnalytics: FirebaseAnalytics? = null
) {
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

        try {
            firebaseAnalytics?.let { fa ->
                val bundle = Bundle()
                parameters.forEach { (key, value) ->
                    when (value) {
                        is String -> bundle.putString(key, value)
                        is Int -> bundle.putInt(key, value)
                        is Long -> bundle.putLong(key, value)
                        is Double -> bundle.putDouble(key, value)
                        is Float -> bundle.putFloat(key, value)
                        is Boolean -> bundle.putBoolean(key, value)
                        else -> bundle.putString(key, value?.toString() ?: "")
                    }
                }
                fa.logEvent(eventName, bundle)
            }
        } catch (t: Throwable) {
            try {
                Log.w(tag, "Failed to log event to Firebase: ${t.message}")
            } catch (_: Throwable) {
                // Ignore in headless test runner
            }
        }

        _events.value = listOf(event) + _events.value.take(49)
    }

    fun clearEvents() {
        _events.value = emptyList()
    }
}
