package com.flexifeed.app.domain.action

/**
 * Domain representation of an SDUI Action contract.
 */
data class SDUIAction(
    val type: String,
    val payload: Map<String, Any?> = emptyMap()
) {
    companion object {
        const val TYPE_NAVIGATE = "NAVIGATE"
        const val TYPE_ADD_TO_CART = "ADD_TO_CART"
        const val TYPE_ANALYTICS = "ANALYTICS"
    }

    fun getTargetUrl(): String? = payload["target"] as? String

    fun getProductId(): String? = (payload["productId"] ?: payload["id"])?.toString()

    fun getQuantity(): Int {
        val qty = payload["quantity"]
        return when (qty) {
            is Number -> qty.toInt()
            is String -> qty.toIntOrNull() ?: 1
            else -> 1
        }
    }

    fun getEventName(): String? = (payload["event"] ?: payload["eventName"]) as? String
}
