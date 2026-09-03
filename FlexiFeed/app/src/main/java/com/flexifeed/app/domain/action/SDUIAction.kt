package com.flexifeed.app.domain.action

import com.flexifeed.app.domain.model.SDUIConstants

/**
 * Domain representation of an SDUI Action contract.
 */
data class SDUIAction(
    val type: String,
    val payload: Map<String, Any?> = emptyMap()
) {
    companion object {
        const val TYPE_NAVIGATE = SDUIConstants.ActionType.NAVIGATE
        const val TYPE_ADD_TO_CART = SDUIConstants.ActionType.ADD_TO_CART
        const val TYPE_ANALYTICS = SDUIConstants.ActionType.ANALYTICS
    }

    fun getTargetUrl(): String? = payload[SDUIConstants.ActionKey.TARGET] as? String

    fun getProductId(): String? = (payload[SDUIConstants.ActionKey.PRODUCT_ID] ?: payload[SDUIConstants.ActionKey.ID])?.toString()

    fun getQuantity(): Int {
        val qty = payload[SDUIConstants.ActionKey.QUANTITY]
        return when (qty) {
            is Number -> qty.toInt()
            is String -> qty.toIntOrNull() ?: 1
            else -> 1
        }
    }

    fun getEventName(): String? = (payload[SDUIConstants.ActionKey.EVENT] ?: payload[SDUIConstants.ActionKey.EVENT_NAME]) as? String
}
