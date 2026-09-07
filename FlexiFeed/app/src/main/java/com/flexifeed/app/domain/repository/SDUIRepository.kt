package com.flexifeed.app.domain.repository

import com.flexifeed.app.domain.model.CampaignType
import com.flexifeed.app.domain.model.SDUIScreen

/**
 * Domain repository contract for fetching Server-Driven UI feeds.
 */
interface SDUIRepository {
    val isLiveServerConnected: Boolean
    suspend fun fetchHomeFeed(campaign: CampaignType = CampaignType.DEFAULT_FEED): Result<SDUIScreen>
    suspend fun fetchScreen(screenId: String): Result<SDUIScreen>
}
