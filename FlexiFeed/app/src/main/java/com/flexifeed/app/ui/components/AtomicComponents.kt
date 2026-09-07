package com.flexifeed.app.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.domain.model.SDUINode
import com.flexifeed.app.ui.theme.parseHexColorOrNull

@Composable
fun AtomicImageComponent(node: SDUINode) {
    val imageUrl = node.resolvedImageUrl.ifEmpty { return }
    val height = node.getInt(SDUIConstants.PropKey.HEIGHT, 200)

    AsyncImage(
        model = imageUrl,
        contentDescription = null,
        modifier = Modifier
            .fillMaxWidth()
            .height(height.dp),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun AtomicTextComponent(node: SDUINode) {
    val text = node.getString(SDUIConstants.PropKey.TEXT)
    val styleStr = node.getString(SDUIConstants.PropKey.STYLE, "bodyMedium")
    val weightStr = node.getString(SDUIConstants.PropKey.WEIGHT, "normal")
    val colorStr = node.getString(SDUIConstants.PropKey.COLOR, "default")
    val alignStr = node.getString(SDUIConstants.PropKey.ALIGN, "start")
    val decorationStr = node.getString(SDUIConstants.PropKey.DECORATION, "none")

    val style = when (styleStr) {
        "headlineLarge" -> MaterialTheme.typography.headlineLarge
        "headlineMedium" -> MaterialTheme.typography.headlineMedium
        "headlineSmall" -> MaterialTheme.typography.headlineSmall
        "titleMedium" -> MaterialTheme.typography.titleMedium
        "bodyLarge" -> MaterialTheme.typography.bodyLarge
        "labelMedium" -> MaterialTheme.typography.labelMedium
        else -> MaterialTheme.typography.bodyMedium
    }

    val fontWeight = when (weightStr) {
        "bold" -> FontWeight.Bold
        "semiBold" -> FontWeight.SemiBold
        "medium" -> FontWeight.Medium
        else -> FontWeight.Normal
    }

    val parsedHex = parseHexColorOrNull(colorStr)
    val color = parsedHex ?: when (colorStr) {
        "primary" -> MaterialTheme.colorScheme.primary
        "secondary" -> MaterialTheme.colorScheme.secondary
        "gray" -> Color.Gray
        else -> MaterialTheme.colorScheme.onSurface
    }

    val textAlign = when (alignStr) {
        "center" -> TextAlign.Center
        "end" -> TextAlign.End
        else -> TextAlign.Start
    }

    val textDecoration = if (decorationStr == "line-through") TextDecoration.LineThrough else null

    Text(
        text = text,
        style = style,
        fontWeight = fontWeight,
        color = color,
        textAlign = textAlign,
        textDecoration = textDecoration,
        modifier = if (textAlign == TextAlign.Center) Modifier.fillMaxWidth() else Modifier
    )
}

@Composable
fun AtomicSpacerComponent(node: SDUINode) {
    val height = node.getInt(SDUIConstants.PropKey.HEIGHT, 0)
    val width = node.getInt(SDUIConstants.PropKey.WIDTH, 0)

    if (height > 0) Spacer(modifier = Modifier.height(height.dp))
    if (width > 0) Spacer(modifier = Modifier.width(width.dp))
}

@Composable
fun LayoutColumnComponent(node: SDUINode, content: @Composable (SDUINode) -> Unit) {
    val padding = node.getInt(SDUIConstants.PropKey.PADDING, 0)
    val alignStr = node.getString(SDUIConstants.PropKey.HORIZONTAL_ALIGN, "start")

    val horizontalAlignment = when (alignStr) {
        "center" -> Alignment.CenterHorizontally
        "end" -> Alignment.End
        else -> Alignment.Start
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding.dp),
        horizontalAlignment = horizontalAlignment
    ) {
        node.items.forEach { childNode ->
            content(childNode)
        }
    }
}

@Composable
fun LayoutRowComponent(node: SDUINode, content: @Composable (SDUINode) -> Unit) {
    val padding = node.getInt(SDUIConstants.PropKey.PADDING, 0)
    val spacing = node.getInt(SDUIConstants.PropKey.SPACING, 0)
    val alignStr = node.getString(SDUIConstants.PropKey.ALIGN, "center")

    val verticalAlignment = when (alignStr) {
        "top" -> Alignment.Top
        "bottom" -> Alignment.Bottom
        else -> Alignment.CenterVertically
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(padding.dp),
        verticalAlignment = verticalAlignment,
        horizontalArrangement = Arrangement.spacedBy(spacing.dp)
    ) {
        node.items.forEach { childNode ->
            content(childNode)
        }
    }
}

@Composable
fun AtomicButtonComponent(node: SDUINode, onAction: (SDUIAction) -> Unit) {
    val text = node.getString(SDUIConstants.PropKey.TEXT, "Button")
    val icon = node.getString(SDUIConstants.PropKey.ICON)

    Button(
        onClick = { node.action?.let { onAction(it) } },
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (icon == "ShoppingCart") {
            Icon(Icons.Default.ShoppingCart, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(Modifier.size(8.dp))
        }
        Text(text, fontWeight = FontWeight.Bold)
    }
}
