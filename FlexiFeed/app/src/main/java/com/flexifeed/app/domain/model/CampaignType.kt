package com.flexifeed.app.domain.model

import androidx.compose.runtime.Immutable

/**
 * Domain representation of Server-Driven UI Campaign Presets.
 * @param id Campaign identifier slug matching Server presets and API query parameters.
 */
@Immutable
enum class CampaignType(val id: String) {
    DEFAULT_FEED("mega-sale"),
    TECH_WEEKEND("tech-weekend");

    companion object {
        fun fromId(id: String?): CampaignType =
            entries.find { it.id.equals(id, ignoreCase = true) } ?: DEFAULT_FEED
    }
}

