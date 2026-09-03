package com.flexifeed.app.ui.components

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddShoppingCart
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUINode
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.ui.theme.FlexiFeedTheme
import com.flexifeed.app.ui.theme.AmberRating

@Composable
fun GridComponent(
    node: SDUINode,
    onAction: (SDUIAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val items = node.items
    val title = node.getString(SDUIConstants.PropKey.TITLE, "สินค้าแนะนำสำหรับคุณ")

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section Title
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // 2x2 Grid via chunked rows
        val itemRows = items.chunked(2)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            itemRows.forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowItems.forEach { itemNode ->
                        ProductCardFull(
                            node = itemNode,
                            onAction = onAction,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill remaining empty slot if row is odd
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun ProductCardFull(
    node: SDUINode,
    onAction: (SDUIAction) -> Unit,
    modifier: Modifier = Modifier
) {
    val name = node.getString(SDUIConstants.PropKey.NAME)
    val price = node.getString(SDUIConstants.PropKey.PRICE)
    val rating = node.getDouble(SDUIConstants.PropKey.RATING, 0.0)
    val imageUrl = node.resolvedImageUrl
    var isFavorite by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = modifier.clickable {
            node.action?.let { onAction(it) }
        }
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            // Product Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                SubcomposeAsyncImage(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    loading = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    },
                    error = {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.BrokenImage,
                                contentDescription = "Error loading image",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                )

                // Favorite Heart Button
                IconButton(
                    onClick = { isFavorite = !isFavorite },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.35f))
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color(0xFFEF4444) else Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Rating badge over image if present
                if (rating > 0) {
                    Row(
                        modifier = Modifier
                            .padding(6.dp)
                            .align(Alignment.BottomStart)
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color.Black.copy(alpha = 0.65f))
                            .padding(horizontal = 6.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Rating",
                            tint = AmberRating,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = String.format("%.1f", rating),
                            color = Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Title
            Text(
                text = name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface,
                minLines = 2
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Price
            Text(
                text = price,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Add to Cart Button
            Button(
                onClick = {
                    val baseAction = node.action ?: SDUIAction(
                        type = SDUIConstants.ActionType.ADD_TO_CART,
                        payload = mapOf(SDUIConstants.ActionKey.PRODUCT_ID to node.id)
                    )
                    val enrichedPayload = baseAction.payload.toMutableMap().apply {
                        put(SDUIConstants.PropKey.NAME, name)
                        put(SDUIConstants.PropKey.PRICE, price)
                        put(SDUIConstants.PropKey.THUMBNAIL_URL, imageUrl)
                    }
                    onAction(baseAction.copy(payload = enrichedPayload))
                },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                ),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 6.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(36.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AddShoppingCart,
                    contentDescription = "Add to Cart",
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = " ใส่ตะกร้า",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// COMPOSE PREVIEWS
// ---------------------------------------------------------------------------

@Preview(name = "GridComponent - Light", showBackground = true)
@Composable
fun GridComponentPreview_Light() {
    FlexiFeedTheme(darkTheme = false) {
        Surface {
            val sampleItems = listOf(
                SDUINode(
                    id = "p1",
                    type = SDUIConstants.ComponentType.PRODUCT_CARD_FULL,
                    props = mapOf(
                        SDUIConstants.PropKey.NAME to "คีย์บอร์ดบลูทูธ Mechanical RGB",
                        SDUIConstants.PropKey.PRICE to "฿2,490",
                        SDUIConstants.PropKey.RATING to 4.9,
                        SDUIConstants.PropKey.THUMBNAIL_URL to "https://picsum.photos/200/200"
                    ),
                    action = SDUIAction(
                        type = SDUIConstants.ActionType.ADD_TO_CART,
                        payload = mapOf(SDUIConstants.ActionKey.PRODUCT_ID to "p1")
                    )
                ),
                SDUINode(
                    id = "p2",
                    type = SDUIConstants.ComponentType.PRODUCT_CARD_FULL,
                    props = mapOf(
                        SDUIConstants.PropKey.NAME to "เมาส์ไร้สาย Ergonomic Laser",
                        SDUIConstants.PropKey.PRICE to "฿990",
                        SDUIConstants.PropKey.RATING to 4.8,
                        SDUIConstants.PropKey.THUMBNAIL_URL to "https://picsum.photos/200/200"
                    ),
                    action = SDUIAction(
                        type = SDUIConstants.ActionType.ADD_TO_CART,
                        payload = mapOf(SDUIConstants.ActionKey.PRODUCT_ID to "p2")
                    )
                )
            )

            val gridNode = SDUINode(
                id = "grid_sample",
                type = SDUIConstants.ComponentType.GRID_2X2,
                props = mapOf(SDUIConstants.PropKey.TITLE to "สินค้าแนะนำสำหรับคุณ"),
                items = sampleItems
            )

            GridComponent(node = gridNode, onAction = {})
        }
    }
}

@Preview(name = "GridComponent - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun GridComponentPreview_Dark() {
    FlexiFeedTheme(darkTheme = true) {
        Surface {
            GridComponentPreview_Light()
        }
    }
}
