package com.flexifeed.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.flexifeed.app.data.remote.CampaignType
import com.flexifeed.app.data.repository.SDUIRepository
import com.flexifeed.app.domain.model.SDUIScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface SDUIFeedUiState {
    data object Loading : SDUIFeedUiState
    data class Success(
        val screen: SDUIScreen,
        val campaign: CampaignType
    ) : SDUIFeedUiState
    data class Error(val message: String) : SDUIFeedUiState
}

class HomeViewModel(
    private val repository: SDUIRepository = SDUIRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<SDUIFeedUiState>(SDUIFeedUiState.Loading)
    val uiState: StateFlow<SDUIFeedUiState> = _uiState.asStateFlow()

    private var currentCampaign: CampaignType = CampaignType.DEFAULT_FEED

    init {
        loadFeed(currentCampaign)
    }

    fun loadFeed(campaign: CampaignType = currentCampaign) {
        currentCampaign = campaign
        _uiState.value = SDUIFeedUiState.Loading
        viewModelScope.launch {
            repository.fetchHomeFeed(campaign)
                .onSuccess { screen ->
                    _uiState.value = SDUIFeedUiState.Success(screen, campaign)
                }
                .onFailure { error ->
                    _uiState.value = SDUIFeedUiState.Error(
                        error.localizedMessage ?: "Failed to load Server-Driven UI feed"
                    )
                }
        }
    }

    fun refreshFeed() {
        loadFeed(currentCampaign)
    }

    fun switchCampaign(campaign: CampaignType) {
        loadFeed(campaign)
    }
}
