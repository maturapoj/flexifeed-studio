package com.flexifeed.app

import com.flexifeed.app.domain.model.SDUIConstants
import com.flexifeed.app.ui.sdui.ComponentRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ComponentRegistryTest {

    @Test
    fun testRegisteredComponentsExist() {
        assertTrue(ComponentRegistry.has(SDUIConstants.ComponentType.CAROUSEL))
        assertTrue(ComponentRegistry.has(SDUIConstants.ComponentType.HORIZONTAL_LIST))
        assertTrue(ComponentRegistry.has(SDUIConstants.ComponentType.GRID_2X2))
        assertTrue(ComponentRegistry.has(SDUIConstants.ComponentType.PRODUCT_CARD_COMPACT))
        assertTrue(ComponentRegistry.has(SDUIConstants.ComponentType.PRODUCT_CARD_FULL))

        assertNotNull(ComponentRegistry.get(SDUIConstants.ComponentType.CAROUSEL))
        assertNotNull(ComponentRegistry.get(SDUIConstants.ComponentType.HORIZONTAL_LIST))
        assertNotNull(ComponentRegistry.get(SDUIConstants.ComponentType.GRID_2X2))
        assertNotNull(ComponentRegistry.get(SDUIConstants.ComponentType.PRODUCT_CARD_COMPACT))
        assertNotNull(ComponentRegistry.get(SDUIConstants.ComponentType.PRODUCT_CARD_FULL))
    }

    @Test
    fun testUnregisteredComponentReturnsNull() {
        assertNull(ComponentRegistry.get("NON_EXISTENT_TYPE"))
    }

    @Test
    fun testDynamicRegistration() {
        val testType = "CUSTOM_HERO_BANNER"
        ComponentRegistry.register(testType) { _, _ -> }
        assertTrue(ComponentRegistry.has(testType))
        assertNotNull(ComponentRegistry.get(testType))
    }
}
