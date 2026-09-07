package com.flexifeed.app.ui.sdui

import androidx.compose.runtime.Composable
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUINode
import com.flexifeed.app.ui.components.CarouselComponent
import com.flexifeed.app.ui.components.GridComponent
import com.flexifeed.app.ui.components.HorizontalListComponent
import com.flexifeed.app.ui.components.ProductCardCompact
import com.flexifeed.app.ui.components.ProductCardFull
import com.flexifeed.app.domain.model.SDUIConstants.ComponentType.CAROUSEL
import com.flexifeed.app.domain.model.SDUIConstants.ComponentType.GRID_2X2
import com.flexifeed.app.domain.model.SDUIConstants.ComponentType.HORIZONTAL_LIST
import com.flexifeed.app.domain.model.SDUIConstants.ComponentType.PRODUCT_CARD_COMPACT
import com.flexifeed.app.domain.model.SDUIConstants.ComponentType.PRODUCT_CARD_FULL

/**
 * Functional interface contract for rendering Server-Driven UI component nodes.
 */
fun interface SDUIComponentRenderer {
    @Composable
    fun Render(node: SDUINode, onAction: (SDUIAction) -> Unit)
}

object ComponentRegistry {
    private val renderers = mutableMapOf<String, SDUIComponentRenderer>()

    init {
        register(CAROUSEL) { node, onAction -> CarouselComponent(node, onAction) }
        register(HORIZONTAL_LIST) { node, onAction -> HorizontalListComponent(node, onAction) }
        register(GRID_2X2) { node, onAction -> GridComponent(node, onAction) }
        register(PRODUCT_CARD_COMPACT) { node, onAction -> ProductCardCompact(node, onAction) }
        register(PRODUCT_CARD_FULL) { node, onAction -> ProductCardFull(node, onAction) }
    }

    fun register(type: String, renderer: SDUIComponentRenderer) {
        renderers[type] = renderer
    }

    fun get(type: String): SDUIComponentRenderer? = renderers[type]

    fun has(type: String): Boolean = renderers.containsKey(type)

    fun registeredTypes(): Set<String> = renderers.keys.toSet()
}
