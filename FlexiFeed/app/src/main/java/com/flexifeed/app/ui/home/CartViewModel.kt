package com.flexifeed.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class CartItemAdded(
    val productId: String,
    val quantity: Int
)

class CartViewModel : ViewModel() {

    private val _cartItems = MutableStateFlow<Map<String, Int>>(emptyMap())
    val cartItems: StateFlow<Map<String, Int>> = _cartItems.asStateFlow()

    private val _totalItemCount = MutableStateFlow(0)
    val totalItemCount: StateFlow<Int> = _totalItemCount.asStateFlow()

    private val _itemAddedEvent = MutableSharedFlow<CartItemAdded>(extraBufferCapacity = 1)
    val itemAddedEvent: SharedFlow<CartItemAdded> = _itemAddedEvent.asSharedFlow()

    fun addToCart(productId: String, quantity: Int = 1) {
        val current = _cartItems.value.toMutableMap()
        current[productId] = (current[productId] ?: 0) + quantity
        _cartItems.value = current
        _totalItemCount.value = current.values.sum()
        _itemAddedEvent.tryEmit(CartItemAdded(productId, quantity))
    }

    fun clearCart() {
        _cartItems.value = emptyMap()
        _totalItemCount.value = 0
    }
}
