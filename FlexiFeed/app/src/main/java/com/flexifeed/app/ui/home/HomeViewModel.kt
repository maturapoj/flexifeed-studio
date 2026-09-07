package com.flexifeed.app.ui.home

import androidx.lifecycle.ViewModel
import com.flexifeed.app.data.remote.CampaignType
import com.flexifeed.app.data.repository.SDUIRepository
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.ui.state.HomeIntent
import com.flexifeed.app.ui.state.HomeSideEffect
import com.flexifeed.app.ui.state.HomeUiState
import com.flexifeed.app.ui.state.SDUIFeedUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * MVI Container ViewModel for the Home Screen.
 * Exposes a single [state] StateFlow and [effect] SharedFlow, and processes [HomeIntent]s.
 */
class HomeViewModel(
    private val repository: SDUIRepository = SDUIRepository(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val scope = CoroutineScope(dispatcher + SupervisorJob())

    private val _state = MutableStateFlow(HomeUiState())
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    private val _effect = Channel<HomeSideEffect>(Channel.BUFFERED)
    val effect: Flow<HomeSideEffect> = _effect.receiveAsFlow()

    // Backward compatibility accessors backed by StateFlowMapper without requiring viewModelScope
    val uiState: StateFlow<SDUIFeedUiState> = StateFlowMapper(_state) { it.feedState }
    val isRefreshing: StateFlow<Boolean> = StateFlowMapper(_state) { it.isRefreshing }

    init {
        onIntent(HomeIntent.LoadFeed(_state.value.currentCampaign))
    }

    /**
     * Single entry point for all UI intents.
     */
    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.LoadFeed -> loadFeed(intent.campaign)
            is HomeIntent.Refresh -> refreshFeed(intent.isPullToRefresh)
            is HomeIntent.SwitchCampaign -> switchCampaign(intent.campaign)
            is HomeIntent.SearchQueryChanged -> updateSearchQuery(intent.query)
            is HomeIntent.ClearSearch -> updateSearchQuery("")
            is HomeIntent.SetCartSheetVisible -> updateCartSheetVisible(intent.isVisible)
            is HomeIntent.SetAnalyticsSheetVisible -> updateAnalyticsSheetVisible(intent.isVisible)
            is HomeIntent.SetNavigationTargetUrl -> updateNavigationTargetUrl(intent.url)
            is HomeIntent.HandleSDUIAction -> handleSDUIAction(intent.action)
            is HomeIntent.UpdateCartCount -> updateCartCount(intent.count)
            is HomeIntent.UpdateAnalyticsCount -> updateAnalyticsCount(intent.count)
        }
    }

    fun loadFeed(campaign: CampaignType = _state.value.currentCampaign) {
        _state.update { it.copy(feedState = SDUIFeedUiState.Loading, currentCampaign = campaign) }
        scope.launch {
            repository.fetchHomeFeed(campaign)
                .onSuccess { screen ->
                    _state.update {
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
                    _state.update {
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
            _state.update { it.copy(isRefreshing = true) }
            scope.launch {
                val campaign = _state.value.currentCampaign
                repository.fetchHomeFeed(campaign)
                    .onSuccess { screen ->
                        _state.update {
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
                        _state.update { it.copy(isRefreshing = false) }
                    }
            }
        } else {
            loadFeed(_state.value.currentCampaign)
        }
    }

    fun switchCampaign(campaign: CampaignType) {
        loadFeed(campaign)
    }

    private fun updateSearchQuery(query: String) {
        _state.update { it.copy(searchQuery = query) }
    }

    private fun updateCartSheetVisible(isVisible: Boolean) {
        _state.update { it.copy(isCartSheetVisible = isVisible) }
    }

    private fun updateAnalyticsSheetVisible(isVisible: Boolean) {
        _state.update { it.copy(isAnalyticsSheetVisible = isVisible) }
    }

    private fun updateNavigationTargetUrl(url: String?) {
        _state.update { it.copy(targetNavigationUrl = url) }
    }

    private fun updateCartCount(count: Int) {
        _state.update { it.copy(cartCount = count) }
    }

    private fun updateAnalyticsCount(count: Int) {
        _state.update { it.copy(analyticsEventsCount = count) }
    }

    private fun handleSDUIAction(action: SDUIAction) {
        if (action.type == SDUIConstants.ActionType.NAVIGATE) {
            action.getTargetUrl()?.let { url ->
                updateNavigationTargetUrl(url)
            }
        }
    }

    fun emitEffect(effect: HomeSideEffect) {
        _effect.trySend(effect)
    }

    override fun onCleared() {
        super.onCleared()
        scope.cancel()
    }

    /**
     * Lightweight adapter to map StateFlow<T> to StateFlow<R> synchronously without coroutine launch.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    private class StateFlowMapper<T, R>(
        private val source: StateFlow<T>,
        private val transform: (T) -> R
    ) : StateFlow<R> {
        override val value: R get() = transform(source.value)
        override val replayCache: List<R> get() = listOf(value)
        override suspend fun collect(collector: FlowCollector<R>): Nothing {
            source.collect { collector.emit(transform(it)) }
            kotlinx.coroutines.awaitCancellation()
        }
    }
}
