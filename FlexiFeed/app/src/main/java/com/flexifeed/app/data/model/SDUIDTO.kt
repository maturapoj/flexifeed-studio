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
    @SerializedName("sections")
    val sections: List<SDUINodeDTO>? = null
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
