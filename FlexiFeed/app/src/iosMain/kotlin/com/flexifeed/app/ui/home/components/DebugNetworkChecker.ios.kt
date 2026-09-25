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
import androidx.compose.material.icons.filled.BugReport
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
import com.flexifeed.app.di.defaultBaseUrl

@Composable
actual fun DebugNetworkChecker(modifier: Modifier) {
    val statusColor = Color(0xFF10B981)
    val statusText = "WIFI online"

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
        val envName = com.flexifeed.app.di.getIosEnvironmentName().uppercase()
        Text(
            text = "🔌 Device: $statusText • [$envName] $defaultBaseUrl",
            style = MaterialTheme.typography.bodySmall,
            color = statusColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp
        )
    }
}
