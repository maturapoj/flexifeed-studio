package com.flexifeed.app.domain.model

import androidx.compose.runtime.Immutable

/**
 * Domain events emitted by the Server-Driven UI Live Stream (SSE).
 */
@Immutable
sealed interface SDUIStreamEvent {
    @Immutable
    data class Connected(val timestamp: Long = System.currentTimeMillis()) : SDUIStreamEvent

    @Immutable
    data class Disconnected(val error: String? = null) : SDUIStreamEvent

    @Immutable
    data class FeedUpdated(
        val action: String = "FEED_UPDATED",
        val presetId: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : SDUIStreamEvent
}
