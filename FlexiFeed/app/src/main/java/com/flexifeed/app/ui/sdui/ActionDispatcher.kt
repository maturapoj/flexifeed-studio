package com.flexifeed.app.ui.sdui

import android.net.Uri
import android.util.Log
import androidx.navigation.NavController
import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.ui.home.CartViewModel

import com.flexifeed.app.domain.model.SDUIConstants

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
            SDUIConstants.ActionType.NAVIGATE -> {
                val url = action.getTargetUrl() ?: return
                analyticsTracker.logEvent("navigate", mapOf(SDUIConstants.ActionKey.TARGET to url))
                onNavigateUrl?.invoke(url)
                try {
                    navController.navigate(Uri.parse(url))
                } catch (e: Exception) {
                    Log.w(tag, "NavController direct navigate fallback for url: $url (${e.localizedMessage})")
                }
            }
            SDUIConstants.ActionType.ADD_TO_CART -> {
                val productId = action.getProductId() ?: return
                val quantity = action.getQuantity()
                val name = (action.payload[SDUIConstants.PropKey.NAME] as? String) ?: "สินค้า FlexiFeed"
                val price = (action.payload[SDUIConstants.PropKey.PRICE] as? String) ?: "฿890"
                val imageUrl = ((action.payload[SDUIConstants.PropKey.IMAGE_URL] ?: action.payload[SDUIConstants.PropKey.THUMBNAIL_URL]) as? String) ?: ""
                cartViewModel.addToCart(productId, quantity, name, price, imageUrl)
                analyticsTracker.logEvent("add_to_cart", mapOf(
                    SDUIConstants.ActionKey.PRODUCT_ID to productId,
                    SDUIConstants.ActionKey.QUANTITY to quantity,
                    SDUIConstants.PropKey.NAME to name
                ))
            }
            SDUIConstants.ActionType.ANALYTICS -> {
                val eventName = action.getEventName() ?: return
                analyticsTracker.logEvent(eventName, action.payload)
            }
            else -> {
                Log.w(tag, "Unknown action type: ${action.type}")
            }
        }
    }
}
