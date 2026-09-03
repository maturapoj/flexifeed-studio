package com.flexifeed.app.domain.model

/**
 * Single source of truth for all Server-Driven UI (SDUI) constants across the project.
 * Completely eliminates hardcoded string literals across components, actions, repositories, and tests.
 */
object SDUIConstants {

    /**
     * Registered Component Types.
     */
    object ComponentType {
        const val CAROUSEL = "CAROUSEL"
        const val HORIZONTAL_LIST = "HORIZONTAL_LIST"
        const val GRID_2X2 = "GRID_2X2"
        const val PRODUCT_CARD_COMPACT = "PRODUCT_CARD_COMPACT"
        const val PRODUCT_CARD_FULL = "PRODUCT_CARD_FULL"
    }

    /**
     * Supported Action Types.
     */
    object ActionType {
        const val NAVIGATE = "NAVIGATE"
        const val ADD_TO_CART = "ADD_TO_CART"
        const val ANALYTICS = "ANALYTICS"
    }

    /**
     * Payload keys for SDUIAction.
     */
    object ActionKey {
        const val TARGET = "target"
        const val PRODUCT_ID = "productId"
        const val ID = "id"
        const val QUANTITY = "quantity"
        const val EVENT = "event"
        const val EVENT_NAME = "eventName"
    }

    /**
     * Property keys for SDUINode props.
     */
    object PropKey {
        const val TITLE = "title"
        const val AUTO_SCROLL_INTERVAL = "autoScrollInterval"
        const val COUNTDOWN_REMAINING_SEC = "countdownRemainingSec"
        const val NAME = "name"
        const val PRICE = "price"
        const val ORIGINAL_PRICE = "originalPrice"
        const val THUMBNAIL_URL = "thumbnailUrl"
        const val IMAGE_URL = "imageUrl"
        const val BANNER_URL = "bannerUrl"
        const val IMAGE = "image"
        const val RATING = "rating"
    }

    /**
     * Screen names and defaults.
     */
    object Screen {
        const val HOME_FEED = "HOME_FEED"
        const val UNKNOWN = "UNKNOWN"
        const val DEFAULT_VERSION = "1.0"
    }

    /**
     * Theme Modes.
     */
    object ThemeMode {
        const val LIGHT = "LIGHT"
        const val DARK = "DARK"
    }
}
