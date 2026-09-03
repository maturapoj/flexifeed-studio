package com.flexifeed.app.ui.state

/**
 * Rich domain/UI data class for an item inside the shopping cart.
 */
data class CartItem(
    val id: String,
    val name: String,
    val priceNumeric: Double,
    val priceFormatted: String,
    val imageUrl: String,
    val quantity: Int
)

/**
 * Event payload when an item is added to cart.
 */
data class CartItemAdded(
    val productId: String,
    val quantity: Int
)

/**
 * Simulated checkout result payload.
 */
data class CheckoutResult(
    val orderId: String,
    val totalAmount: Double,
    val totalAmountFormatted: String,
    val itemsCount: Int
)
