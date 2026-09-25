package com.flexifeed.app.domain.repository

import com.flexifeed.app.domain.model.SDUIStreamEvent
import kotlinx.coroutines.flow.Flow

/**
 * Domain service contract for observing real-time Server-Driven UI events.
 */
interface SDUIStreamService {
    fun observeEvents(): Flow<SDUIStreamEvent>
}
