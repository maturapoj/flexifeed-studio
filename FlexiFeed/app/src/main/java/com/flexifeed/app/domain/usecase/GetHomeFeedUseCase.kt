package com.flexifeed.app.domain.usecase

import com.flexifeed.app.domain.model.CampaignType
import com.flexifeed.app.domain.model.SDUIScreen
import com.flexifeed.app.domain.repository.SDUIRepository

/**
 * Domain Use Case for retrieving the home feed for a given campaign preset.
 */
class GetHomeFeedUseCase(
    private val repository: SDUIRepository
) {
    val isLiveServerConnected: Boolean
        get() = repository.isLiveServerConnected

    suspend operator fun invoke(campaign: CampaignType = CampaignType.DEFAULT_FEED): Result<SDUIScreen> =
        repository.fetchHomeFeed(campaign)
}
