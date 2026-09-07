package com.flexifeed.app.ui.state

import androidx.compose.runtime.Immutable
import com.flexifeed.app.domain.model.CampaignType
import com.flexifeed.app.domain.model.SDUIScreen

/**
 * Sealed interface representing UI State for the SDUI Feed.
 */
@Immutable
sealed interface SDUIFeedUiState {
    data object Loading : SDUIFeedUiState

    data class Success(
        val screen: SDUIScreen,
        val campaign: CampaignType,
        val isLiveServer: Boolean = false
    ) : SDUIFeedUiState

    data class Error(val message: String) : SDUIFeedUiState
}
