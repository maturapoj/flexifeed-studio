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

import com.flexifeed.app.data.remote.RemoteSDUIService
import com.flexifeed.app.data.remote.SDUIService

class SDUIRepository(
    private val remoteService: SDUIService = RemoteSDUIService(),
    private val mockService: SDUIService = MockSDUIService(),
    private val gson: Gson = Gson()
) {

    var isLiveServerConnected: Boolean = false
        private set

    suspend fun fetchHomeFeed(campaign: CampaignType = CampaignType.DEFAULT_FEED): Result<SDUIScreen> {
        // Attempt live fetch via remote SDUIService (Retrofit) first
        return try {
            val responseDTO = if (remoteService is RemoteSDUIService) {
                remoteService.getHomeFeedDTO()
            } else {
                val liveJson = remoteService.getHomeFeed(campaign)
                gson.fromJson(liveJson, SDUIResponseDTO::class.java)
            }
            val screen = mapToDomain(responseDTO)
            isLiveServerConnected = true
            Result.success(screen)
        } catch (remoteError: Exception) {
            // Graceful fallback to mock SDUIService when server is offline
            try {
                isLiveServerConnected = false
                val fallbackJson = mockService.getHomeFeed(campaign)
                val responseDTO = gson.fromJson(fallbackJson, SDUIResponseDTO::class.java)
                val screen = mapToDomain(responseDTO)
                Result.success(screen)
            } catch (fallbackError: Exception) {
                Result.failure(fallbackError)
            }
        }
    }

    fun parseJsonToScreen(jsonString: String): SDUIScreen {
        val responseDTO = gson.fromJson(jsonString, SDUIResponseDTO::class.java)
        return mapToDomain(responseDTO)
    }

    private fun mapToDomain(dto: SDUIResponseDTO?): SDUIScreen {
        val screenName = dto?.screen ?: "UNKNOWN"
        val version = dto?.version ?: "1.0"
        val theme = dto?.theme?.let {
            com.flexifeed.app.domain.model.SDUITheme(
                primaryColorHex = it.primaryColor,
                accentColorHex = it.accentColor,
                isDarkMode = it.mode.equals("DARK", ignoreCase = true)
            )
        }
        val sections = dto?.sections?.map { mapNode(it) } ?: emptyList()
        return SDUIScreen(
            screen = screenName,
            version = version,
            theme = theme,
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
