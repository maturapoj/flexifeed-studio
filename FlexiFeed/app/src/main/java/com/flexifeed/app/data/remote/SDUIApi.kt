package com.flexifeed.app.data.remote

import com.flexifeed.app.data.model.SDUIResponseDTO
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * Retrofit HTTP API Interface for Server-Driven UI endpoints.
 */
interface SDUIApi {

    @GET("api/v1/home-feed")
    suspend fun getHomeFeed(
        @Query("campaign") campaign: String? = null
    ): SDUIResponseDTO
}
