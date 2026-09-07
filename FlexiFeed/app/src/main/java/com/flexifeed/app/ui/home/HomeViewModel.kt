package com.flexifeed.app.ui.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.CampaignType
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.domain.model.SDUIScreen
import com.flexifeed.app.domain.model.SDUIStreamEvent
import com.flexifeed.app.domain.repository.SDUIRepository
import com.flexifeed.app.domain.repository.SDUIStreamService
import com.flexifeed.app.ui.state.HomeEffect
import com.flexifeed.app.ui.state.HomeEvent
import com.flexifeed.app.ui.state.HomeUiState
import com.flexifeed.app.ui.state.SDUIFeedUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
 * Purely depends on Domain layer abstractions ([SDUIRepository], [SDUIStreamService]).
 */
class HomeViewModel(
    private val repository: SDUIRepository,
    private val streamService: SDUIStreamService? = null,
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val tag = "HomeViewModel"
    private var fetchFeedJob: Job? = null
    private var cachedHomeFeed: SDUIScreen? = null

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _effect = Channel<HomeEffect>(Channel.BUFFERED)
    val effect: Flow<HomeEffect> = _effect.receiveAsFlow()

    init {
        onEvent(HomeEvent.LoadFeed(_uiState.value.currentCampaign))
        observeLiveStream()
    }

    private fun observeLiveStream() {
        if (streamService == null) {
            Log.d(tag, "streamService is null - Live Stream disabled")
            return
        }
        Log.d(tag, "Starting observeLiveStream...")
        viewModelScope.launch(dispatcher) {
            try {
                streamService.observeEvents().collect { event ->
                    Log.d(tag, "observeLiveStream event: $event")
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
                Log.e(tag, "Error in observeLiveStream: ${t.message}", t)
            }
        }
    }

    /**
     * Single entry point for all UI events.
     */
    fun onEvent(event: HomeEvent) {
        when (event) {
            is HomeEvent.LoadFeed -> loadFeed(event.campaign)
            is HomeEvent.LoadScreen -> loadScreen(event.screenId)
            is HomeEvent.RestoreHomeFeed -> restoreHomeFeed()
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
        _uiState.update { it.copy(feedState = SDUIFeedUiState.Loading, currentCampaign = campaign, currentScreenId = "home") }
        fetchFeedJob?.cancel()
        fetchFeedJob = viewModelScope.launch(dispatcher) {
            repository.fetchHomeFeed(campaign)
                .onSuccess { screen ->
                    cachedHomeFeed = screen
                    _uiState.update {
                        it.copy(
                            feedState = SDUIFeedUiState.Success(
                                screen = screen,
                                campaign = campaign,
                                isLiveServer = repository.isLiveServerConnected
                            ),
                            currentScreenId = "home"
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            feedState = SDUIFeedUiState.Error(
                                error.localizedMessage ?: "Failed to load Server-Driven UI feed"
                            ),
                            currentScreenId = "home"
                        )
                    }
                }
        }
    }

    fun restoreHomeFeed() {
        val cached = cachedHomeFeed
        if (cached != null) {
            _uiState.update {
                it.copy(
                    feedState = SDUIFeedUiState.Success(
                        screen = cached,
                        campaign = it.currentCampaign,
                        isLiveServer = repository.isLiveServerConnected
                    ),
                    currentScreenId = "home"
                )
            }
        } else {
            loadFeed(_uiState.value.currentCampaign)
        }
    }

    fun loadScreen(screenId: String) {
        _uiState.update { it.copy(feedState = SDUIFeedUiState.Loading, currentScreenId = screenId) }
        fetchFeedJob?.cancel()
        fetchFeedJob = viewModelScope.launch(dispatcher) {
            repository.fetchScreen(screenId)
                .onSuccess { screen ->
                    _uiState.update {
                        it.copy(
                            feedState = SDUIFeedUiState.Success(
                                screen = screen,
                                campaign = _uiState.value.currentCampaign,
                                isLiveServer = repository.isLiveServerConnected
                            )
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            feedState = SDUIFeedUiState.Error(
                                error.localizedMessage ?: "Failed to load Server-Driven UI screen"
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
            fetchFeedJob = viewModelScope.launch(dispatcher) {
                val campaign = _uiState.value.currentCampaign
                repository.fetchHomeFeed(campaign)
                    .onSuccess { screen ->
                        cachedHomeFeed = screen
                        _uiState.update {
                            it.copy(
                                feedState = SDUIFeedUiState.Success(
                                    screen = screen,
                                    campaign = campaign,
                                    isLiveServer = repository.isLiveServerConnected
                                ),
                                isRefreshing = false,
                                currentScreenId = "home"
                            )
                        }
                    }
                    .onFailure {
                        _uiState.update { it.copy(isRefreshing = false) }
                    }
            }
        } else {
            if (_uiState.value.currentScreenId == "home") {
                loadFeed(_uiState.value.currentCampaign)
            } else {
                loadScreen(_uiState.value.currentScreenId)
            }
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
        fetchFeedJob = viewModelScope.launch(dispatcher) {
            repository.fetchHomeFeed(targetCampaign)
                .onSuccess { screen ->
                    cachedHomeFeed = screen
                    _uiState.update {
                        it.copy(
                            feedState = SDUIFeedUiState.Success(
                                screen = screen,
                                campaign = targetCampaign,
                                isLiveServer = repository.isLiveServerConnected
                            ),
                            currentScreenId = "home",
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
}
