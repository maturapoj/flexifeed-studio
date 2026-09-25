package com.flexifeed.app.ui.sdui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import com.flexifeed.app.domain.action.AnalyticsEvent
import com.flexifeed.app.ui.home.CartBottomSheetContent
import com.flexifeed.app.ui.home.components.AnalyticsInspectorSheet
import com.flexifeed.app.ui.home.components.NavigationPreviewSheet
import com.flexifeed.app.ui.state.CartItem
import com.flexifeed.app.ui.state.CheckoutResult

/**
 * Shared bottom sheets for SDUI screens:
 * 1. Interactive Cart Modal Bottom Sheet
 * 2. Real-time Analytics Event Inspector Sheet
 * 3. Deep Link Navigation Preview Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SDUICommonSheets(
    isCartVisible: Boolean,
    cartItems: List<CartItem>,
    cartTotalCount: Int,
    cartTotalPriceFormatted: String,
    onCartIncrement: (String) -> Unit,
    onCartDecrement: (String) -> Unit,
    onCartRemove: (String) -> Unit,
    onCartClear: () -> Unit,
    onCartCheckout: () -> CheckoutResult,
    onDismissCart: () -> Unit,
    isAnalyticsVisible: Boolean,
    analyticsEvents: List<AnalyticsEvent>,
    onAnalyticsClear: () -> Unit,
    onDismissAnalytics: () -> Unit,
    targetNavigationUrl: String?,
    onDismissNavigationPreview: () -> Unit
) {
    if (isCartVisible) {
        ModalBottomSheet(
            onDismissRequest = onDismissCart,
            sheetState = rememberModalBottomSheetState()
        ) {
            CartBottomSheetContent(
                cartItems = cartItems,
                totalCount = cartTotalCount,
                totalPriceFormatted = cartTotalPriceFormatted,
                onIncrement = onCartIncrement,
                onDecrement = onCartDecrement,
                onRemove = onCartRemove,
                onClearCart = onCartClear,
                onCheckout = onCartCheckout,
                onClose = onDismissCart
            )
        }
    }

    if (isAnalyticsVisible) {
        AnalyticsInspectorSheet(
            events = analyticsEvents,
            onClear = onAnalyticsClear,
            onDismiss = onDismissAnalytics
        )
    }

    targetNavigationUrl?.let { url ->
        NavigationPreviewSheet(
            targetUrl = url,
            onDismiss = onDismissNavigationPreview
        )
    }
}
