package com.flexifeed.app.data.repository

import com.flexifeed.app.data.model.SDUIActionDTO
import com.flexifeed.app.data.model.SDUINodeDTO
import com.flexifeed.app.data.model.SDUIResponseDTO
import com.flexifeed.app.data.remote.MockSDUIService
import com.flexifeed.app.data.remote.SDUIApi
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.CampaignType
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.domain.model.SDUINode
import com.flexifeed.app.domain.model.SDUIScreen
import com.flexifeed.app.domain.repository.SDUIRepository
import com.google.gson.Gson

/**
 * Data layer implementation of [SDUIRepository] interface from Domain layer.
 */
class SDUIRepositoryImpl(
    private val api: SDUIApi? = null,
    private val mockService: MockSDUIService = MockSDUIService(),
    private val gson: Gson = Gson()
) : SDUIRepository {

    override var isLiveServerConnected: Boolean = false
        private set

    override suspend fun fetchHomeFeed(campaign: CampaignType): Result<SDUIScreen> {
        return executeWithFallback(
            liveCall = { api?.getHomeFeed(campaign.id) },
            fallbackCall = { mockService.getHomeFeed(campaign) }
        )
    }

    override suspend fun fetchScreen(screenId: String): Result<SDUIScreen> {
        return executeWithFallback(
            liveCall = { api?.getScreen(screenId) },
            fallbackCall = { mockService.getScreen(screenId) }
        )
    }

    private suspend fun executeWithFallback(
        liveCall: suspend () -> SDUIResponseDTO?,
        fallbackCall: suspend () -> SDUIResponseDTO
    ): Result<SDUIScreen> {
        try {
            val responseDTO = liveCall()
            if (responseDTO != null) {
                val screen = mapToDomain(responseDTO)
                isLiveServerConnected = true
                return Result.success(screen)
            }
        } catch (_: Exception) {
            // Graceful fallback to mock service below
        }
        return try {
            isLiveServerConnected = false
            val fallbackDTO = fallbackCall()
            val screen = mapToDomain(fallbackDTO)
            Result.success(screen)
        } catch (fallbackError: Exception) {
            Result.failure(fallbackError)
        }
    }

    fun parseJsonToScreen(jsonString: String): SDUIScreen {
        val responseDTO = gson.fromJson(jsonString, SDUIResponseDTO::class.java)
        return mapToDomain(responseDTO)
    }

    private fun mapToDomain(dto: SDUIResponseDTO?): SDUIScreen {
        val screenName = dto?.screen ?: SDUIConstants.Screen.UNKNOWN
        val version = dto?.version ?: SDUIConstants.Screen.DEFAULT_VERSION
        val theme = dto?.theme?.let {
            val logoTheme = it.logo?.let { logoDto ->
                com.flexifeed.app.domain.model.SDUILogoTheme(
                    bgColorHex = logoDto.bgColor ?: it.logoBgColor,
                    iconColorHex = logoDto.iconColor ?: it.logoIconColor,
                    titleColorHex = logoDto.titleColor,
                    subtitleColorHex = logoDto.subtitleColor ?: it.logoSubtitleColor
                )
            }
                ?: if (it.logoBgColor != null || it.logoIconColor != null || it.logoSubtitleColor != null) {
                    com.flexifeed.app.domain.model.SDUILogoTheme(
                        bgColorHex = it.logoBgColor,
                        iconColorHex = it.logoIconColor,
                        subtitleColorHex = it.logoSubtitleColor
                    )
                } else null

            com.flexifeed.app.domain.model.SDUITheme(
                primaryColorHex = it.primaryColor,
                accentColorHex = it.accentColor,
                isDarkMode = it.mode.equals(SDUIConstants.ThemeMode.DARK, ignoreCase = true),
                logoTheme = logoTheme
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
        val mergedProps = (dto.props?.toMutableMap() ?: mutableMapOf())
        if (!dto.imageUrl.isNullOrEmpty() && !mergedProps.containsKey(SDUIConstants.PropKey.IMAGE_URL)) {
            mergedProps[SDUIConstants.PropKey.IMAGE_URL] = dto.imageUrl
        }

        val items = dto.items?.map { mapNode(it) } ?: emptyList()
        val action = dto.action?.let { mapAction(it) }

        return SDUINode(
            id = dto.id ?: "",
            type = dto.type ?: SDUIConstants.Screen.UNKNOWN,
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
