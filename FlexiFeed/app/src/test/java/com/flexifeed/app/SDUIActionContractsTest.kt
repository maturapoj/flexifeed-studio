package com.flexifeed.app

import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.ui.home.CartViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class SDUIActionContractsTest {

    @Test
    fun testSDUIActionPayloadHelpers() {
        val navAction = SDUIAction(
            type = SDUIConstants.ActionType.NAVIGATE,
            payload = mapOf(SDUIConstants.ActionKey.TARGET to "flexifeed://campaign/mega-sale")
        )
        assertEquals("flexifeed://campaign/mega-sale", navAction.getTargetUrl())

        val cartAction = SDUIAction(
            type = SDUIConstants.ActionType.ADD_TO_CART,
            payload = mapOf(SDUIConstants.ActionKey.PRODUCT_ID to "201", SDUIConstants.ActionKey.QUANTITY to 2)
        )
        assertEquals("201", cartAction.getProductId())
        assertEquals(2, cartAction.getQuantity())

        val analyticsAction = SDUIAction(
            type = SDUIConstants.ActionType.ANALYTICS,
            payload = mapOf(SDUIConstants.ActionKey.EVENT to "banner_click", SDUIConstants.ActionKey.ID to "banner_01")
        )
        assertEquals("banner_click", analyticsAction.getEventName())
    }

    @Test
    fun testAnalyticsTrackerLogsEvents() {
        val tracker = AnalyticsTracker()
        assertEquals(0, tracker.events.value.size)

        tracker.logEvent("banner_click", mapOf("bannerId" to "mega_sale"))
        assertEquals(1, tracker.events.value.size)
        assertEquals("banner_click", tracker.events.value[0].eventName)
        assertEquals("mega_sale", tracker.events.value[0].parameters["bannerId"])

        tracker.logEvent("add_to_cart", mapOf("productId" to "101"))
        assertEquals(2, tracker.events.value.size)
        assertEquals("add_to_cart", tracker.events.value[0].eventName)

        tracker.clearEvents()
        assertEquals(0, tracker.events.value.size)
    }

    @Test
    fun testCartViewModel() {
        val cartViewModel = CartViewModel()
        assertEquals(0, cartViewModel.totalItemCount.value)

        cartViewModel.addToCart("prod_101", 1)
        assertEquals(1, cartViewModel.totalItemCount.value)
        assertEquals(1, cartViewModel.cartItems.value["prod_101"])

        cartViewModel.addToCart("prod_101", 2)
        assertEquals(3, cartViewModel.totalItemCount.value)
        assertEquals(3, cartViewModel.cartItems.value["prod_101"])

        cartViewModel.addToCart("prod_201", 1)
        assertEquals(4, cartViewModel.totalItemCount.value)

        cartViewModel.clearCart()
        assertEquals(0, cartViewModel.totalItemCount.value)
        assertTrue(cartViewModel.cartItems.value.isEmpty())
    }
}
