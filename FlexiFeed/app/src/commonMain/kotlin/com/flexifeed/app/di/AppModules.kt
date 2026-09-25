package com.flexifeed.app.di

import com.flexifeed.app.data.remote.KtorSDUIApi
import com.flexifeed.app.data.remote.MockSDUIService
import com.flexifeed.app.data.remote.RemoteSDUIStreamService
import com.flexifeed.app.data.remote.SDUIApi
import com.flexifeed.app.data.repository.SDUIRepositoryImpl
import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.domain.repository.SDUIRepository
import com.flexifeed.app.domain.repository.SDUIStreamService
import com.flexifeed.app.ui.home.CartViewModel
import com.flexifeed.app.ui.home.HomeViewModel
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

expect val platformModule: Module

expect val defaultBaseUrl: String

val commonNetworkModule = module {
    single {
        com.flexifeed.app.util.NetworkLogCollector()
    }

    single {
        Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = true
        }
    }

    single {
        val engine = getOrNull<io.ktor.client.engine.HttpClientEngine>()
        if (engine != null) {
            HttpClient(engine) {
                install(ContentNegotiation) {
                    json(get<Json>())
                }
                install(Logging) {
                    level = LogLevel.INFO
                }
            }
        } else {
            HttpClient {
                install(ContentNegotiation) {
                    json(get<Json>())
                }
                install(Logging) {
                    level = LogLevel.INFO
                }
            }
        }
    }

    single<SDUIApi> {
        KtorSDUIApi(
            client = get(),
            baseUrl = defaultBaseUrl,
            logCollector = getOrNull()
        )
    }

    single<SDUIStreamService> {
        RemoteSDUIStreamService(
            client = get(),
            baseUrl = defaultBaseUrl,
            logCollector = getOrNull()
        )
    }
}

val repositoryModule = module {
    single { MockSDUIService(json = get()) }
    single<SDUIRepository> {
        SDUIRepositoryImpl(
            api = get(),
            mockService = get(),
            json = get()
        )
    }
}

val domainModule = module {
    single {
        AnalyticsTracker()
    }
}

val viewModelModule = module {
    viewModel { HomeViewModel(repository = get(), streamService = get()) }
    viewModel { CartViewModel() }
}

val commonAppModules = listOf(
    commonNetworkModule,
    repositoryModule,
    domainModule,
    viewModelModule
)

fun initKoin(appDeclaration: KoinAppDeclaration = {}) = startKoin {
    appDeclaration()
    modules(commonAppModules + platformModule)
}
