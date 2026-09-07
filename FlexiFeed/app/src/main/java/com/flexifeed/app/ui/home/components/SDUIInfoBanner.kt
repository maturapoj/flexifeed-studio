package com.flexifeed.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.flexifeed.app.BuildConfig

/**
 * SDUI server connectivity and screen metadata badge banner.
 */
@Composable
fun SDUIInfoBanner(
    screenName: String,
    version: String,
    isLiveServer: Boolean = false,
    isLiveConnected: Boolean = false,
    isHotReloading: Boolean = false,
    modifier: Modifier = Modifier
) {
    val containerBg = when {
        isHotReloading -> Color(0xFF8B5CF6).copy(alpha = 0.15f)
        isLiveConnected -> Color(0xFF10B981).copy(alpha = 0.12f)
        isLiveServer -> Color(0xFF10B981).copy(alpha = 0.08f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
    }

    val borderColor = when {
        isHotReloading -> Color(0xFF8B5CF6).copy(alpha = 0.5f)
        isLiveConnected -> Color(0xFF10B981).copy(alpha = 0.45f)
        isLiveServer -> Color(0xFF10B981).copy(alpha = 0.35f)
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
    }

    val textColor = when {
        isHotReloading -> Color(0xFF7C3AED)
        isLiveConnected -> Color(0xFF047857)
        isLiveServer -> Color(0xFF047857)
        else -> MaterialTheme.colorScheme.primary
    }

    val bannerText = when {
        isHotReloading -> "⚡ [${BuildConfig.ENVIRONMENT}] Live Hot-Reloading... • $screenName v$version"
        isLiveConnected -> "🟢 [${BuildConfig.ENVIRONMENT}] Live Stream Connected • $screenName v$version"
        isLiveServer -> "🟢 [${BuildConfig.ENVIRONMENT}] Live SDUI Server • $screenName v$version"
        else -> "🟠 [${BuildConfig.ENVIRONMENT}] Offline Fallback • $screenName v$version"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerBg)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Status",
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = bannerText,
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}
