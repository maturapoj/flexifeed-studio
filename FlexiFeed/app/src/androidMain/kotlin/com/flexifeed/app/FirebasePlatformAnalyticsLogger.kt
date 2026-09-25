package com.flexifeed.app

import android.content.Context
import android.os.Bundle
import com.flexifeed.app.domain.action.PlatformAnalyticsLogger
import com.google.firebase.analytics.FirebaseAnalytics

class FirebasePlatformAnalyticsLogger(context: Context) : PlatformAnalyticsLogger {
    private val firebaseAnalytics: FirebaseAnalytics? = try {
        FirebaseAnalytics.getInstance(context)
    } catch (_: Throwable) {
        null
    }

    override fun logEvent(name: String, parameters: Map<String, Any?>) {
        val fa = firebaseAnalytics ?: return
        val bundle = Bundle()
        parameters.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putFloat(key, value)
                is Boolean -> bundle.putBoolean(key, value)
                else -> bundle.putString(key, value?.toString() ?: "")
            }
        }
        fa.logEvent(name, bundle)
    }
}
