package com.flexifeed.app

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.ui.home.CartViewModel
import com.flexifeed.app.ui.home.HomeViewModel
import com.flexifeed.app.ui.navigation.FlexiFeedNavGraph
import com.flexifeed.app.ui.theme.FlexiFeedTheme
import com.flexifeed.app.ui.theme.parseHexColorOrNull
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    homeViewModel: HomeViewModel = koinViewModel(),
    cartViewModel: CartViewModel = koinViewModel(),
    analyticsTracker: AnalyticsTracker = koinInject()
) {
    val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()
    val sduiTheme = uiState.sduiTheme

    val isDark = sduiTheme?.isDarkMode ?: isSystemInDarkTheme()
    val primaryColor = parseHexColorOrNull(sduiTheme?.primaryColorHex)
    val accentColor = parseHexColorOrNull(sduiTheme?.accentColorHex)

    FlexiFeedTheme(
        darkTheme = isDark,
        primaryColor = primaryColor,
        accentColor = accentColor
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            FlexiFeedNavGraph(
                homeViewModel = homeViewModel,
                cartViewModel = cartViewModel,
                analyticsTracker = analyticsTracker
            )
        }
    }
}
