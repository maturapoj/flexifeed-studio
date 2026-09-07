package com.flexifeed.app.ui.home

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flexifeed.app.data.remote.CampaignType
import com.flexifeed.app.domain.action.AnalyticsEvent
import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.domain.model.SDUINode
import com.flexifeed.app.domain.model.SDUIScreen
import com.flexifeed.app.ui.state.CheckoutResult
import com.flexifeed.app.ui.home.components.AnalyticsInspectorSheet
import com.flexifeed.app.ui.home.components.CampaignSwitcherBar
import com.flexifeed.app.ui.home.components.HomeFeedContent
import com.flexifeed.app.ui.home.components.HomeSearchBar
import com.flexifeed.app.ui.home.components.HomeTopAppBar
import com.flexifeed.app.ui.home.components.NavigationPreviewSheet
import com.flexifeed.app.ui.sdui.ActionDispatcher
import com.flexifeed.app.ui.state.CartItem
import com.flexifeed.app.ui.state.HomeIntent
import com.flexifeed.app.ui.state.HomeSideEffect
import com.flexifeed.app.ui.state.HomeUiState
import com.flexifeed.app.ui.state.SDUIFeedUiState
import com.flexifeed.app.ui.theme.FlexiFeedTheme

/**
 * Stateful Coordinator Composable for the Home Screen.
 * Connects Koin ViewModels, collects MVI StateFlow and SideEffects, and delegates to [HomeScreenContent].
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    cartViewModel: CartViewModel,
    analyticsTracker: AnalyticsTracker,
    actionDispatcher: ActionDispatcher,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val cartCount by cartViewModel.totalItemCount.collectAsStateWithLifecycle()
    val richCartItems by cartViewModel.items.collectAsStateWithLifecycle()
    val analyticsEvents by analyticsTracker.events.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Listen for cart addition events
    LaunchedEffect(cartViewModel) {
        cartViewModel.itemAddedEvent.collect { added ->
            snackbarHostState.showSnackbar("🛒 เพิ่มสินค้า #${added.productId} ลงในตะกร้าแล้ว!")
        }
    }

    // Listen for MVI ViewModel SideEffects
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeSideEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    // Pass hoisted state to the pure stateless content container
    val hoistedState = state.copy(
        cartCount = cartCount,
        analyticsEventsCount = analyticsEvents.size
    )

    HomeScreenContent(
        state = hoistedState,
        richCartItems = richCartItems.values.toList(),
        cartTotalPriceFormatted = cartViewModel.calculateTotalFormatted(),
        analyticsEvents = analyticsEvents,
        onIntent = viewModel::onIntent,
        onCartIncrement = cartViewModel::incrementQuantity,
        onCartDecrement = cartViewModel::decrementQuantity,
        onCartRemove = cartViewModel::removeItem,
        onCartClear = cartViewModel::clearCart,
        onCartCheckout = cartViewModel::checkout,
        onAnalyticsClear = analyticsTracker::clearEvents,
        onAction = { action ->
            viewModel.onIntent(HomeIntent.HandleSDUIAction(action))
            actionDispatcher.handleAction(action)
        },
        snackbarHostState = snackbarHostState,
        modifier = modifier
    )
}

/**
 * Stateless Home Screen Content displaying subcomponents and handling state-hoisted user interactions.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreenContent(
    state: HomeUiState,
    richCartItems: List<CartItem>,
    cartTotalPriceFormatted: String,
    analyticsEvents: List<AnalyticsEvent>,
    onIntent: (HomeIntent) -> Unit,
    onCartIncrement: (String) -> Unit,
    onCartDecrement: (String) -> Unit,
    onCartRemove: (String) -> Unit,
    onCartClear: () -> Unit,
    onCartCheckout: () -> CheckoutResult,
    onAnalyticsClear: () -> Unit,
    onAction: (SDUIAction) -> Unit,
    modifier: Modifier = Modifier,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            HomeTopAppBar(
                cartCount = state.cartCount,
                analyticsCount = state.analyticsEventsCount,
                onOpenAnalytics = { onIntent(HomeIntent.SetAnalyticsSheetVisible(true)) },
                onOpenCart = { onIntent(HomeIntent.SetCartSheetVisible(true)) },
                onRefresh = { onIntent(HomeIntent.Refresh(isPullToRefresh = true)) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // SDUI Campaign Selector Strip
            CampaignSwitcherBar(
                currentCampaign = state.currentCampaign,
                onSwitchCampaign = { onIntent(HomeIntent.SwitchCampaign(it)) }
            )

            // Real-time Search / Filter bar
            HomeSearchBar(
                query = state.searchQuery,
                onQueryChange = { onIntent(HomeIntent.SearchQueryChanged(it)) },
                onClearQuery = { onIntent(HomeIntent.ClearSearch) }
            )

            // Main Feed Content (PullToRefresh + LazyColumn + Shimmer + Error State)
            HomeFeedContent(
                feedState = state.feedState,
                isRefreshing = state.isRefreshing,
                searchQuery = state.searchQuery,
                onRefresh = { onIntent(HomeIntent.Refresh(isPullToRefresh = true)) },
                onAction = onAction,
                onClearSearch = { onIntent(HomeIntent.ClearSearch) },
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Modal Bottom Sheet: Rich Interactive Cart & Checkout
    if (state.isCartSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { onIntent(HomeIntent.SetCartSheetVisible(false)) },
            sheetState = rememberModalBottomSheetState()
        ) {
            CartBottomSheetContent(
                cartItems = richCartItems,
                totalCount = state.cartCount,
                totalPriceFormatted = cartTotalPriceFormatted,
                onIncrement = onCartIncrement,
                onDecrement = onCartDecrement,
                onRemove = onCartRemove,
                onClearCart = onCartClear,
                onCheckout = onCartCheckout,
                onClose = { onIntent(HomeIntent.SetCartSheetVisible(false)) }
            )
        }
    }

    // Modal Bottom Sheet: Analytics Tracker
    if (state.isAnalyticsSheetVisible) {
        AnalyticsInspectorSheet(
            events = analyticsEvents,
            onClear = onAnalyticsClear,
            onDismiss = { onIntent(HomeIntent.SetAnalyticsSheetVisible(false)) }
        )
    }

    // Modal Bottom Sheet: Deep Link Navigation Preview
    state.targetNavigationUrl?.let { url ->
        NavigationPreviewSheet(
            targetUrl = url,
            onDismiss = { onIntent(HomeIntent.SetNavigationTargetUrl(null)) }
        )
    }
}

fun parseHexColor(hexString: String?, defaultColor: Color): Color {
    if (hexString.isNullOrBlank()) return defaultColor
    return try {
        val clean = hexString.removePrefix("#")
        val colorInt = if (clean.length == 6) {
            (0xFF000000 or clean.toLong(16)).toInt()
        } else if (clean.length == 8) {
            clean.toLong(16).toInt()
        } else {
            return defaultColor
        }
        Color(colorInt)
    } catch (e: Exception) {
        defaultColor
    }
}

// ---------------------------------------------------------------------------
// COMPOSE PREVIEWS (Light & Dark)
// ---------------------------------------------------------------------------

@Preview(name = "HomeScreen - Light", showBackground = true)
@Composable
fun HomeScreenPreview_Light() {
    FlexiFeedTheme(darkTheme = false) {
        HomeScreenPreviewSample()
    }
}

@Preview(name = "HomeScreen - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview_Dark() {
    FlexiFeedTheme(darkTheme = true) {
        HomeScreenPreviewSample()
    }
}

@Composable
private fun HomeScreenPreviewSample() {
    val sampleSections = listOf(
        SDUINode(
            id = "sec_banner",
            type = SDUIConstants.ComponentType.CAROUSEL,
            items = listOf(
                SDUINode(
                    id = "b1",
                    type = SDUIConstants.ComponentType.CAROUSEL,
                    props = mapOf(SDUIConstants.PropKey.IMAGE_URL to "https://picsum.photos/id/1060/800/400")
                )
            )
        ),
        SDUINode(
            id = "sec_flash",
            type = SDUIConstants.ComponentType.HORIZONTAL_LIST,
            props = mapOf(
                SDUIConstants.PropKey.TITLE to "⚡ Flash Sale ดีลเด็ด",
                SDUIConstants.PropKey.COUNTDOWN_REMAINING_SEC to 7200L
            ),
            items = listOf(
                SDUINode(
                    id = "f1",
                    type = SDUIConstants.ComponentType.PRODUCT_CARD_COMPACT,
                    props = mapOf(
                        SDUIConstants.PropKey.NAME to "หูฟังบลูทูธไร้สาย Pro",
                        SDUIConstants.PropKey.PRICE to "฿890",
                        SDUIConstants.PropKey.ORIGINAL_PRICE to "฿1,590",
                        SDUIConstants.PropKey.THUMBNAIL_URL to "https://picsum.photos/200/200"
                    )
                ),
                SDUINode(
                    id = "f2",
                    type = SDUIConstants.ComponentType.PRODUCT_CARD_COMPACT,
                    props = mapOf(
                        SDUIConstants.PropKey.NAME to "สมาร์ตวอทช์ Ultra Fit",
                        SDUIConstants.PropKey.PRICE to "฿1,290",
                        SDUIConstants.PropKey.ORIGINAL_PRICE to "฿2,990",
                        SDUIConstants.PropKey.THUMBNAIL_URL to "https://picsum.photos/200/200"
                    )
                )
            )
        )
    )

    val sampleScreen = SDUIScreen(
        screen = "HOME_FEED",
        version = "1.0",
        sections = sampleSections
    )

    HomeScreenContent(
        state = HomeUiState(
            feedState = SDUIFeedUiState.Success(
                screen = sampleScreen,
                campaign = CampaignType.DEFAULT_FEED,
                isLiveServer = true
            ),
            cartCount = 2,
            analyticsEventsCount = 3
        ),
        richCartItems = emptyList(),
        cartTotalPriceFormatted = "฿2,180",
        analyticsEvents = emptyList(),
        onIntent = {},
        onCartIncrement = {},
        onCartDecrement = {},
        onCartRemove = {},
        onCartClear = {},
        onCartCheckout = { CheckoutResult(orderId = "ORD-TEST", totalAmount = 2180.0, totalAmountFormatted = "฿2,180", itemsCount = 2) },
        onAnalyticsClear = {},
        onAction = {}
    )
}
