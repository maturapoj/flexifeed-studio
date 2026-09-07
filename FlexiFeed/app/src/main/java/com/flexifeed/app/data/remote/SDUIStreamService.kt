package com.flexifeed.app.data.remote

import androidx.compose.runtime.Immutable
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import org.json.JSONObject

/**
 * Events emitted by the Server-Driven UI Live Stream (SSE).
 */
@Immutable
sealed interface SDUIStreamEvent {
    @Immutable
    data class Connected(val timestamp: Long = System.currentTimeMillis()) : SDUIStreamEvent

    @Immutable
    data class Disconnected(val error: String? = null) : SDUIStreamEvent

    @Immutable
    data class FeedUpdated(
        val action: String = "FEED_UPDATED",
        val presetId: String? = null,
        val timestamp: Long = System.currentTimeMillis()
    ) : SDUIStreamEvent
}

/**
 * Service contract for observing live Server-Driven UI updates.
 */
interface SDUIStreamService {
    fun observeEvents(): Flow<SDUIStreamEvent>
}

/**
 * OkHttp SSE implementation of [SDUIStreamService].
 */
class RemoteSDUIStreamService(
    private val okHttpClient: OkHttpClient,
    private val baseUrl: String
) : SDUIStreamService {

    private val sseClient: OkHttpClient = okHttpClient.newBuilder()
        .apply {
            // Streaming SSE responses must NOT be buffered by body-logging interceptors
            interceptors().clear()
        }
        .readTimeout(0, java.util.concurrent.TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .build()

    override fun observeEvents(): Flow<SDUIStreamEvent> = flow {
        var retryAttempt = 0
        while (currentCoroutineContext().isActive) {
            try {
                createRawStream().collect { event ->
                    if (event is SDUIStreamEvent.Connected) {
                        retryAttempt = 0 // Reset backoff on successful connection
                    }
                    emit(event)
                }
            } catch (c: CancellationException) {
                throw c
            } catch (t: Throwable) {
                android.util.Log.w("SDUIStream", "SSE stream exception: ${t.message}")
                emit(SDUIStreamEvent.Disconnected(error = t.localizedMessage))
            }
            retryAttempt++
            val backoffMs = minOf(1500L * (1L shl (retryAttempt - 1).coerceAtMost(3)), 10000L)
            android.util.Log.d("SDUIStream", "SSE disconnected. Reconnecting in ${backoffMs}ms (attempt $retryAttempt)...")
            delay(backoffMs)
        }
    }

    private fun createRawStream(): Flow<SDUIStreamEvent> = callbackFlow {
        val sseUrl = if (baseUrl.endsWith("/")) "${baseUrl}api/v1/feed-stream" else "$baseUrl/api/v1/feed-stream"
        android.util.Log.d("SDUIStream", "callbackFlow started! Connecting to $sseUrl")
        val request = Request.Builder()
            .url(sseUrl)
            .header("Accept", "text/event-stream")
            .header("Cache-Control", "no-cache")
            .build()

        val listener = object : EventSourceListener() {
            override fun onOpen(eventSource: EventSource, response: Response) {
                android.util.Log.d("SDUIStream", "SSE onOpen received with HTTP ${response.code}")
                trySend(SDUIStreamEvent.Connected())
            }

            override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                android.util.Log.d("SDUIStream", "SSE onEvent: type=$type, data=$data")
                when (type) {
                    "CONNECTED" -> {
                        trySend(SDUIStreamEvent.Connected())
                    }
                    "FEED_UPDATED" -> {
                        var action = "FEED_UPDATED"
                        var presetId: String? = null
                        try {
                            val json = JSONObject(data)
                            action = json.optString("action", "FEED_UPDATED")
                            presetId = json.optString("presetId").takeIf { it.isNotEmpty() && it != "null" }
                        } catch (_: Exception) {}
                        trySend(SDUIStreamEvent.FeedUpdated(action = action, presetId = presetId))
                    }
                }
            }

            override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                android.util.Log.e("SDUIStream", "SSE onFailure: ${t?.message}, HTTP ${response?.code}")
                trySend(SDUIStreamEvent.Disconnected(error = t?.localizedMessage))
                close(t)
            }

            override fun onClosed(eventSource: EventSource) {
                android.util.Log.d("SDUIStream", "SSE onClosed")
                trySend(SDUIStreamEvent.Disconnected())
                close()
            }
        }

        val sseFactory = EventSources.createFactory(sseClient)
        val eventSource = sseFactory.newEventSource(request, listener)

        awaitClose {
            eventSource.cancel()
        }
    }
}
