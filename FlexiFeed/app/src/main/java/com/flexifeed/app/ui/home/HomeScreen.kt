package com.flexifeed.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.flexifeed.app.data.remote.CampaignType
import com.flexifeed.app.domain.action.AnalyticsEvent
import com.flexifeed.app.domain.action.AnalyticsTracker
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.domain.model.SDUINode
import com.flexifeed.app.domain.model.SDUIScreen
import com.flexifeed.app.ui.components.SDUIFeedShimmer
import com.flexifeed.app.ui.sdui.ActionDispatcher
import com.flexifeed.app.ui.sdui.SDUIRenderer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    cartViewModel: CartViewModel,
    analyticsTracker: AnalyticsTracker,
    actionDispatcher: ActionDispatcher,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val cartCount by cartViewModel.totalItemCount.collectAsStateWithLifecycle()
    val richCartItems by cartViewModel.items.collectAsStateWithLifecycle()
    val analyticsEvents by analyticsTracker.events.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    var showCartSheet by remember { mutableStateOf(false) }
    var showAnalyticsSheet by remember { mutableStateOf(false) }
    var navigatedTargetUrl by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }

    // Listen for cart add events to show snackbar
    LaunchedEffect(Unit) {
        cartViewModel.itemAddedEvent.collect { added ->
            snackbarHostState.showSnackbar("🛒 เพิ่มสินค้า #${added.productId} ลงในตะกร้าแล้ว!")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface,
                    actionIconContentColor = MaterialTheme.colorScheme.onSurface
                ),
                title = {
                    val isDark = (0.299f * MaterialTheme.colorScheme.surface.red +
                            0.587f * MaterialTheme.colorScheme.surface.green +
                            0.114f * MaterialTheme.colorScheme.surface.blue) < 0.5f

                    val logoBoxBg = if (isDark) {
                        Color(0xFFE8EDFA) // Soft light contrast surface matching web mock
                    } else {
                        MaterialTheme.colorScheme.primaryContainer
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(logoBoxBg),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "🛒",
                                fontSize = 19.sp
                            )
                        }
                        Column(
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "FlexiFeed",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 18.sp
                            )
                            Text(
                                text = "Server-Driven UI",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isDark) Color(0xFF94A3B8) else MaterialTheme.colorScheme.primary,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                lineHeight = 14.sp
                            )
                        }
                    }
                },
                actions = {
                    // Analytics Inspector
                    IconButton(onClick = { showAnalyticsSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (analyticsEvents.isNotEmpty()) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.primary,
                                        contentColor = Color.White
                                    ) {
                                        Text(analyticsEvents.size.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Analytics,
                                contentDescription = "Analytics Tracker",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Cart Button with Badge
                    IconButton(onClick = { showCartSheet = true }) {
                        BadgedBox(
                            badge = {
                                if (cartCount > 0) {
                                    Badge(
                                        containerColor = MaterialTheme.colorScheme.secondary,
                                        contentColor = Color.White
                                    ) {
                                        Text(cartCount.toString())
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.ShoppingCart,
                                contentDescription = "Cart",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    // Refresh Button
                    IconButton(onClick = { viewModel.refreshFeed() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // SDUI Campaign Selector Strip
            CampaignSwitcherBar(
                uiState = uiState,
                onSwitchCampaign = { viewModel.switchCampaign(it) }
            )

            // Real-time Search / Filter bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = "ค้นหาสินค้าใน Feed (เช่น หูฟัง, กล้อง, คีย์บอร์ด)...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                    focusedContainerColor = MaterialTheme.colorScheme.surface,
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )

            // Content Area based on State
            when (val state = uiState) {
                is SDUIFeedUiState.Loading -> {
                    SDUIFeedShimmer(modifier = Modifier.fillMaxSize())
                }

                is SDUIFeedUiState.Success -> {
                    val filteredSections = remember(state.screen.sections, searchQuery) {
                        if (searchQuery.isBlank()) {
                            state.screen.sections
                        } else {
                            val query = searchQuery.trim()
                            state.screen.sections.mapNotNull { section ->
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
                                OutlinedButton(onClick = { searchQuery = "" }) {
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
                                    screenName = state.screen.screen,
                                    version = state.screen.version,
                                    isLiveServer = state.isLiveServer
                                )
                            }

                            // Render each SDUI Section dynamically
                            items(filteredSections, key = { it.id }) { sectionNode ->
                                SDUIRenderer(
                                    node = sectionNode,
                                    onAction = { action ->
                                        // Intercept target navigation for in-app sheet preview
                                        if (action.type == SDUIConstants.ActionType.NAVIGATE) {
                                            action.getTargetUrl()?.let { targetUrl ->
                                                navigatedTargetUrl = targetUrl
                                            }
                                        }
                                        actionDispatcher.handleAction(action)
                                    }
                                )
                            }
                        }
                    }
                }

                is SDUIFeedUiState.Error -> {
                    Box(
                        modifier = Modifier
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
                                text = state.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Button(onClick = { viewModel.refreshFeed() }) {
                                Text("ลองใหม่")
                            }
                        }
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet: Rich Interactive Cart & Checkout
    if (showCartSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCartSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            CartBottomSheetContent(
                cartItems = richCartItems.values.toList(),
                totalCount = cartCount,
                totalPriceFormatted = cartViewModel.calculateTotalFormatted(),
                onIncrement = { cartViewModel.incrementQuantity(it) },
                onDecrement = { cartViewModel.decrementQuantity(it) },
                onRemove = { cartViewModel.removeItem(it) },
                onClearCart = { cartViewModel.clearCart() },
                onCheckout = { cartViewModel.checkout() },
                onClose = { showCartSheet = false }
            )
        }
    }

    // Modal Bottom Sheet: Analytics Tracker
    if (showAnalyticsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showAnalyticsSheet = false },
            sheetState = rememberModalBottomSheetState()
        ) {
            AnalyticsSheetContent(
                events = analyticsEvents,
                onClear = { analyticsTracker.clearEvents() },
                onClose = { showAnalyticsSheet = false }
            )
        }
    }

    // Modal Bottom Sheet: Deep Link Navigation Preview
    navigatedTargetUrl?.let { url ->
        ModalBottomSheet(
            onDismissRequest = { navigatedTargetUrl = null },
            sheetState = rememberModalBottomSheetState()
        ) {
            DeepLinkNavContent(
                url = url,
                onClose = { navigatedTargetUrl = null }
            )
        }
    }
}

@Composable
fun CampaignSwitcherBar(
    uiState: SDUIFeedUiState,
    onSwitchCampaign: (CampaignType) -> Unit,
    modifier: Modifier = Modifier
) {
    val activeCampaign =
        (uiState as? SDUIFeedUiState.Success)?.campaign ?: CampaignType.DEFAULT_FEED

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.SwapHoriz,
            contentDescription = "Switch Campaign",
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = "DSL Payload:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        FilterChip(
            selected = activeCampaign == CampaignType.DEFAULT_FEED,
            onClick = { onSwitchCampaign(CampaignType.DEFAULT_FEED) },
            label = { Text("Mega Sale (Default)", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )

        FilterChip(
            selected = activeCampaign == CampaignType.TECH_WEEKEND,
            onClick = { onSwitchCampaign(CampaignType.TECH_WEEKEND) },
            label = { Text("Tech Weekend", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
            )
        )
    }
}

@Composable
fun SDUIInfoBanner(screenName: String, version: String, isLiveServer: Boolean = false) {
    val containerBg =
        if (isLiveServer) Color(0xFF10B981).copy(alpha = 0.1f) else MaterialTheme.colorScheme.primary.copy(
            alpha = 0.08f
        )
    val borderColor =
        if (isLiveServer) Color(0xFF10B981).copy(alpha = 0.35f) else MaterialTheme.colorScheme.primary.copy(
            alpha = 0.2f
        )
    val textColor = if (isLiveServer) Color(0xFF047857) else MaterialTheme.colorScheme.primary

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(containerBg)
            .border(1.dp, borderColor, RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = Icons.Default.Info,
            contentDescription = "Info",
            tint = textColor,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = if (isLiveServer) {
                "🟢 [${com.flexifeed.app.BuildConfig.ENVIRONMENT}] Live SDUI Server • $screenName v$version"
            } else {
                "🟠 [${com.flexifeed.app.BuildConfig.ENVIRONMENT}] Offline Fallback • $screenName v$version"
            },
            style = MaterialTheme.typography.bodySmall,
            color = textColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp
        )
    }
}

// ---------------------------------------------------------------------------
// COMPOSE PREVIEWS
// ---------------------------------------------------------------------------

@Preview(name = "HomeScreen - Light", showBackground = true)
@Composable
fun HomeScreenPreview_Light() {
    com.flexifeed.app.ui.theme.FlexiFeedTheme(darkTheme = false) {
        Surface {
            HomeScreenPreviewSample()
        }
    }
}

@Preview(name = "HomeScreen - Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun HomeScreenPreview_Dark() {
    com.flexifeed.app.ui.theme.FlexiFeedTheme(darkTheme = true) {
        Surface {
            HomeScreenPreviewSample()
        }
    }
}

@Composable
private fun HomeScreenPreviewSample() {
    val sampleSections = listOf(
        SDUINode(
            id = "sec_banner",
            type = SDUIConstants.ComponentType.CAROUSEL,
            items = listOf(
                SDUINode(
                    id = "b1",
                    type = SDUIConstants.ComponentType.CAROUSEL,
                    props = mapOf(SDUIConstants.PropKey.IMAGE_URL to "https://picsum.photos/id/1060/800/400")
                )
            )
        ),
        SDUINode(
            id = "sec_flash",
            type = SDUIConstants.ComponentType.HORIZONTAL_LIST,
            props = mapOf(
                SDUIConstants.PropKey.TITLE to "⚡ Flash Sale ดีลเด็ด",
                SDUIConstants.PropKey.COUNTDOWN_REMAINING_SEC to 7200L
            ),
            items = listOf(
                SDUINode(
                    id = "f1",
                    type = SDUIConstants.ComponentType.PRODUCT_CARD_COMPACT,
                    props = mapOf(
                        SDUIConstants.PropKey.NAME to "หูฟังบลูทูธไร้สาย Pro",
                        SDUIConstants.PropKey.PRICE to "฿890",
                        SDUIConstants.PropKey.ORIGINAL_PRICE to "฿1,590",
                        SDUIConstants.PropKey.THUMBNAIL_URL to "https://picsum.photos/200/200"
                    )
                ),
                SDUINode(
                    id = "f2",
                    type = SDUIConstants.ComponentType.PRODUCT_CARD_COMPACT,
                    props = mapOf(
                        SDUIConstants.PropKey.NAME to "สมาร์ตวอทช์ Ultra Fit",
                        SDUIConstants.PropKey.PRICE to "฿1,290",
                        SDUIConstants.PropKey.ORIGINAL_PRICE to "฿2,990",
                        SDUIConstants.PropKey.THUMBNAIL_URL to "https://picsum.photos/200/200"
                    )
                )
            )
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        SDUIInfoBanner(screenName = "HOME_FEED", version = "1.0", isLiveServer = true)
        sampleSections.forEach { section ->
            SDUIRenderer(node = section, onAction = {})
        }
    }
}

@Composable
fun AnalyticsSheetContent(
    events: List<AnalyticsEvent>,
    onClear: () -> Unit,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "📊 Live Analytics Events (${events.size})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "Close")
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

        if (events.isEmpty()) {
            Text(
                text = "ยังไม่มี Event ใดๆ\nลองแตะแบนเนอร์ สินค้า หรือปุ่มใส่ตะกร้าเพื่อสร้าง Event จาก SDUI Actions",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 20.dp)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(events) { ev ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(10.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "🏷️ ${ev.eventName}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = ev.timestamp,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Text(
                            text = ev.parameters.toString(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onClear,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("ล้างประวัติ Event")
            }
        }
        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
fun DeepLinkNavContent(
    url: String,
    onClose: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "🚀 SDUI Navigation Triggered",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(14.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = url,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Action 'NAVIGATE' ถูกส่งไปยัง ActionDispatcher สำเร็จ โดยเป้าหมายคือ Deep Link URL ด้านบน",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onClose,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("เข้าใจแล้ว")
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

fun parseHexColor(hexString: String?, defaultColor: Color): Color {
    if (hexString.isNullOrBlank()) return defaultColor
    return try {
        val clean = hexString.removePrefix("#")
        val colorInt = if (clean.length == 6) {
            (0xFF000000 or clean.toLong(16)).toInt()
        } else if (clean.length == 8) {
            clean.toLong(16).toInt()
        } else {
            return defaultColor
        }
        Color(colorInt)
    } catch (e: Exception) {
        defaultColor
    }
}
