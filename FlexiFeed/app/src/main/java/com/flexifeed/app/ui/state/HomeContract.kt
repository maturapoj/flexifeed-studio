package com.flexifeed.app.ui.state

import com.flexifeed.app.data.remote.CampaignType
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUITheme

/**
 * MVI Contract for the Home Screen.
 * Contains the single source of truth State, user Intents, and one-shot SideEffects.
 */

data class HomeUiState(
    val feedState: SDUIFeedUiState = SDUIFeedUiState.Loading,
    val isRefreshing: Boolean = false,
    val searchQuery: String = "",
    val currentCampaign: CampaignType = CampaignType.DEFAULT_FEED,
    val isCartSheetVisible: Boolean = false,
    val isAnalyticsSheetVisible: Boolean = false,
    val targetNavigationUrl: String? = null,
    val cartCount: Int = 0,
    val analyticsEventsCount: Int = 0
) {
    val sduiTheme: SDUITheme?
        get() = (feedState as? SDUIFeedUiState.Success)?.screen?.theme
}

sealed interface HomeIntent {
    data class LoadFeed(val campaign: CampaignType = CampaignType.DEFAULT_FEED) : HomeIntent
    data class Refresh(val isPullToRefresh: Boolean = true) : HomeIntent
    data class SwitchCampaign(val campaign: CampaignType) : HomeIntent
    data class SearchQueryChanged(val query: String) : HomeIntent
    data object ClearSearch : HomeIntent
    data class SetCartSheetVisible(val isVisible: Boolean) : HomeIntent
    data class SetAnalyticsSheetVisible(val isVisible: Boolean) : HomeIntent
    data class SetNavigationTargetUrl(val url: String?) : HomeIntent
    data class HandleSDUIAction(val action: SDUIAction) : HomeIntent
    data class UpdateCartCount(val count: Int) : HomeIntent
    data class UpdateAnalyticsCount(val count: Int) : HomeIntent
}

sealed interface HomeSideEffect {
    data class ShowSnackbar(val message: String) : HomeSideEffect
}
