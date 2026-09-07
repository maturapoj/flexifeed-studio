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
import com.flexifeed.app.ui.home.components.AnalyticsInspectorSheet
import com.flexifeed.app.ui.home.components.CampaignSwitcherBar
import com.flexifeed.app.ui.home.components.HomeFeedContent
import com.flexifeed.app.ui.home.components.HomeSearchBar
import com.flexifeed.app.ui.home.components.HomeTopAppBar
import com.flexifeed.app.ui.home.components.NavigationPreviewSheet
import com.flexifeed.app.ui.sdui.ActionDispatcher
import com.flexifeed.app.ui.state.CartItem
import com.flexifeed.app.ui.state.CheckoutResult
import com.flexifeed.app.ui.state.HomeEffect
import com.flexifeed.app.ui.state.HomeEvent
import com.flexifeed.app.ui.state.HomeUiState
import com.flexifeed.app.ui.state.SDUIFeedUiState
import com.flexifeed.app.ui.theme.FlexiFeedTheme

/**
 * Stateful Coordinator Composable for the Home Screen.
 * Collects [HomeViewModel.uiState] and dispatches user actions via [HomeEvent] model.
 */
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    cartViewModel: CartViewModel,
    analyticsTracker: AnalyticsTracker,
    actionDispatcher: ActionDispatcher,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
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

    // Listen for ViewModel Effects
    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    // Pass hoisted state to the pure stateless content container
    val hoistedState = uiState.copy(
        cartCount = cartCount,
        analyticsEventsCount = analyticsEvents.size
    )

    HomeScreenContent(
        uiState = hoistedState,
        richCartItems = richCartItems.values.toList(),
        cartTotalPriceFormatted = cartViewModel.calculateTotalFormatted(),
        analyticsEvents = analyticsEvents,
        onEvent = viewModel::onEvent,
        onCartIncrement = cartViewModel::incrementQuantity,
        onCartDecrement = cartViewModel::decrementQuantity,
        onCartRemove = cartViewModel::removeItem,
        onCartClear = cartViewModel::clearCart,
        onCartCheckout = cartViewModel::checkout,
        onAnalyticsClear = analyticsTracker::clearEvents,
        onAction = { action ->
            viewModel.onEvent(HomeEvent.HandleSDUIAction(action))
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
    uiState: HomeUiState,
    richCartItems: List<CartItem>,
    cartTotalPriceFormatted: String,
    analyticsEvents: List<AnalyticsEvent>,
    onEvent: (HomeEvent) -> Unit,
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
                cartCount = uiState.cartCount,
                analyticsCount = uiState.analyticsEventsCount,
                onOpenAnalytics = { onEvent(HomeEvent.SetAnalyticsSheetVisible(true)) },
                onOpenCart = { onEvent(HomeEvent.SetCartSheetVisible(true)) },
                onRefresh = { onEvent(HomeEvent.Refresh(isPullToRefresh = true)) }
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
                currentCampaign = uiState.currentCampaign,
                onSwitchCampaign = { onEvent(HomeEvent.SwitchCampaign(it)) }
            )

            // Real-time Search / Filter bar
            HomeSearchBar(
                query = uiState.searchQuery,
                onQueryChange = { onEvent(HomeEvent.SearchQueryChanged(it)) },
                onClearQuery = { onEvent(HomeEvent.ClearSearch) }
            )

            // Main Feed Content (PullToRefresh + LazyColumn + Shimmer + Error State)
            HomeFeedContent(
                feedState = uiState.feedState,
                isRefreshing = uiState.isRefreshing,
                searchQuery = uiState.searchQuery,
                onRefresh = { onEvent(HomeEvent.Refresh(isPullToRefresh = true)) },
                onAction = onAction,
                onClearSearch = { onEvent(HomeEvent.ClearSearch) },
                isLiveConnected = uiState.isLiveConnected,
                isHotReloading = uiState.isHotReloading,
                modifier = Modifier.weight(1f)
            )
        }
    }

    // Modal Bottom Sheet: Rich Interactive Cart & Checkout
    if (uiState.isCartSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { onEvent(HomeEvent.SetCartSheetVisible(false)) },
            sheetState = rememberModalBottomSheetState()
        ) {
            CartBottomSheetContent(
                cartItems = richCartItems,
                totalCount = uiState.cartCount,
                totalPriceFormatted = cartTotalPriceFormatted,
                onIncrement = onCartIncrement,
                onDecrement = onCartDecrement,
                onRemove = onCartRemove,
                onClearCart = onCartClear,
                onCheckout = onCartCheckout,
                onClose = { onEvent(HomeEvent.SetCartSheetVisible(false)) }
            )
        }
    }

    // Modal Bottom Sheet: Analytics Tracker
    if (uiState.isAnalyticsSheetVisible) {
        AnalyticsInspectorSheet(
            events = analyticsEvents,
            onClear = onAnalyticsClear,
            onDismiss = { onEvent(HomeEvent.SetAnalyticsSheetVisible(false)) }
        )
    }

    // Modal Bottom Sheet: Deep Link Navigation Preview
    uiState.targetNavigationUrl?.let { url ->
        NavigationPreviewSheet(
            targetUrl = url,
            onDismiss = { onEvent(HomeEvent.SetNavigationTargetUrl(null)) }
        )
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
        uiState = HomeUiState(
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
        onEvent = {},
        onCartIncrement = {},
        onCartDecrement = {},
        onCartRemove = {},
        onCartClear = {},
        onCartCheckout = { CheckoutResult(orderId = "ORD-TEST", totalAmount = 2180.0, totalAmountFormatted = "฿2,180", itemsCount = 2) },
        onAnalyticsClear = {},
        onAction = {}
    )
}
