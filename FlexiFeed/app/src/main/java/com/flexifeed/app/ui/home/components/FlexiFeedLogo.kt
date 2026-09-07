package com.flexifeed.app.ui.home.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
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

import com.flexifeed.app.domain.model.SDUILogoTheme
import com.flexifeed.app.ui.theme.parseHexColorOrNull

@Composable
fun FlexiFeedLogo(
    modifier: Modifier = Modifier,
    logoTheme: SDUILogoTheme? = null
) {
    val isDark = (0.299f * MaterialTheme.colorScheme.surface.red +
            0.587f * MaterialTheme.colorScheme.surface.green +
            0.114f * MaterialTheme.colorScheme.surface.blue) < 0.5f

    val customBoxBg = parseHexColorOrNull(logoTheme?.bgColorHex)
    val customIconTint = parseHexColorOrNull(logoTheme?.iconColorHex)
    val customTitleColor = parseHexColorOrNull(logoTheme?.titleColorHex)
    val customSubtitleColor = parseHexColorOrNull(logoTheme?.subtitleColorHex)

    val logoBoxBg = customBoxBg ?: if (isDark) {
        Color(0xFFE8EDFA)
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    
    val logoIconTint = customIconTint ?: if (isDark) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val titleColor = customTitleColor ?: MaterialTheme.colorScheme.onSurface
    val subtitleColor = customSubtitleColor ?: if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.primary

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(logoBoxBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ShoppingCart,
                contentDescription = "Logo",
                tint = logoIconTint,
                modifier = Modifier.size(22.dp)
            )
        }
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "FlexiFeed",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = titleColor,
                lineHeight = 18.sp
            )
            Text(
                text = "Server-Driven UI",
                style = MaterialTheme.typography.bodySmall,
                color = subtitleColor,
                fontSize = 10.5.sp,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 14.sp
            )
        }
    }
}
