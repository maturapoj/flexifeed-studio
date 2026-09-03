package com.flexifeed.app

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.ui.home.CartViewModel
import com.flexifeed.app.ui.home.HomeScreen
import com.flexifeed.app.ui.home.HomeViewModel
import com.flexifeed.app.ui.sdui.ActionDispatcher
import com.flexifeed.app.ui.theme.FlexiFeedTheme

class MainActivity : ComponentActivity() {

    private val homeViewModel: HomeViewModel by viewModel()
    private val cartViewModel: CartViewModel by viewModel()
    private val analyticsTracker: AnalyticsTracker by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        handleIncomingIntent(intent)

        setContent {
            FlexiFeedTheme {
                val navController = rememberNavController()
                val actionDispatcher = remember {
                    ActionDispatcher(
                        navController = navController,
                        cartViewModel = cartViewModel,
                        analyticsTracker = analyticsTracker
                    )
                }

                Surface(modifier = Modifier.fillMaxSize()) {
                    HomeScreen(
                        viewModel = homeViewModel,
                        cartViewModel = cartViewModel,
                        analyticsTracker = analyticsTracker,
                        actionDispatcher = actionDispatcher
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val uri: Uri? = intent?.data
        if (uri != null && uri.scheme == "flexifeed") {
            Log.d("MainActivity", "Received deep link intent: $uri")
            analyticsTracker.logEvent(
                "deeplink_opened",
                mapOf("uri" to uri.toString(), "path" to (uri.path ?: ""))
            )
        }
    }
}
