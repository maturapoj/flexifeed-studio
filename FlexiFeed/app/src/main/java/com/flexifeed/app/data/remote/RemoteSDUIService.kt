package com.flexifeed.app.data.remote

import com.flexifeed.app.BuildConfig
import com.flexifeed.app.data.model.SDUIResponseDTO
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit-powered implementation of SDUIService communicating
 * with the Server-Driven UI backend (configured via build.gradle.kts BuildConfig).
 */
class RemoteSDUIService(
    val baseUrl: String = BuildConfig.SDUI_BASE_URL,
    private val gson: Gson = Gson(),
    apiClient: SDUIApi? = null
) : SDUIService {

    private val api: SDUIApi = apiClient ?: createRetrofitApi(baseUrl, gson)

    override suspend fun getHomeFeed(campaign: CampaignType): String {
        val campaignParam = when (campaign) {
            CampaignType.TECH_WEEKEND -> "tech-weekend"
            CampaignType.DEFAULT_FEED -> "mega-sale"
        }
        val dto = api.getHomeFeed(campaignParam)
        return gson.toJson(dto)
    }

    suspend fun getHomeFeedDTO(campaign: CampaignType = CampaignType.DEFAULT_FEED): SDUIResponseDTO {
        val campaignParam = when (campaign) {
            CampaignType.TECH_WEEKEND -> "tech-weekend"
            CampaignType.DEFAULT_FEED -> "mega-sale"
        }
        return api.getHomeFeed(campaignParam)
    }

    companion object {
        fun createRetrofitApi(baseUrl: String, gson: Gson = Gson()): SDUIApi {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(2500, TimeUnit.MILLISECONDS)
                .readTimeout(2500, TimeUnit.MILLISECONDS)
                .addInterceptor(logging)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            return retrofit.create(SDUIApi::class.java)
        }
    }
}
