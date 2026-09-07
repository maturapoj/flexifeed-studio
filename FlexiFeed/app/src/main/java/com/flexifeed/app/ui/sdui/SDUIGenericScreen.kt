package com.flexifeed.app.ui.sdui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.ui.home.CartViewModel
import com.flexifeed.app.ui.home.HomeViewModel
import com.flexifeed.app.ui.home.components.HomeFeedContent
import com.flexifeed.app.ui.state.HomeEffect
import com.flexifeed.app.ui.state.HomeEvent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SDUIGenericScreen(
    screenId: String,
    viewModel: HomeViewModel,
    cartViewModel: CartViewModel,
    analyticsTracker: AnalyticsTracker,
    actionDispatcher: ActionDispatcher,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartCount by cartViewModel.totalItemCount.collectAsStateWithLifecycle()
    val richCartItems by cartViewModel.items.collectAsStateWithLifecycle()
    val analyticsEvents by analyticsTracker.events.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(screenId) {
        if (uiState.currentScreenId != screenId) {
            viewModel.loadScreen(screenId)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is HomeEffect.ShowSnackbar -> snackbarHostState.showSnackbar(effect.message)
            }
        }
    }

    val title = if (screenId == "home") "FlexiFeed" else screenId.replace("_", " ").uppercase()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(title) },
                navigationIcon = {
                    if (screenId != "home") {
                        IconButton(onClick = onBackClick) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    navigationIconContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            HomeFeedContent(
                feedState = uiState.feedState,
                isRefreshing = uiState.isRefreshing,
                searchQuery = "",
                onRefresh = { viewModel.onEvent(HomeEvent.Refresh(isPullToRefresh = true)) },
                onAction = { action ->
                    actionDispatcher.handleAction(action)
                },
                onClearSearch = { },
                isLiveConnected = uiState.isLiveConnected,
                isHotReloading = uiState.isHotReloading,
                modifier = Modifier.weight(1f)
            )
        }
    }

    SDUICommonSheets(
        isCartVisible = uiState.isCartSheetVisible,
        cartItems = richCartItems.values.toList(),
        cartTotalCount = cartCount,
        cartTotalPriceFormatted = cartViewModel.calculateTotalFormatted(),
        onCartIncrement = cartViewModel::incrementQuantity,
        onCartDecrement = cartViewModel::decrementQuantity,
        onCartRemove = cartViewModel::removeItem,
        onCartClear = cartViewModel::clearCart,
        onCartCheckout = cartViewModel::checkout,
        onDismissCart = { viewModel.onEvent(HomeEvent.SetCartSheetVisible(false)) },
        isAnalyticsVisible = uiState.isAnalyticsSheetVisible,
        analyticsEvents = analyticsEvents,
        onAnalyticsClear = analyticsTracker::clearEvents,
        onDismissAnalytics = { viewModel.onEvent(HomeEvent.SetAnalyticsSheetVisible(false)) },
        targetNavigationUrl = uiState.targetNavigationUrl,
        onDismissNavigationPreview = { viewModel.onEvent(HomeEvent.SetNavigationTargetUrl(null)) }
    )
}
