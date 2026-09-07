package com.flexifeed.app.ui.home

import androidx.lifecycle.ViewModel
import com.flexifeed.app.data.remote.CampaignType
import com.flexifeed.app.data.repository.SDUIRepository
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.ui.state.HomeEffect
import com.flexifeed.app.ui.state.HomeEvent
import com.flexifeed.app.ui.state.HomeIntent
import com.flexifeed.app.ui.state.HomeSideEffect
import com.flexifeed.app.ui.state.HomeUiState
import com.flexifeed.app.ui.state.SDUIFeedUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home Screen using [uiState] and [HomeEvent] model.
 * Exposes a single [uiState] StateFlow and processes user actions via [onEvent].
 */
class HomeViewModel(
    private val repository: SDUIRepository = SDUIRepository(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Backward-compatible alias for state
    val state: StateFlow<HomeUiState> get() = uiState

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    init {
        onEvent(HomeEvent.LoadFeed(_uiState.value.currentCampaign))
    }

    /**
     * Single entry point for all UI events.
     */
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadFeed -> loadFeed(event.campaign)
            is HomeEvent.Refresh -> refreshFeed(event.isPullToRefresh)
            is HomeEvent.SwitchCampaign -> switchCampaign(event.campaign)
            is HomeEvent.SearchQueryChanged -> updateSearchQuery(event.query)
            is HomeEvent.ClearSearch -> updateSearchQuery("")
            is HomeEvent.SetCartSheetVisible -> updateCartSheetVisible(event.isVisible)
            is HomeEvent.SetAnalyticsSheetVisible -> updateAnalyticsSheetVisible(event.isVisible)
            is HomeEvent.SetNavigationTargetUrl -> updateNavigationTargetUrl(event.url)
            is HomeEvent.HandleSDUIAction -> handleSDUIAction(event.action)
            is HomeEvent.UpdateCartCount -> updateCartCount(event.count)
            is HomeEvent.UpdateAnalyticsCount -> updateAnalyticsCount(event.count)
        }
    }

    // Backward compatibility for onIntent
    fun onIntent(intent: HomeIntent) = onEvent(intent)

    fun loadFeed(campaign: CampaignType = _uiState.value.currentCampaign) {
        _uiState.update { it.copy(feedState = SDUIFeedUiState.Loading, currentCampaign = campaign) }
        scope.launch {
            repository.fetchHomeFeed(campaign)
                .onSuccess { screen ->
                    _uiState.update {
                        it.copy(
                            feedState = SDUIFeedUiState.Success(
                                screen = screen,
                                campaign = campaign,
                                isLiveServer = repository.isLiveServerConnected
                            )
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            feedState = SDUIFeedUiState.Error(
                                error.localizedMessage ?: "Failed to load Server-Driven UI feed"
                            )
                        )
                    }
                }
        }
    }

    fun refreshFeed(isPullToRefresh: Boolean = false) {
        if (isPullToRefresh) {
            _isRefreshing.value = true
            _uiState.update { it.copy(isRefreshing = true) }
            scope.launch {
                val campaign = _uiState.value.currentCampaign
                repository.fetchHomeFeed(campaign)
                    .onSuccess { screen ->
                        _uiState.update {
                            it.copy(
                                feedState = SDUIFeedUiState.Success(
                                    screen = screen,
                                    campaign = campaign,
                                    isLiveServer = repository.isLiveServerConnected
                                ),
                                isRefreshing = false
                            )
                        }
                        _isRefreshing.value = false
                    }
                    .onFailure {
                        _uiState.update { it.copy(isRefreshing = false) }
                        _isRefreshing.value = false
                    }
            }
        } else {
            loadFeed(_uiState.value.currentCampaign)
        }
    }

    fun switchCampaign(campaign: CampaignType) {
        loadFeed(campaign)
    }

    private fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    private fun updateCartSheetVisible(isVisible: Boolean) {
        _uiState.update { it.copy(isCartSheetVisible = isVisible) }
    }

    private fun updateAnalyticsSheetVisible(isVisible: Boolean) {
        _uiState.update { it.copy(isAnalyticsSheetVisible = isVisible) }
    }

    private fun updateNavigationTargetUrl(url: String?) {
        _uiState.update { it.copy(targetNavigationUrl = url) }
    }

    private fun updateCartCount(count: Int) {
        _uiState.update { it.copy(cartCount = count) }
    }

    private fun updateAnalyticsCount(count: Int) {
        _uiState.update { it.copy(analyticsEventsCount = count) }
    }

    private fun handleSDUIAction(action: SDUIAction) {
        if (action.type == SDUIConstants.ActionType.NAVIGATE) {
            action.getTargetUrl()?.let { url ->
                updateNavigationTargetUrl(url)
            }
        }
    }

    fun emitEffect(effect: HomeEffect) {
        _effect.trySend(effect)
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}
