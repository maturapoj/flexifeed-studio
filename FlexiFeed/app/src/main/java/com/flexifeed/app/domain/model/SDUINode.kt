package com.flexifeed.app.domain.model

import com.flexifeed.app.domain.action.SDUIAction

/**
 * Screen entity containing the list of root SDUI section nodes.
 */
data class SDUIScreen(
    val screen: String,
    val version: String,
    val sections: List<SDUINode>
)

/**
 * Clean domain model for any node in the Server-Driven UI tree.
 */
data class SDUINode(
    val id: String,
    val type: String,
    val props: Map<String, Any?> = emptyMap(),
    val items: List<SDUINode> = emptyList(),
    val action: SDUIAction? = null
) {
    fun getString(key: String, default: String = ""): String {
        return (props[key] as? String) ?: default
    }

    fun getInt(key: String, default: Int = 0): Int {
        val value = props[key]
        return when (value) {
            is Number -> value.toInt()
            is String -> value.toDoubleOrNull()?.toInt() ?: default
            else -> default
        }
    }

    fun getLong(key: String, default: Long = 0L): Long {
        val value = props[key]
        return when (value) {
            is Number -> value.toLong()
            is String -> value.toDoubleOrNull()?.toLong() ?: default
            else -> default
        }
    }

    fun getDouble(key: String, default: Double = 0.0): Double {
        val value = props[key]
        return when (value) {
            is Number -> value.toDouble()
            is String -> value.toDoubleOrNull() ?: default
            else -> default
        }
    }

    fun getBoolean(key: String, default: Boolean = false): Boolean {
        val value = props[key]
        return when (value) {
            is Boolean -> value
            is String -> value.toBoolean()
            else -> default
        }
    }

    /**
     * Resolves the image URL checking props ("imageUrl", "thumbnailUrl", "bannerUrl", "image")
     */
    val resolvedImageUrl: String
        get() = getString("imageUrl")
            .ifEmpty { getString("thumbnailUrl") }
            .ifEmpty { getString("bannerUrl") }
            .ifEmpty { getString("image") }
}
