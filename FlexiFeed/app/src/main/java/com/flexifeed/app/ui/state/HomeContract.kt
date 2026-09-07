package com.flexifeed.app.ui.state

import androidx.compose.runtime.Immutable
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.CampaignType
import com.flexifeed.app.domain.model.SDUITheme

/**
 * UI State and Event Model contract for the Home Screen.
 * Exposes a single [HomeUiState] model and [HomeEvent] model for all user actions.
 */

@Immutable
data class HomeUiState(
    val feedState: SDUIFeedUiState = SDUIFeedUiState.Loading,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val currentScreenId: String = "home",
    val currentCampaign: CampaignType = CampaignType.DEFAULT_FEED,
    val isCartSheetVisible: Boolean = false,
    val isAnalyticsSheetVisible: Boolean = false,
    val targetNavigationUrl: String? = null,
    val cartCount: Int = 0,
    val analyticsEventsCount: Int = 0,
    val isLiveConnected: Boolean = false,
    val isHotReloading: Boolean = false
) {
    val sduiTheme: SDUITheme?
        get() = (feedState as? SDUIFeedUiState.Success)?.screen?.theme
}

@Immutable
sealed interface HomeEvent {
    data class LoadFeed(val campaign: CampaignType = CampaignType.DEFAULT_FEED) : HomeEvent
    data class LoadScreen(val screenId: String) : HomeEvent
    data class Refresh(val isPullToRefresh: Boolean = true) : HomeEvent
    data class SwitchCampaign(val campaign: CampaignType) : HomeEvent
    data class SearchQueryChanged(val query: String) : HomeEvent
    data object ClearSearch : HomeEvent
    data class SetCartSheetVisible(val isVisible: Boolean) : HomeEvent
    data class SetAnalyticsSheetVisible(val isVisible: Boolean) : HomeEvent
    data class SetNavigationTargetUrl(val url: String?) : HomeEvent
    data class HandleSDUIAction(val action: SDUIAction) : HomeEvent
    data class UpdateCartCount(val count: Int) : HomeEvent
    data class UpdateAnalyticsCount(val count: Int) : HomeEvent
    data class LiveConnectionChanged(val isConnected: Boolean) : HomeEvent
    data class LiveHotReloadReceived(val presetId: String?) : HomeEvent
    data object RestoreHomeFeed : HomeEvent
}

@Immutable
sealed interface HomeEffect {
    data class ShowSnackbar(val message: String) : HomeEffect
}

