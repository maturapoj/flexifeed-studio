package com.flexifeed.app.ui.theme

import androidx.compose.ui.graphics.Color

val IndigoPrimary = Color(0xFF4F46E5)
val IndigoPrimaryLight = Color(0xFF6366F1)
val IndigoDark = Color(0xFF3730A3)
val IndigoAccent = Color(0xFF818CF8)

val CoralSale = Color(0xFFFF3366)
val CoralSaleDark = Color(0xFFE11D48)
val AmberRating = Color(0xFFF59E0B)

val BackgroundLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceCardLight = Color(0xFFFFFFFF)
val TextPrimaryLight = Color(0xFF0F172A)
val TextSecondaryLight = Color(0xFF64748B)
val BorderLight = Color(0xFFE2E8F0)

val BackgroundDark = Color(0xFF0F172A)
val SurfaceDark = Color(0xFF1E293B)
val SurfaceCardDark = Color(0xFF334155)
val TextPrimaryDark = Color(0xFFF8FAFC)
val TextSecondaryDark = Color(0xFF94A3B8)
val BorderDark = Color(0xFF334155)

val ShimmerBaseLight = Color(0xFFE2E8F0)
val ShimmerHighlightLight = Color(0xFFF1F5F9)
val ShimmerBaseDark = Color(0xFF1E293B)
val ShimmerHighlightDark = Color(0xFF334155)

/**
 * Safely parse hex string into Compose Color with a fallback color.
 * Supports #RRGGBB and #AARRGGBB.
 */
fun parseHexColor(hexString: String?, defaultColor: Color): Color {
    if (hexString.isNullOrBlank()) return defaultColor
    return try {
        val cleanHex = hexString.trim().removePrefix("#")
        when (cleanHex.length) {
            6 -> {
                val r = cleanHex.substring(0, 2).toInt(16)
                val g = cleanHex.substring(2, 4).toInt(16)
                val b = cleanHex.substring(4, 6).toInt(16)
                Color(r, g, b, 255)
            }
            8 -> {
                val a = cleanHex.substring(0, 2).toInt(16)
                val r = cleanHex.substring(2, 4).toInt(16)
                val g = cleanHex.substring(4, 6).toInt(16)
                val b = cleanHex.substring(6, 8).toInt(16)
                Color(r, g, b, a)
            }
            else -> defaultColor
        }
    } catch (e: Exception) {
        defaultColor
    }
}

/**
 * Safely parse hex string into nullable Compose Color.
 * Supports #RRGGBB and #AARRGGBB.
 */
fun parseHexColorOrNull(hexString: String?): Color? {
    if (hexString.isNullOrBlank()) return null
    return try {
        val cleanHex = hexString.trim().removePrefix("#")
        when (cleanHex.length) {
            6 -> {
                val r = cleanHex.substring(0, 2).toInt(16)
                val g = cleanHex.substring(2, 4).toInt(16)
                val b = cleanHex.substring(4, 6).toInt(16)
                Color(r, g, b, 255)
            }
            8 -> {
                val a = cleanHex.substring(0, 2).toInt(16)
                val r = cleanHex.substring(2, 4).toInt(16)
                val g = cleanHex.substring(4, 6).toInt(16)
                val b = cleanHex.substring(6, 8).toInt(16)
                Color(r, g, b, a)
            }
            else -> null
        }
    } catch (e: Exception) {
        null
    }
}
