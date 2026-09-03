package com.flexifeed.app.ui.home

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.NumberFormat
import java.util.Locale

/**
 * Rich domain model for an item inside the shopping cart.
 */
data class CartItem(
    val id: String,
    val name: String,
    val priceNumeric: Double,
    val priceFormatted: String,
    val imageUrl: String,
    val quantity: Int
)

data class CartItemAdded(
    val productId: String,
    val quantity: Int
)

data class CheckoutResult(
    val orderId: String,
    val totalAmount: Double,
    val totalAmountFormatted: String,
    val itemsCount: Int
)

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
        name: String = "สินค้า FlexiFeed",
        priceFormatted: String = "฿890",
        imageUrl: String = ""
    ) {
        val current = _itemsMap.value.toMutableMap()
        val existing = current[productId]
        val newQty = (existing?.quantity ?: 0) + quantity
        val parsedPrice = parsePrice(priceFormatted)

        current[productId] = CartItem(
            id = productId,
            name = if (!existing?.name.isNullOrEmpty() && existing?.name != "สินค้า FlexiFeed") existing!!.name else name,
            priceNumeric = if (existing != null && existing.priceNumeric > 0.0) existing.priceNumeric else parsedPrice,
            priceFormatted = if (!existing?.priceFormatted.isNullOrEmpty() && existing?.priceFormatted != "฿0") existing!!.priceFormatted else priceFormatted,
            imageUrl = if (!existing?.imageUrl.isNullOrEmpty()) existing!!.imageUrl else imageUrl,
            quantity = newQty
        )

        _itemsMap.value = current
        _cartItems.value = current.mapValues { it.value.quantity }
        _totalItemCount.value = current.values.sumOf { it.quantity }
        _itemAddedEvent.tryEmit(CartItemAdded(productId, quantity))
    }

    fun incrementQuantity(productId: String) {
        val current = _itemsMap.value.toMutableMap()
        val item = current[productId] ?: return
        current[productId] = item.copy(quantity = item.quantity + 1)
        _itemsMap.value = current
        _cartItems.value = current.mapValues { it.value.quantity }
        _totalItemCount.value = current.values.sumOf { it.quantity }
    }

    fun decrementQuantity(productId: String) {
        val current = _itemsMap.value.toMutableMap()
        val item = current[productId] ?: return
        if (item.quantity <= 1) {
            current.remove(productId)
        } else {
            current[productId] = item.copy(quantity = item.quantity - 1)
        }
        _itemsMap.value = current
        _cartItems.value = current.mapValues { it.value.quantity }
        _totalItemCount.value = current.values.sumOf { it.quantity }
    }

    fun removeItem(productId: String) {
        val current = _itemsMap.value.toMutableMap()
        current.remove(productId)
        _itemsMap.value = current
        _cartItems.value = current.mapValues { it.value.quantity }
        _totalItemCount.value = current.values.sumOf { it.quantity }
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
        _itemsMap.value = emptyMap()
        _cartItems.value = emptyMap()
        _totalItemCount.value = 0
    }

    private fun parsePrice(str: String): Double {
        val clean = str.replace("[^0-9.]".toRegex(), "")
        return clean.toDoubleOrNull() ?: 490.0
    }
}
