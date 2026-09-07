package com.flexifeed.app.di

import com.flexifeed.app.BuildConfig
import com.flexifeed.app.data.remote.MockSDUIService
import com.flexifeed.app.data.remote.RemoteSDUIService
import com.flexifeed.app.data.remote.RemoteSDUIStreamService
import com.flexifeed.app.data.remote.SDUIApi
import com.flexifeed.app.data.repository.SDUIRepositoryImpl
import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.domain.repository.SDUIRepository
import com.flexifeed.app.domain.repository.SDUIStreamService
import com.flexifeed.app.domain.usecase.GetHomeFeedUseCase
import com.flexifeed.app.domain.usecase.ObserveFeedStreamUseCase
import com.flexifeed.app.ui.home.CartViewModel
import com.flexifeed.app.ui.home.HomeViewModel
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

val networkModule = module {
    single { Gson() }

    single {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
        }
        OkHttpClient.Builder()
            .connectTimeout(3000, TimeUnit.MILLISECONDS)
            .readTimeout(3000, TimeUnit.MILLISECONDS)
            .addInterceptor(logging)
            .build()
    }

    single {
        Retrofit.Builder()
            .baseUrl(BuildConfig.SDUI_BASE_URL)
            .client(get())
            .addConverterFactory(GsonConverterFactory.create(get()))
            .build()
    }

    single<SDUIApi> {
        get<Retrofit>().create(SDUIApi::class.java)
    }

    single<SDUIStreamService> {
        RemoteSDUIStreamService(
            okHttpClient = get(),
            baseUrl = BuildConfig.SDUI_BASE_URL
        )
    }
}

val repositoryModule = module {
    single {
        RemoteSDUIService(
            baseUrl = BuildConfig.SDUI_BASE_URL,
            gson = get(),
            apiClient = get()
        )
    }
    single { MockSDUIService() }
    single<SDUIRepository> {
        SDUIRepositoryImpl(
            remoteService = get<RemoteSDUIService>(),
            mockService = get<MockSDUIService>(),
            gson = get()
        )
    }
}

val domainModule = module {
    single { AnalyticsTracker() }
    factory { GetHomeFeedUseCase(repository = get()) }
    factory { ObserveFeedStreamUseCase(streamService = get()) }
}

val viewModelModule = module {
    viewModel { HomeViewModel(repository = get(), streamService = get()) }
    viewModel { CartViewModel() }
}

val appModules = listOf(
    networkModule,
    repositoryModule,
    domainModule,
    viewModelModule
)
