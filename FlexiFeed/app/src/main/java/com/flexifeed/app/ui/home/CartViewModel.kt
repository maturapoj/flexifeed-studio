package com.flexifeed.app.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import com.flexifeed.app.ui.state.CartItem
import com.flexifeed.app.ui.state.CartItemAdded
import com.flexifeed.app.ui.state.CheckoutResult
import java.text.NumberFormat
import java.util.Locale

class CartViewModel : ViewModel() {

    private val _itemsMap = MutableStateFlow<Map<String, CartItem>>(emptyMap())
    val items: StateFlow<Map<String, CartItem>> = _itemsMap.asStateFlow()

    // Backwards-compatible map for simple ID -> Quantity lookup
    private val _cartItems = MutableStateFlow<Map<String, Int>>(emptyMap())
    val cartItems: StateFlow<Map<String, Int>> = _cartItems.asStateFlow()

    private val _totalItemCount = MutableStateFlow(0)
    val totalItemCount: StateFlow<Int> = _totalItemCount.asStateFlow()

    private val _itemAddedEvent = MutableSharedFlow<CartItemAdded>(extraBufferCapacity = 1)
    val itemAddedEvent: SharedFlow<CartItemAdded> = _itemAddedEvent.asSharedFlow()

    private val _checkoutEvent = MutableSharedFlow<CheckoutResult>(extraBufferCapacity = 1)
    val checkoutEvent: SharedFlow<CheckoutResult> = _checkoutEvent.asSharedFlow()

    fun addToCart(
        productId: String,
        quantity: Int = 1,
        name: String = "Product",
        priceFormatted: String = "",
        imageUrl: String = ""
    ) {
        val current = _itemsMap.value.toMutableMap()
        val existing = current[productId]
        val newQty = (existing?.quantity ?: 0) + quantity
        val parsedPrice = parsePrice(priceFormatted)

        val resolvedName = existing?.name?.takeIf { it.isNotBlank() } ?: name
        val resolvedPriceNumeric = if (existing != null && existing.priceNumeric > 0.0) existing.priceNumeric else parsedPrice
        val resolvedPriceFormatted = existing?.priceFormatted?.takeIf { it.isNotBlank() } ?: priceFormatted
        val resolvedImageUrl = existing?.imageUrl?.takeIf { it.isNotBlank() } ?: imageUrl

        current[productId] = CartItem(
            id = productId,
            name = resolvedName,
            priceNumeric = resolvedPriceNumeric,
            priceFormatted = resolvedPriceFormatted,
            imageUrl = resolvedImageUrl,
            quantity = newQty
        )

        updateCartState(current)
        _itemAddedEvent.tryEmit(CartItemAdded(productId, quantity))
    }

    fun incrementQuantity(productId: String) {
        val current = _itemsMap.value.toMutableMap()
        val item = current[productId] ?: return
        current[productId] = item.copy(quantity = item.quantity + 1)
        updateCartState(current)
    }

    fun decrementQuantity(productId: String) {
        val current = _itemsMap.value.toMutableMap()
        val item = current[productId] ?: return
        if (item.quantity <= 1) {
            current.remove(productId)
        } else {
            current[productId] = item.copy(quantity = item.quantity - 1)
        }
        updateCartState(current)
    }

    fun removeItem(productId: String) {
        val current = _itemsMap.value.toMutableMap()
        current.remove(productId)
        updateCartState(current)
    }

    fun calculateTotal(): Double {
        return _itemsMap.value.values.sumOf { it.priceNumeric * it.quantity }
    }

    fun calculateTotalFormatted(): String {
        val total = calculateTotal()
        return "฿" + NumberFormat.getNumberInstance(Locale.US).format(total)
    }

    fun checkout(): CheckoutResult {
        val total = calculateTotal()
        val count = _totalItemCount.value
        val result = CheckoutResult(
            orderId = "ORD-" + (100000..999999).random(),
            totalAmount = total,
            totalAmountFormatted = calculateTotalFormatted(),
            itemsCount = count
        )
        clearCart()
        _checkoutEvent.tryEmit(result)
        return result
    }

    fun clearCart() {
        updateCartState(emptyMap())
    }

    private fun updateCartState(newMap: Map<String, CartItem>) {
        _itemsMap.value = newMap
        _cartItems.value = newMap.mapValues { it.value.quantity }
        _totalItemCount.value = newMap.values.sumOf { it.quantity }
    }

    private fun parsePrice(str: String): Double {
        val clean = str.replace("[^0-9.]".toRegex(), "")
        return clean.toDoubleOrNull() ?: 0.0
    }
}
