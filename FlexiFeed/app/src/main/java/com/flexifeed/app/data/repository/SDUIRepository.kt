package com.flexifeed.app.data.repository

import com.flexifeed.app.data.remote.MockSDUIService
import com.flexifeed.app.data.remote.RemoteSDUIService
import com.flexifeed.app.data.remote.SDUIService
import com.google.gson.Gson

import com.flexifeed.app.domain.model.CampaignType
import com.flexifeed.app.domain.model.SDUIScreen
import com.flexifeed.app.domain.repository.SDUIRepository as DomainSDUIRepository

/**
 * Backward compatibility class delegating to [SDUIRepositoryImpl].
 */
class SDUIRepository(
    private val delegate: SDUIRepositoryImpl
) : DomainSDUIRepository {

    constructor(
        remoteService: SDUIService = RemoteSDUIService(),
        mockService: SDUIService = MockSDUIService(),
        gson: Gson = Gson()
    ) : this(SDUIRepositoryImpl(remoteService, mockService, gson))

    override val isLiveServerConnected: Boolean
        get() = delegate.isLiveServerConnected

    override suspend fun fetchHomeFeed(campaign: CampaignType): Result<SDUIScreen> {
        return delegate.fetchHomeFeed(campaign)
    }

    fun parseJsonToScreen(jsonString: String): SDUIScreen {
        return delegate.parseJsonToScreen(jsonString)
    }
}
