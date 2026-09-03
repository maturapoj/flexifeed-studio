package com.flexifeed.app.ui.sdui

import android.net.Uri
import android.util.Log
import androidx.navigation.NavController
import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.ui.home.CartViewModel

class ActionDispatcher(
    private val navController: NavController,
    private val cartViewModel: CartViewModel,
    private val analyticsTracker: AnalyticsTracker,
    private val onNavigateUrl: ((String) -> Unit)? = null
) {
    private val tag = "ActionDispatcher"

    fun handleAction(action: SDUIAction) {
        Log.d(tag, "Dispatching action type=${action.type} payload=${action.payload}")
        when (action.type) {
            "NAVIGATE" -> {
                val url = action.payload["target"] as? String ?: return
                analyticsTracker.logEvent("navigate", mapOf("target" to url))
                onNavigateUrl?.invoke(url)
                try {
                    navController.navigate(Uri.parse(url))
                } catch (e: Exception) {
                    Log.w(tag, "NavController direct navigate fallback for url: $url (${e.localizedMessage})")
                }
            }
            "ADD_TO_CART" -> {
                val productId = action.payload["productId"]?.toString() ?: return
                val quantity = when (val q = action.payload["quantity"]) {
                    is Number -> q.toInt()
                    is String -> q.toIntOrNull() ?: 1
                    else -> 1
                }
                cartViewModel.addToCart(productId, quantity)
                analyticsTracker.logEvent("add_to_cart", mapOf("productId" to productId, "quantity" to quantity))
            }
            "ANALYTICS" -> {
                val eventName = action.payload["event"] as? String ?: return
                analyticsTracker.logEvent(eventName, action.payload)
            }
            else -> {
                Log.w(tag, "Unknown action type: ${action.type}")
            }
        }
    }
}
