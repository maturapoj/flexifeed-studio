package com.flexifeed.app.di

import com.flexifeed.app.BuildConfig
import com.flexifeed.app.data.remote.MockSDUIService
import com.flexifeed.app.data.remote.RemoteSDUIStreamService
import com.flexifeed.app.data.remote.SDUIApi
import com.flexifeed.app.data.repository.SDUIRepositoryImpl
import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.domain.repository.SDUIRepository
import com.flexifeed.app.domain.repository.SDUIStreamService
import com.flexifeed.app.ui.home.CartViewModel
import com.flexifeed.app.ui.home.HomeViewModel
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import com.google.firebase.analytics.FirebaseAnalytics
import org.koin.android.ext.koin.androidContext
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
            .addInterceptor(ChuckerInterceptor.Builder(androidContext()).build())
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
    single { MockSDUIService() }
    single<SDUIRepository> {
        SDUIRepositoryImpl(
            api = get(),
            mockService = get(),
            gson = get()
        )
    }
}

val domainModule = module {
    single {
        val firebaseAnalytics = try {
            FirebaseAnalytics.getInstance(androidContext())
        } catch (_: Throwable) {
            null
        }
        AnalyticsTracker(firebaseAnalytics)
    }
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
