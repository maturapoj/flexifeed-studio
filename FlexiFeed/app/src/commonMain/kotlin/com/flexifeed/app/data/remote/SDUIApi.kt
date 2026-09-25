package com.flexifeed.app.data.remote

import com.flexifeed.app.data.model.SDUIResponseDTO
import com.flexifeed.app.util.NetworkLogCollector
import com.flexifeed.app.util.NetworkLogEntry
import com.flexifeed.app.util.currentTimeMillis
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse

/**
 * Server-Driven UI API Interface.
 */
interface SDUIApi {

    suspend fun getHomeFeed(
        campaign: String? = null
    ): SDUIResponseDTO

    suspend fun getScreen(
        screenId: String
    ): SDUIResponseDTO
}

class KtorSDUIApi(
    private val client: HttpClient,
    private val baseUrl: String,
    private val logCollector: NetworkLogCollector? = null
) : SDUIApi {

    private var reqCounter = 0

    override suspend fun getHomeFeed(campaign: String?): SDUIResponseDTO {
        val cleanBase = baseUrl.trimEnd('/')
        val url = if (!campaign.isNullOrEmpty()) {
            "$cleanBase/api/v1/home-feed?campaign=$campaign"
        } else {
            "$cleanBase/api/v1/home-feed"
        }
        return executeAndLog(url)
    }

    override suspend fun getScreen(screenId: String): SDUIResponseDTO {
        val cleanBase = baseUrl.trimEnd('/')
        val url = "$cleanBase/api/v1/screen/$screenId"
        return executeAndLog(url)
    }

    private suspend fun executeAndLog(url: String): SDUIResponseDTO {
        val id = "req_${++reqCounter}_${currentTimeMillis()}"
        val start = currentTimeMillis()
        try {
            val response: HttpResponse = client.get(url)
            val duration = currentTimeMillis() - start
            val bodyDto: SDUIResponseDTO = response.body()
            logCollector?.log(
                NetworkLogEntry(
                    id = id,
                    method = "GET",
                    url = url,
                    statusCode = response.status.value,
                    durationMs = duration,
                    timestamp = start,
                    responseBodySummary = "screen: ${bodyDto.screen}, version: ${bodyDto.version}, sections: ${bodyDto.sections?.size ?: 0}"
                )
            )
            return bodyDto
        } catch (t: Throwable) {
            val duration = currentTimeMillis() - start
            logCollector?.log(
                NetworkLogEntry(
                    id = id,
                    method = "GET",
                    url = url,
                    statusCode = null,
                    durationMs = duration,
                    timestamp = start,
                    error = t.message ?: t.toString()
                )
            )
            throw t
        }
    }
}
