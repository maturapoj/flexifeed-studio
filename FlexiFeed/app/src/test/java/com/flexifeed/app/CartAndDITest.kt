package com.flexifeed.app

import com.flexifeed.app.data.repository.SDUIRepositoryImpl
import com.flexifeed.app.ui.home.CartViewModel
import com.flexifeed.app.ui.home.HomeViewModel
import com.flexifeed.app.ui.state.CartItem
import kotlinx.coroutines.Dispatchers
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class CartAndDITest {

    private lateinit var cartViewModel: CartViewModel

    @Before
    fun setUp() {
        cartViewModel = CartViewModel()
    }

    @Test
    fun `addToCart with rich parameters stores CartItem properly and updates counts`() {
        cartViewModel.addToCart(
            productId = "prod_99",
            quantity = 2,
            name = "หูฟัง Sony ANC",
            priceFormatted = "฿1,590",
            imageUrl = "https://picsum.photos/200/200"
        )

        assertEquals(2, cartViewModel.totalItemCount.value)
        val items = cartViewModel.items.value
        assertTrue(items.containsKey("prod_99"))
        val item = items["prod_99"]
        assertNotNull(item)
        assertEquals("หูฟัง Sony ANC", item!!.name)
        assertEquals(2, item.quantity)
        assertEquals(1590.0, item.priceNumeric, 0.01)
        assertEquals(3180.0, cartViewModel.calculateTotal(), 0.01)
    }

    @Test
    fun `incrementQuantity and decrementQuantity update count and price`() {
        cartViewModel.addToCart(
            productId = "mouse_1",
            quantity = 1,
            name = "Wireless Mouse",
            priceFormatted = "฿500"
        )
        assertEquals(1, cartViewModel.totalItemCount.value)

        cartViewModel.incrementQuantity("mouse_1")
        assertEquals(2, cartViewModel.totalItemCount.value)
        assertEquals(1000.0, cartViewModel.calculateTotal(), 0.01)

        cartViewModel.decrementQuantity("mouse_1")
        assertEquals(1, cartViewModel.totalItemCount.value)
        assertEquals(500.0, cartViewModel.calculateTotal(), 0.01)

        // Decrementing again should remove it from cart
        cartViewModel.decrementQuantity("mouse_1")
        assertEquals(0, cartViewModel.totalItemCount.value)
        assertTrue(cartViewModel.items.value.isEmpty())
    }

    @Test
    fun `checkout clears cart and returns OrderSummary with ID and formatted total`() {
        cartViewModel.addToCart(
            productId = "kb_1",
            quantity = 1,
            name = "Keyboard RGB",
            priceFormatted = "฿2,000"
        )
        cartViewModel.addToCart(
            productId = "pad_1",
            quantity = 1,
            name = "Mousepad XL",
            priceFormatted = "฿500"
        )

        assertEquals(2, cartViewModel.totalItemCount.value)
        assertEquals(2500.0, cartViewModel.calculateTotal(), 0.01)

        val checkoutResult = cartViewModel.checkout()
        assertTrue(checkoutResult.orderId.startsWith("ORD-"))
        assertEquals(2, checkoutResult.itemsCount)
        assertEquals(2500.0, checkoutResult.totalAmount, 0.01)

        // Cart should be empty after checkout
        assertEquals(0, cartViewModel.totalItemCount.value)
        assertTrue(cartViewModel.items.value.isEmpty())
    }

    @Test
    fun `HomeViewModel supports pull to refresh with isRefreshing state flow`() {
        val homeViewModel = HomeViewModel(
            repository = SDUIRepositoryImpl(),
            dispatcher = Dispatchers.Unconfined
        )
        assertFalse(homeViewModel.isRefreshing.value)
        homeViewModel.refreshFeed(isPullToRefresh = true)
        assertNotNull(homeViewModel.isRefreshing)
    }
}
