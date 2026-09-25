package com.flexifeed.app.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull

/**
 * Root SDUI response returned from the server.
 */
@Serializable
data class SDUIResponseDTO(
    @SerialName("screen")
    val screen: String? = null,
    @SerialName("version")
    val version: String? = null,
    @SerialName("theme")
    val theme: SDUIThemeDTO? = null,
    @SerialName("sections")
    val sections: List<SDUINodeDTO>? = null
)

/**
 * Dynamic theme payload configured from server.
 */
@Serializable
data class SDUIThemeDTO(
    @SerialName("primaryColor")
    val primaryColor: String? = null,
    @SerialName("accentColor")
    val accentColor: String? = null,
    @SerialName("mode")
    val mode: String? = null,
    @SerialName("logo")
    val logo: SDUILogoThemeDTO? = null,
    @SerialName("logoBgColor")
    val logoBgColor: String? = null,
    @SerialName("logoIconColor")
    val logoIconColor: String? = null,
    @SerialName("logoSubtitleColor")
    val logoSubtitleColor: String? = null
)

/**
 * Dedicated theme customization for the brand logo.
 */
@Serializable
data class SDUILogoThemeDTO(
    @SerialName("bgColor")
    val bgColor: String? = null,
    @SerialName("iconColor")
    val iconColor: String? = null,
    @SerialName("titleColor")
    val titleColor: String? = null,
    @SerialName("subtitleColor")
    val subtitleColor: String? = null
)

/**
 * Recursive node representation in the SDUI tree hierarchy.
 */
@Serializable
data class SDUINodeDTO(
    @SerialName("id")
    val id: String? = null,
    @SerialName("type")
    val type: String? = null,
    @SerialName("imageUrl")
    val imageUrl: String? = null,
    @SerialName("props")
    val props: JsonObject? = null,
    @SerialName("items")
    val items: List<SDUINodeDTO>? = null,
    @SerialName("action")
    val action: SDUIActionDTO? = null
)

/**
 * Action triggered by user interaction on an SDUI component.
 */
@Serializable
data class SDUIActionDTO(
    @SerialName("type")
    val type: String,
    @SerialName("payload")
    val payload: JsonObject? = null
)

fun JsonObject.toMapAny(): Map<String, Any?> {
    return this.mapValues { (_, value) -> value.toAny() }
}

fun JsonElement.toAny(): Any? = when (this) {
    is JsonNull -> null
    is JsonPrimitive -> {
        if (isString) {
            content
        } else {
            booleanOrNull
                ?: intOrNull
                ?: longOrNull
                ?: doubleOrNull
                ?: content
        }
    }
    is JsonArray -> map { it.toAny() }
    is JsonObject -> toMapAny()
}
