package com.flexifeed.app

import com.flexifeed.app.data.remote.CampaignType
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.ui.home.HomeViewModel
import com.flexifeed.app.ui.state.HomeIntent
import com.flexifeed.app.ui.state.HomeSideEffect
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
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
    fun `initial state has default values and loads default campaign feed`() {
        val state = viewModel.state.value
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
    fun `SearchQueryChanged intent updates searchQuery in state`() {
        viewModel.onIntent(HomeIntent.SearchQueryChanged("หูฟัง"))
        assertEquals("หูฟัง", viewModel.state.value.searchQuery)

        viewModel.onIntent(HomeIntent.SearchQueryChanged("คีย์บอร์ดเกมมิ่ง"))
        assertEquals("คีย์บอร์ดเกมมิ่ง", viewModel.state.value.searchQuery)
    }

    @Test
    fun `ClearSearch intent resets searchQuery to empty`() {
        viewModel.onIntent(HomeIntent.SearchQueryChanged("สมาร์ตวอทช์"))
        assertEquals("สมาร์ตวอทช์", viewModel.state.value.searchQuery)

        viewModel.onIntent(HomeIntent.ClearSearch)
        assertEquals("", viewModel.state.value.searchQuery)
    }

    @Test
    fun `SetCartSheetVisible intent toggles cart sheet state`() {
        assertFalse(viewModel.state.value.isCartSheetVisible)

        viewModel.onIntent(HomeIntent.SetCartSheetVisible(true))
        assertTrue(viewModel.state.value.isCartSheetVisible)

        viewModel.onIntent(HomeIntent.SetCartSheetVisible(false))
        assertFalse(viewModel.state.value.isCartSheetVisible)
    }

    @Test
    fun `SetAnalyticsSheetVisible intent toggles analytics sheet state`() {
        assertFalse(viewModel.state.value.isAnalyticsSheetVisible)

        viewModel.onIntent(HomeIntent.SetAnalyticsSheetVisible(true))
        assertTrue(viewModel.state.value.isAnalyticsSheetVisible)

        viewModel.onIntent(HomeIntent.SetAnalyticsSheetVisible(false))
        assertFalse(viewModel.state.value.isAnalyticsSheetVisible)
    }

    @Test
    fun `SetNavigationTargetUrl intent sets and clears target url`() {
        assertNull(viewModel.state.value.targetNavigationUrl)

        val target = "flexifeed://deals/flash-sale"
        viewModel.onIntent(HomeIntent.SetNavigationTargetUrl(target))
        assertEquals(target, viewModel.state.value.targetNavigationUrl)

        viewModel.onIntent(HomeIntent.SetNavigationTargetUrl(null))
        assertNull(viewModel.state.value.targetNavigationUrl)
    }

    @Test
    fun `HandleSDUIAction intent intercepts NAVIGATE action url`() {
        val action = SDUIAction(
            type = SDUIConstants.ActionType.NAVIGATE,
            payload = mapOf(SDUIConstants.ActionKey.TARGET to "flexifeed://products/123")
        )
        viewModel.onIntent(HomeIntent.HandleSDUIAction(action))
        assertEquals("flexifeed://products/123", viewModel.state.value.targetNavigationUrl)
    }

    @Test
    fun `UpdateCartCount and UpdateAnalyticsCount update respective counters`() {
        viewModel.onIntent(HomeIntent.UpdateCartCount(5))
        assertEquals(5, viewModel.state.value.cartCount)

        viewModel.onIntent(HomeIntent.UpdateAnalyticsCount(12))
        assertEquals(12, viewModel.state.value.analyticsEventsCount)
    }

    @Test
    fun `SwitchCampaign intent updates campaign in state`() {
        viewModel.onIntent(HomeIntent.SwitchCampaign(CampaignType.TECH_WEEKEND))
        assertEquals(CampaignType.TECH_WEEKEND, viewModel.state.value.currentCampaign)
    }

    @Test
    fun `emitEffect emits side effect properly`() = runBlocking {
        val expectedMessage = "🎉 สินค้าพิเศษ Flash Sale!"
        viewModel.emitEffect(HomeSideEffect.ShowSnackbar(expectedMessage))
        val effect = viewModel.effect.first()
        assertTrue(effect is HomeSideEffect.ShowSnackbar)
        assertEquals(expectedMessage, (effect as HomeSideEffect.ShowSnackbar).message)
    }
}
