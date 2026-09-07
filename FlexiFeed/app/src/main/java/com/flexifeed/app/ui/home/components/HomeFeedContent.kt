package com.flexifeed.app.ui.home.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.ui.components.SDUIFeedShimmer
import com.flexifeed.app.ui.sdui.SDUIRenderer
import com.flexifeed.app.ui.state.SDUIFeedUiState

/**
 * Main feed content area with PullToRefresh, search filtering, and SDUI rendering.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeFeedContent(
    feedState: SDUIFeedUiState,
    isRefreshing: Boolean,
    searchQuery: String,
    onRefresh: () -> Unit,
    onAction: (SDUIAction) -> Unit,
    onClearSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (feedState) {
        is SDUIFeedUiState.Loading -> {
            SDUIFeedShimmer(modifier = modifier.fillMaxSize())
        }

        is SDUIFeedUiState.Success -> {
            val filteredSections = remember(feedState.screen.sections, searchQuery) {
                if (searchQuery.isBlank()) {
                    feedState.screen.sections
                } else {
                    val query = searchQuery.trim()
                    feedState.screen.sections.mapNotNull { section ->
                        val matchedItems = section.items.filter { item ->
                            val name = item.getString(SDUIConstants.PropKey.NAME)
                            val title = item.getString(SDUIConstants.PropKey.TITLE)
                            name.contains(query, ignoreCase = true) || title.contains(query, ignoreCase = true)
                        }
                        val sectionTitle = section.getString(SDUIConstants.PropKey.TITLE)
                        val titleMatches = sectionTitle.contains(query, ignoreCase = true)

                        if (matchedItems.isNotEmpty()) {
                            section.copy(items = matchedItems)
                        } else if (titleMatches) {
                            section
                        } else {
                            null
                        }
                    }
                }
            }

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = modifier.fillMaxSize()
            ) {
                if (filteredSections.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "🔍 ไม่พบสินค้าที่ตรงกับ \"$searchQuery\"",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "ลองค้นหาด้วยคำอื่น เช่น หูฟัง, สมาร์ตวอทช์, คีย์บอร์ด",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            OutlinedButton(onClick = onClearSearch) {
                                Text("ล้างคำค้นหา")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        // Info badge explaining SDUI
                        item {
                            SDUIInfoBanner(
                                screenName = feedState.screen.screen,
                                version = feedState.screen.version,
                                isLiveServer = feedState.isLiveServer
                            )
                        }

                        // Render each SDUI Section dynamically
                        items(filteredSections, key = { it.id }) { sectionNode ->
                            SDUIRenderer(
                                node = sectionNode,
                                onAction = onAction
                            )
                        }
                    }
                }
            }
        }

        is SDUIFeedUiState.Error -> {
            Box(
                modifier = modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "เกิดข้อผิดพลาดในการโหลด Feed",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.error
                    )
                    Text(
                        text = feedState.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(onClick = onRefresh) {
                        Text("ลองใหม่")
                    }
                }
            }
        }
    }
}
