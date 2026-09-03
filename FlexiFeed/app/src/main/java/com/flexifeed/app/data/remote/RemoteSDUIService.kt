package com.flexifeed.app.data.remote

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL

/**
 * Production-ready HTTP implementation of SDUIService communicating
 * with the Server-Driven UI backend (e.g. Node.js backend on http://10.0.2.2:8080).
 */
class RemoteSDUIService(
    private val endpointUrl: String = "http://10.0.2.2:8080/api/v1/home-feed"
) : SDUIService {

    override suspend fun getHomeFeed(campaign: CampaignType): String = withContext(Dispatchers.IO) {
        val url = URL(endpointUrl)
        val conn = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            connectTimeout = 2500
            readTimeout = 2500
            setRequestProperty("Accept", "application/json")
        }

        val statusCode = conn.responseCode
        if (statusCode == HttpURLConnection.HTTP_OK) {
            conn.inputStream.bufferedReader().use { it.readText() }
        } else {
            throw IOException("SDUI Server responded with HTTP status $statusCode")
        }
    }
}
