package com.flexifeed.app.ui.sdui

import androidx.navigation.NavController
import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.ui.home.CartViewModel

class ActionDispatcher(
    private val navController: NavController,
    private val cartViewModel: CartViewModel,
    private val analyticsTracker: AnalyticsTracker,
    private val onNavigateUrl: ((String) -> Unit)? = null
) {
    private val tag = "ActionDispatcher"

    fun handleAction(action: SDUIAction) {
        println("[$tag] Dispatching action type=${action.type} payload=${action.payload}")
        when (action.type) {
            SDUIConstants.ActionType.NAVIGATE -> {
                val url = action.getTargetUrl() ?: return
                analyticsTracker.logEvent("navigate", mapOf(SDUIConstants.ActionKey.TARGET to url))
                onNavigateUrl?.invoke(url)
                try {
                    val clean = url.substringAfter("://", "")
                    val parts = clean.split('/')
                    val host = parts.getOrNull(0)
                    val path = parts.drop(1).joinToString("/")
                    val screenId = if (!host.isNullOrEmpty() && path.isNotEmpty()) {
                        "${host}_$path".replace("-", "_")
                    } else {
                        "home"
                    }
                    navController.navigate("sdui/$screenId")
                } catch (e: Exception) {
                    println("[$tag] NavController direct navigate fallback for url: $url (${e.message})")
                }
            }
            SDUIConstants.ActionType.ADD_TO_CART -> {
                val productId = action.getProductId() ?: return
                val quantity = action.getQuantity()
                val name = action.getName() ?: "Product"
                val price = action.getPrice() ?: ""
                val imageUrl = action.getImageUrl() ?: ""
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
                println("[$tag] Unknown action type: ${action.type}")
            }
        }
    }
}
