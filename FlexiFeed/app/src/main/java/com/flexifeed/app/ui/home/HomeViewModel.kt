package com.flexifeed.app.ui.home

import androidx.lifecycle.ViewModel
import com.flexifeed.app.data.remote.SDUIStreamEvent
import com.flexifeed.app.data.remote.SDUIStreamService
import com.flexifeed.app.data.repository.SDUIRepository
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.CampaignType
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.ui.state.HomeEffect
import com.flexifeed.app.ui.state.HomeEvent
import com.flexifeed.app.ui.state.HomeUiState
import com.flexifeed.app.ui.state.SDUIFeedUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the Home Screen using [uiState] and [HomeEvent] model.
 * Exposes a single [uiState] StateFlow and processes user actions via [onEvent].
 */
class HomeViewModel(
    private val repository: SDUIRepository = SDUIRepository(),
    private val streamService: SDUIStreamService? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val scope = CoroutineScope(dispatcher + SupervisorJob())
    private var fetchFeedJob: Job? = null

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // Backward-compatible alias for state
    val state: StateFlow<HomeUiState> get() = uiState

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    val isRefreshing: StateFlow<Boolean> = _uiState
        .map { it.isRefreshing }
        .stateIn(scope, SharingStarted.Eagerly, _uiState.value.isRefreshing)

    init {
        onEvent(HomeEvent.LoadFeed(_uiState.value.currentCampaign))
        observeLiveStream()
    }

    private fun observeLiveStream() {
        if (streamService == null) {
            android.util.Log.d("HomeViewModel", "streamService is null - Live Stream disabled")
            return
        }
        android.util.Log.d("HomeViewModel", "Starting observeLiveStream...")
        scope.launch {
            android.util.Log.d("HomeViewModel", "Coroutines scope launched, calling collect on streamService...")
            try {
                streamService.observeEvents().collect { event ->
                    android.util.Log.d("HomeViewModel", "observeLiveStream event: $event")
                    when (event) {
                        is SDUIStreamEvent.Connected -> {
                            onEvent(HomeEvent.LiveConnectionChanged(true))
                        }
                        is SDUIStreamEvent.Disconnected -> {
                            onEvent(HomeEvent.LiveConnectionChanged(false))
                        }
                        is SDUIStreamEvent.FeedUpdated -> {
                            onEvent(HomeEvent.LiveHotReloadReceived(event.presetId))
                        }
                    }
                }
            } catch (t: Throwable) {
                android.util.Log.e("HomeViewModel", "Error in observeLiveStream: ${t.message}", t)
            }
        }
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
            is HomeEvent.LiveConnectionChanged -> updateLiveConnection(event.isConnected)
            is HomeEvent.LiveHotReloadReceived -> performLiveHotReload(event.presetId)
        }
    }

    fun loadFeed(campaign: CampaignType = _uiState.value.currentCampaign) {
        _uiState.update { it.copy(feedState = SDUIFeedUiState.Loading, currentCampaign = campaign) }
        fetchFeedJob?.cancel()
        fetchFeedJob = scope.launch {
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
            _uiState.update { it.copy(isRefreshing = true) }
            fetchFeedJob?.cancel()
            fetchFeedJob = scope.launch {
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
                    }
                    .onFailure {
                        _uiState.update { it.copy(isRefreshing = false) }
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

    fun performLiveHotReload(presetId: String? = null) {
        val targetCampaign = if (presetId != null) {
            CampaignType.fromId(presetId)
        } else {
            _uiState.value.currentCampaign
        }
        _uiState.update { it.copy(isHotReloading = true, currentCampaign = targetCampaign) }
        fetchFeedJob?.cancel()
        fetchFeedJob = scope.launch {
            repository.fetchHomeFeed(targetCampaign)
                .onSuccess { screen ->
                    _uiState.update {
                        it.copy(
                            feedState = SDUIFeedUiState.Success(
                                screen = screen,
                                campaign = targetCampaign,
                                isLiveServer = repository.isLiveServerConnected
                            ),
                            isHotReloading = false
                        )
                    }
                    _effect.trySend(HomeEffect.ShowSnackbar("⚡ Live UI Hot-Reloaded!"))
                }
                .onFailure {
                    _uiState.update { it.copy(isHotReloading = false) }
                }
        }
    }

    fun updateLiveConnection(isConnected: Boolean) {
        _uiState.update { it.copy(isLiveConnected = isConnected) }
    }

    fun emitEffect(effect: HomeEffect) {
        _effect.trySend(effect)
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }
}
