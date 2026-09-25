package com.flexifeed.app.ui.home.components

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flexifeed.app.BuildConfig

/**
 * Debug-only banner reporting the device's raw network transport state and the
 * configured [BuildConfig.SDUI_BASE_URL], to help distinguish "device has no network"
 * from "device is online but can't reach the SDUI base URL" (e.g. 10.0.2.2 on a
 * physical device instead of the emulator).
 */
@Composable
actual fun DebugNetworkChecker(modifier: Modifier) {
    if (!BuildConfig.DEBUG) return

    val context = LocalContext.current
    var isOnline by remember { mutableStateOf(false) }
    var transport by remember { mutableStateOf("NONE") }

    DisposableEffect(context) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        fun refresh(capabilities: NetworkCapabilities?) {
            isOnline = capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
            transport = when {
                capabilities == null -> "NONE"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "WIFI"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "CELLULAR"
                capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "ETHERNET"
                else -> "OTHER"
            }
        }

        refresh(connectivityManager.activeNetwork?.let(connectivityManager::getNetworkCapabilities))

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                refresh(connectivityManager.getNetworkCapabilities(network))
            }

            override fun onLost(network: Network) {
                refresh(null)
            }

            override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                refresh(capabilities)
            }
        }
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivityManager.registerNetworkCallback(request, callback)

        onDispose { connectivityManager.unregisterNetworkCallback(callback) }
    }

    val statusColor = if (isOnline) Color(0xFF10B981) else Color(0xFFEF4444)
    val statusText = if (isOnline) "$transport online" else "no network connection"

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(statusColor.copy(alpha = 0.08f))
            .border(1.dp, statusColor.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.BugReport,
            contentDescription = "Debug network status",
            tint = statusColor,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = "🔌 Device: $statusText • Target: ${BuildConfig.SDUI_BASE_URL}",
            style = MaterialTheme.typography.bodySmall,
            color = statusColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp
        )
    }
}
