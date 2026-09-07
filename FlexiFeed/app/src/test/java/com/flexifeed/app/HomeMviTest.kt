package com.flexifeed.app

import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.CampaignType
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.ui.home.HomeViewModel
import com.flexifeed.app.ui.state.HomeEffect
import com.flexifeed.app.ui.state.HomeEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class HomeMviTest {

    private lateinit var viewModel: HomeViewModel

    @Before
    fun setUp() {
        viewModel = HomeViewModel(dispatcher = Dispatchers.Unconfined)
    }

    @Test
    fun `initial uiState has default values and loads default campaign feed`() {
        val state = viewModel.uiState.value
        assertEquals(CampaignType.DEFAULT_FEED, state.currentCampaign)
        assertEquals("", state.searchQuery)
        assertFalse(state.isCartSheetVisible)
        assertFalse(state.isAnalyticsSheetVisible)
        assertNull(state.targetNavigationUrl)
        assertEquals(0, state.cartCount)
        assertEquals(0, state.analyticsEventsCount)
        assertNotNull(state.feedState)
    }

    @Test
    fun `SearchQueryChanged event updates searchQuery in uiState`() {
        viewModel.onEvent(HomeEvent.SearchQueryChanged("หูฟัง"))
        assertEquals("หูฟัง", viewModel.uiState.value.searchQuery)

        viewModel.onEvent(HomeEvent.SearchQueryChanged("คีย์บอร์ดเกมมิ่ง"))
        assertEquals("คีย์บอร์ดเกมมิ่ง", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `ClearSearch event resets searchQuery to empty`() {
        viewModel.onEvent(HomeEvent.SearchQueryChanged("สมาร์ตวอทช์"))
        assertEquals("สมาร์ตวอทช์", viewModel.uiState.value.searchQuery)

        viewModel.onEvent(HomeEvent.ClearSearch)
        assertEquals("", viewModel.uiState.value.searchQuery)
    }

    @Test
    fun `SetCartSheetVisible event toggles cart sheet state`() {
        assertFalse(viewModel.uiState.value.isCartSheetVisible)

        viewModel.onEvent(HomeEvent.SetCartSheetVisible(true))
        assertTrue(viewModel.uiState.value.isCartSheetVisible)

        viewModel.onEvent(HomeEvent.SetCartSheetVisible(false))
        assertFalse(viewModel.uiState.value.isCartSheetVisible)
    }

    @Test
    fun `SetAnalyticsSheetVisible event toggles analytics sheet state`() {
        assertFalse(viewModel.uiState.value.isAnalyticsSheetVisible)

        viewModel.onEvent(HomeEvent.SetAnalyticsSheetVisible(true))
        assertTrue(viewModel.uiState.value.isAnalyticsSheetVisible)

        viewModel.onEvent(HomeEvent.SetAnalyticsSheetVisible(false))
        assertFalse(viewModel.uiState.value.isAnalyticsSheetVisible)
    }

    @Test
    fun `SetNavigationTargetUrl event sets and clears target url`() {
        assertNull(viewModel.uiState.value.targetNavigationUrl)

        val target = "flexifeed://deals/flash-sale"
        viewModel.onEvent(HomeEvent.SetNavigationTargetUrl(target))
        assertEquals(target, viewModel.uiState.value.targetNavigationUrl)

        viewModel.onEvent(HomeEvent.SetNavigationTargetUrl(null))
        assertNull(viewModel.uiState.value.targetNavigationUrl)
    }

    @Test
    fun `HandleSDUIAction event intercepts NAVIGATE action url`() {
        val action = SDUIAction(
            type = SDUIConstants.ActionType.NAVIGATE,
            payload = mapOf(SDUIConstants.ActionKey.TARGET to "flexifeed://products/123")
        )
        viewModel.onEvent(HomeEvent.HandleSDUIAction(action))
        assertEquals("flexifeed://products/123", viewModel.uiState.value.targetNavigationUrl)
    }

    @Test
    fun `UpdateCartCount and UpdateAnalyticsCount update respective counters`() {
        viewModel.onEvent(HomeEvent.UpdateCartCount(5))
        assertEquals(5, viewModel.uiState.value.cartCount)

        viewModel.onEvent(HomeEvent.UpdateAnalyticsCount(12))
        assertEquals(12, viewModel.uiState.value.analyticsEventsCount)
    }

    @Test
    fun `SwitchCampaign event updates campaign in uiState`() {
        viewModel.onEvent(HomeEvent.SwitchCampaign(CampaignType.TECH_WEEKEND))
        assertEquals(CampaignType.TECH_WEEKEND, viewModel.uiState.value.currentCampaign)
    }

    @Test
    fun `emitEffect emits side effect properly`() = runBlocking {
        val expectedMessage = "🎉 สินค้าพิเศษ Flash Sale!"
        viewModel.emitEffect(HomeEffect.ShowSnackbar(expectedMessage))
        val effect = viewModel.effect.first()
        assertTrue(effect is HomeEffect.ShowSnackbar)
        assertEquals(expectedMessage, (effect as HomeEffect.ShowSnackbar).message)
    }

    @Test
    fun `LiveConnectionChanged event updates isLiveConnected in uiState`() {
        assertFalse(viewModel.uiState.value.isLiveConnected)

        viewModel.onEvent(HomeEvent.LiveConnectionChanged(true))
        assertTrue(viewModel.uiState.value.isLiveConnected)

        viewModel.onEvent(HomeEvent.LiveConnectionChanged(false))
        assertFalse(viewModel.uiState.value.isLiveConnected)
    }

    @Test
    fun `LiveHotReloadReceived event triggers silent reload and updates campaign`() = runBlocking {
        val mockRepo = com.flexifeed.app.data.repository.SDUIRepository(
            remoteService = com.flexifeed.app.data.remote.MockSDUIService(),
            mockService = com.flexifeed.app.data.remote.MockSDUIService()
        )
        val testVm = HomeViewModel(
            repository = mockRepo,
            dispatcher = Dispatchers.Unconfined
        )
        testVm.onEvent(HomeEvent.LiveHotReloadReceived(presetId = "tech-weekend"))
        assertEquals(CampaignType.TECH_WEEKEND, testVm.uiState.value.currentCampaign)
        assertTrue(testVm.uiState.value.isHotReloading)

        val effect = testVm.effect.first()
        assertTrue(effect is HomeEffect.ShowSnackbar)
        assertEquals("⚡ Live UI Hot-Reloaded!", (effect as HomeEffect.ShowSnackbar).message)
        assertFalse(testVm.uiState.value.isHotReloading)
    }

    @Test
    fun `HomeViewModel automatically subscribes to SDUIStreamService events`() = runBlocking {
        val streamFlow = kotlinx.coroutines.flow.MutableSharedFlow<com.flexifeed.app.data.remote.SDUIStreamEvent>()
        val fakeService = object : com.flexifeed.app.data.remote.SDUIStreamService {
            override fun observeEvents() = streamFlow
        }

        val testViewModel = HomeViewModel(
            streamService = fakeService,
            dispatcher = Dispatchers.Unconfined
        )

        assertFalse(testViewModel.uiState.value.isLiveConnected)

        streamFlow.emit(com.flexifeed.app.data.remote.SDUIStreamEvent.Connected())
        assertTrue(testViewModel.uiState.value.isLiveConnected)

        streamFlow.emit(com.flexifeed.app.data.remote.SDUIStreamEvent.FeedUpdated(presetId = "tech-weekend"))
        assertEquals(CampaignType.TECH_WEEKEND, testViewModel.uiState.value.currentCampaign)

        streamFlow.emit(com.flexifeed.app.data.remote.SDUIStreamEvent.Disconnected())
        assertFalse(testViewModel.uiState.value.isLiveConnected)
    }
}
