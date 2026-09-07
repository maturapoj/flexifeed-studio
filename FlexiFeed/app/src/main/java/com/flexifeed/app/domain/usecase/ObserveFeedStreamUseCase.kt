package com.flexifeed.app.domain.usecase

import com.flexifeed.app.domain.model.SDUIStreamEvent
import com.flexifeed.app.domain.repository.SDUIStreamService
import kotlinx.coroutines.flow.Flow

/**
 * Domain Use Case for observing the real-time Server-Sent Events stream.
 */
class ObserveFeedStreamUseCase(
    private val streamService: SDUIStreamService
) {
    operator fun invoke(): Flow<SDUIStreamEvent> =
        streamService.observeEvents()
}
