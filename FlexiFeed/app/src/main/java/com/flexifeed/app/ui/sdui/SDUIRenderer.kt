package com.flexifeed.app.ui.sdui

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUINode
import com.flexifeed.app.domain.model.SDUIScreen

private const val TAG = "SDUIRenderer"

/**
 * Recursive tree renderer for any node in the SDUI component hierarchy.
 */
@Composable
fun SDUIRenderer(
    node: SDUINode,
    onAction: (SDUIAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val renderer = ComponentRegistry.get(node.type)
    if (renderer != null) {
        renderer.Render(node, onAction)
    } else {
        Log.w(TAG, "No component renderer registered for type: ${node.type}")
        UnknownComponentFallback(node = node, modifier = modifier)
    }
}

/**
 * Renders all sections of an SDUIScreen in sequence.
 */
@Composable
fun SDUIScreenRenderer(
    screen: SDUIScreen,
    onAction: (SDUIAction) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        screen.sections.forEach { sectionNode ->
            SDUIRenderer(
                node = sectionNode,
                onAction = onAction
            )
        }
    }
}

@Composable
fun UnknownComponentFallback(
    node: SDUINode,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
            .padding(12.dp)
    ) {
        Text(
            text = "Unsupported SDUI Component: ${node.type} (${node.id})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
