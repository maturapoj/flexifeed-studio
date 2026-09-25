package com.flexifeed.app

import android.app.Application
import com.flexifeed.app.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.logger.Level

class FlexiFeedApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@FlexiFeedApp)
        }
    }
}
