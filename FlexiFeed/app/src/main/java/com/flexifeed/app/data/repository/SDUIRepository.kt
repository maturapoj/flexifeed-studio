package com.flexifeed.app.data.repository

import com.flexifeed.app.data.model.SDUIActionDTO
import com.flexifeed.app.data.model.SDUINodeDTO
import com.flexifeed.app.data.model.SDUIResponseDTO
import com.flexifeed.app.data.remote.CampaignType
import com.flexifeed.app.data.remote.MockSDUIService
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUINode
import com.flexifeed.app.domain.model.SDUIScreen
import com.google.gson.Gson

class SDUIRepository(
    private val service: MockSDUIService = MockSDUIService(),
    private val gson: Gson = Gson()
) {

    val isLiveServerConnected: Boolean
        get() = service.isLiveServerConnected

    suspend fun fetchHomeFeed(campaign: CampaignType = CampaignType.DEFAULT_FEED): Result<SDUIScreen> {
        return try {
            val jsonString = service.getHomeFeed(campaign)
            val responseDTO = gson.fromJson(jsonString, SDUIResponseDTO::class.java)
            val screen = mapToDomain(responseDTO)
            Result.success(screen)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun parseJsonToScreen(jsonString: String): SDUIScreen {
        val responseDTO = gson.fromJson(jsonString, SDUIResponseDTO::class.java)
        return mapToDomain(responseDTO)
    }

    private fun mapToDomain(dto: SDUIResponseDTO?): SDUIScreen {
        val screenName = dto?.screen ?: "UNKNOWN"
        val version = dto?.version ?: "1.0"
        val sections = dto?.sections?.map { mapNode(it) } ?: emptyList()
        return SDUIScreen(
            screen = screenName,
            version = version,
            sections = sections
        )
    }

    private fun mapNode(dto: SDUINodeDTO): SDUINode {
        val mergedProps = (dto.props?.toMutableMap() ?: mutableMapOf<String, Any?>())
        if (!dto.imageUrl.isNullOrEmpty() && !mergedProps.containsKey("imageUrl")) {
            mergedProps["imageUrl"] = dto.imageUrl
        }

        val items = dto.items?.map { mapNode(it) } ?: emptyList()
        val action = dto.action?.let { mapAction(it) }

        return SDUINode(
            id = dto.id ?: "",
            type = dto.type ?: "UNKNOWN",
            props = mergedProps,
            items = items,
            action = action
        )
    }

    private fun mapAction(dto: SDUIActionDTO): SDUIAction {
        return SDUIAction(
            type = dto.type,
            payload = dto.payload ?: emptyMap()
        )
    }
}
