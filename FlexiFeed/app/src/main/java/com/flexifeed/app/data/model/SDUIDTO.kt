package com.flexifeed.app.data.model

import com.google.gson.annotations.SerializedName

/**
 * Root SDUI response returned from the server.
 */
data class SDUIResponseDTO(
    @SerializedName("screen")
    val screen: String? = null,
    @SerializedName("version")
    val version: String? = null,
    @SerializedName("theme")
    val theme: SDUIThemeDTO? = null,
    @SerializedName("sections")
    val sections: List<SDUINodeDTO>? = null
)

/**
 * Dynamic theme payload configured from server.
 */
data class SDUIThemeDTO(
    @SerializedName("primaryColor")
    val primaryColor: String? = null,
    @SerializedName("accentColor")
    val accentColor: String? = null,
    @SerializedName("mode")
    val mode: String? = null,
    @SerializedName("logo")
    val logo: SDUILogoThemeDTO? = null,
    @SerializedName("logoBgColor")
    val logoBgColor: String? = null,
    @SerializedName("logoIconColor")
    val logoIconColor: String? = null,
    @SerializedName("logoSubtitleColor")
    val logoSubtitleColor: String? = null
)

/**
 * Dedicated theme customization for the brand logo.
 */
data class SDUILogoThemeDTO(
    @SerializedName("bgColor")
    val bgColor: String? = null,
    @SerializedName("iconColor")
    val iconColor: String? = null,
    @SerializedName("titleColor")
    val titleColor: String? = null,
    @SerializedName("subtitleColor")
    val subtitleColor: String? = null
)

/**
 * Recursive node representation in the SDUI tree hierarchy.
 */
data class SDUINodeDTO(
    @SerializedName("id")
    val id: String? = null,
    @SerializedName("type")
    val type: String? = null,
    @SerializedName("imageUrl")
    val imageUrl: String? = null,
    @SerializedName("props")
    val props: Map<String, Any?>? = null,
    @SerializedName("items")
    val items: List<SDUINodeDTO>? = null,
    @SerializedName("action")
    val action: SDUIActionDTO? = null
)

/**
 * Action triggered by user interaction on an SDUI component.
 */
data class SDUIActionDTO(
    @SerializedName("type")
    val type: String,
    @SerializedName("payload")
    val payload: Map<String, Any?>? = null
)
