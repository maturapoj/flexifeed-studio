package com.flexifeed.app.data.remote

enum class CampaignType {
    DEFAULT_FEED,
    TECH_WEEKEND
}

/**
 * Clean service contract for fetching Server-Driven UI feeds.
 */
interface SDUIService {
    suspend fun getHomeFeed(campaign: CampaignType = CampaignType.DEFAULT_FEED): String
}
