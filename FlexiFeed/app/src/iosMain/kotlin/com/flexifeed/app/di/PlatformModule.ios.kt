package com.flexifeed.app.di

import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.darwin.Darwin
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSBundle
import platform.Foundation.NSProcessInfo

actual val defaultBaseUrl: String
    get() {
        // 1. Check Process Environment (passed dynamically via Xcode Scheme)
        val envUrl = NSProcessInfo.processInfo.environment["SDUI_BASE_URL"] as? String
        if (!envUrl.isNullOrBlank()) return envUrl.trim()

        // 2. Check Info.plist value (bound to Xcode Build Settings)
        val plistUrl = NSBundle.mainBundle.objectForInfoDictionaryKey("SDUI_BASE_URL") as? String
        if (!plistUrl.isNullOrBlank() && !plistUrl.startsWith("$(")) return plistUrl.trim()

        // 3. Default fallback to Develop Server
        return "https://flexifeed-studio.onrender.com/"
    }

fun getIosEnvironmentName(): String {
    val envName = NSProcessInfo.processInfo.environment["APP_ENVIRONMENT"] as? String
    if (!envName.isNullOrBlank()) return envName.trim()

    val plistEnv = NSBundle.mainBundle.objectForInfoDictionaryKey("APP_ENVIRONMENT") as? String
    if (!plistEnv.isNullOrBlank() && !plistEnv.startsWith("$(")) return plistEnv.trim()

    return "dev"
}

actual val platformModule: Module = module {
    single<HttpClientEngine> {
        Darwin.create()
    }
}
