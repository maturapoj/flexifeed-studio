package com.flexifeed.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.ui.home.CartViewModel
import com.flexifeed.app.ui.home.HomeScreen
import com.flexifeed.app.ui.home.HomeViewModel
import com.flexifeed.app.ui.sdui.ActionDispatcher
import com.flexifeed.app.ui.sdui.SDUIGenericScreen

@Composable
fun FlexiFeedNavGraph(
    homeViewModel: HomeViewModel,
    cartViewModel: CartViewModel,
    analyticsTracker: AnalyticsTracker,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    startDestination: String = "sdui/home"
) {
    val actionDispatcher = remember(navController, cartViewModel, analyticsTracker) {
        ActionDispatcher(
            navController = navController,
            cartViewModel = cartViewModel,
            analyticsTracker = analyticsTracker
        )
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(
            route = "sdui/{screenId}",
            arguments = listOf(navArgument("screenId") { type = NavType.StringType })
        ) { backStackEntry ->
            val screenId = backStackEntry.arguments?.getString("screenId") ?: "home"
            
            // To ensure each screen has its own state if needed, you might scope ViewModels
            // But since HomeViewModel handles all screens for now, we just pass it.
            // Ideally use koinViewModel() here if they shouldn't share the exact same instance state.
            
            if (screenId == "home") {
                HomeScreen(
                    viewModel = homeViewModel,
                    cartViewModel = cartViewModel,
                    analyticsTracker = analyticsTracker,
                    actionDispatcher = actionDispatcher
                )
            } else {
                SDUIGenericScreen(
                    screenId = screenId,
                    viewModel = homeViewModel,
                    cartViewModel = cartViewModel,
                    analyticsTracker = analyticsTracker,
                    actionDispatcher = actionDispatcher,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}
