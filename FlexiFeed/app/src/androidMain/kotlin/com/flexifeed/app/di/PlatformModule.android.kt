package com.flexifeed.app.di

import com.flexifeed.app.BuildConfig
import com.flexifeed.app.FirebasePlatformAnalyticsLogger
import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.domain.action.PlatformAnalyticsLogger
import com.chuckerteam.chucker.api.ChuckerInterceptor
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.okhttp.OkHttp
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.Module
import org.koin.dsl.module

actual val defaultBaseUrl: String = BuildConfig.SDUI_BASE_URL

actual val platformModule: Module = module {
    single<PlatformAnalyticsLogger> {
        FirebasePlatformAnalyticsLogger(androidContext())
    }
    single {
        AnalyticsTracker(platformLogger = getOrNull())
    }
    single<HttpClientEngine> {
        OkHttp.create {
            addInterceptor(ChuckerInterceptor.Builder(androidContext()).build())
        }
    }
}
