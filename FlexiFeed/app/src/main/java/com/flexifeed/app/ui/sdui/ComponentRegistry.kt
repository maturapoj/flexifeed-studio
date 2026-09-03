package com.flexifeed.app.ui.sdui

import androidx.compose.runtime.Composable
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUINode
import com.flexifeed.app.ui.components.CarouselComponent
import com.flexifeed.app.ui.components.GridComponent
import com.flexifeed.app.ui.components.HorizontalListComponent
import com.flexifeed.app.ui.components.ProductCardCompact
import com.flexifeed.app.ui.components.ProductCardFull

import com.flexifeed.app.domain.model.SDUIConstants

typealias SDUIRendererContent = @Composable (node: SDUINode, onAction: (SDUIAction) -> Unit) -> Unit

object ComponentRegistry {
    private val renderers = mutableMapOf<String, SDUIRendererContent>()

    init {
        register(SDUIConstants.ComponentType.CAROUSEL) { node, onAction -> CarouselComponent(node, onAction) }
        register(SDUIConstants.ComponentType.HORIZONTAL_LIST) { node, onAction -> HorizontalListComponent(node, onAction) }
        register(SDUIConstants.ComponentType.GRID_2X2) { node, onAction -> GridComponent(node, onAction) }
        register(SDUIConstants.ComponentType.PRODUCT_CARD_COMPACT) { node, onAction -> ProductCardCompact(node, onAction) }
        register(SDUIConstants.ComponentType.PRODUCT_CARD_FULL) { node, onAction -> ProductCardFull(node, onAction) }
    }

    fun register(type: String, renderer: SDUIRendererContent) {
        renderers[type] = renderer
    }

    fun get(type: String): SDUIRendererContent? = renderers[type]

    fun has(type: String): Boolean = renderers.containsKey(type)

    fun registeredTypes(): Set<String> = renderers.keys.toSet()
}
