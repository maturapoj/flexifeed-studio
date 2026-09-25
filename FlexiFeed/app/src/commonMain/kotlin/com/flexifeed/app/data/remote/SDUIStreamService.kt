package com.flexifeed.app.data.remote

import com.flexifeed.app.domain.model.SDUIStreamEvent
import com.flexifeed.app.domain.repository.SDUIStreamService
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.prepareGet
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.readUTF8Line
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Multiplatform HTTP SSE streaming implementation of domain [SDUIStreamService].
 */
class RemoteSDUIStreamService(
    private val client: HttpClient,
    private val baseUrl: String,
    private val logCollector: com.flexifeed.app.util.NetworkLogCollector? = null
) : SDUIStreamService {

    private val json = Json { ignoreUnknownKeys = true; isLenient = true }

    override fun observeEvents(): Flow<SDUIStreamEvent> = flow {
        var retryAttempt = 0
        while (currentCoroutineContext().isActive) {
            try {
                val cleanBase = baseUrl.trimEnd('/')
                val sseUrl = "$cleanBase/api/v1/feed-stream"
                println("SDUIStream: Connecting to $sseUrl")

                client.prepareGet(sseUrl) {
                    header("Accept", "text/event-stream")
                    header("Cache-Control", "no-cache")
                }.execute { response ->
                    println("SDUIStream: Connected with status ${response.status}")
                    logCollector?.log(
                        com.flexifeed.app.util.NetworkLogEntry(
                            id = "sse_${com.flexifeed.app.util.currentTimeMillis()}",
                            method = "GET",
                            url = sseUrl,
                            statusCode = response.status.value,
                            durationMs = 0L,
                            timestamp = com.flexifeed.app.util.currentTimeMillis(),
                            responseBodySummary = "SSE Stream Connected (Status: ${response.status})",
                            isSse = true
                        )
                    )
                    emit(SDUIStreamEvent.Connected())
                    retryAttempt = 0

                    val channel = response.bodyAsChannel()
                    var currentEventType = ""

                    while (!channel.isClosedForRead && currentCoroutineContext().isActive) {
                        val line = channel.readUTF8Line() ?: break
                        if (line.startsWith("event:")) {
                            currentEventType = line.removePrefix("event:").trim()
                        } else if (line.startsWith("data:")) {
                            val data = line.removePrefix("data:").trim()
                            if (currentEventType == "CONNECTED") {
                                emit(SDUIStreamEvent.Connected())
                            } else if (currentEventType == "FEED_UPDATED") {
                                var action = "FEED_UPDATED"
                                var presetId: String? = null
                                try {
                                    val obj = json.parseToJsonElement(data).jsonObject
                                    action = obj["action"]?.jsonPrimitive?.content ?: "FEED_UPDATED"
                                    presetId = obj["presetId"]?.jsonPrimitive?.content?.takeIf { it.isNotEmpty() && it != "null" }
                                } catch (_: Exception) {}
                                emit(SDUIStreamEvent.FeedUpdated(action = action, presetId = presetId))
                            }
                        }
                    }
                    emit(SDUIStreamEvent.Disconnected())
                }
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                println("SDUIStream: SSE exception: ${t.message}")
                emit(SDUIStreamEvent.Disconnected(error = t.message))
            }

            retryAttempt++
            val backoffMs = minOf(1500L * (1L shl (retryAttempt - 1).coerceAtMost(3)), 10000L)
            println("SDUIStream: Reconnecting in ${backoffMs}ms (attempt $retryAttempt)...")
            delay(backoffMs)
        }
    }
}
