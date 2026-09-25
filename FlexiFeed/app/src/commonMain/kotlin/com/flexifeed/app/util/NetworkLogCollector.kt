package com.flexifeed.app.util

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Data model for an in-app HTTP / SSE network log entry.
 */
data class NetworkLogEntry(
    val id: String,
    val method: String,
    val url: String,
    val statusCode: Int? = null,
    val durationMs: Long = 0L,
    val timestamp: Long = currentTimeMillis(),
    val error: String? = null,
    val responseBodySummary: String? = null,
    val isSse: Boolean = false
) {
    val isSuccess: Boolean
        get() = statusCode != null && statusCode in 200..299

    val isError: Boolean
        get() = error != null || (statusCode != null && statusCode >= 400)
}

/**
 * In-memory collector for network inspection across Android & iOS (zero third-party pods needed).
 */
class NetworkLogCollector {

    private val _logs = MutableStateFlow<List<NetworkLogEntry>>(emptyList())
    val logs: StateFlow<List<NetworkLogEntry>> = _logs.asStateFlow()

    fun log(entry: NetworkLogEntry) {
        _logs.update { current ->
            (listOf(entry) + current).take(100) // Keep the most recent 100 requests
        }
    }

    fun clear() {
        _logs.value = emptyList()
    }
}
