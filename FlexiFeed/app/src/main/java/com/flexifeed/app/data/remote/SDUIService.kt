package com.flexifeed.app.data.remote

import com.flexifeed.app.data.model.SDUIResponseDTO
import com.flexifeed.app.domain.model.CampaignType

/**
 * Clean service contract for fetching Server-Driven UI feeds.
 * Directly returns [SDUIResponseDTO] parsed by network client or mock provider.
 */
interface SDUIService {
    suspend fun getHomeFeed(campaign: CampaignType = CampaignType.DEFAULT_FEED): SDUIResponseDTO
}
