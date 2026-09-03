package com.flexifeed.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flexifeed.app.data.remote.CampaignType
import com.flexifeed.app.data.repository.SDUIRepository
import com.flexifeed.app.domain.model.SDUIScreen
import com.flexifeed.app.ui.state.SDUIFeedUiState
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: SDUIRepository = SDUIRepository(),
    private val dispatcher: CoroutineDispatcher = Dispatchers.Main
) : ViewModel() {

    private val _uiState = MutableStateFlow<SDUIFeedUiState>(SDUIFeedUiState.Loading)
    val uiState: StateFlow<SDUIFeedUiState> = _uiState.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private var currentCampaign: CampaignType = CampaignType.DEFAULT_FEED

    init {
        loadFeed(currentCampaign)
    }

    fun loadFeed(campaign: CampaignType = currentCampaign) {
        currentCampaign = campaign
        _uiState.value = SDUIFeedUiState.Loading
        viewModelScope.launch(dispatcher) {
            repository.fetchHomeFeed(campaign)
                .onSuccess { screen ->
                    _uiState.value = SDUIFeedUiState.Success(
                        screen = screen,
                        campaign = campaign,
                        isLiveServer = repository.isLiveServerConnected
                    )
                }
                .onFailure { error ->
                    _uiState.value = SDUIFeedUiState.Error(
                        error.localizedMessage ?: "Failed to load Server-Driven UI feed"
                    )
                }
        }
    }

    fun refreshFeed(isPullToRefresh: Boolean = false) {
        if (isPullToRefresh) {
            _isRefreshing.value = true
            viewModelScope.launch(dispatcher) {
                repository.fetchHomeFeed(currentCampaign)
                    .onSuccess { screen ->
                        _uiState.value = SDUIFeedUiState.Success(
                            screen = screen,
                            campaign = currentCampaign,
                            isLiveServer = repository.isLiveServerConnected
                        )
                        _isRefreshing.value = false
                    }
                    .onFailure {
                        _isRefreshing.value = false
                    }
            }
        } else {
            loadFeed(currentCampaign)
        }
    }

    fun switchCampaign(campaign: CampaignType) {
        loadFeed(campaign)
    }
}
