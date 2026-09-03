package com.flexifeed.app

import com.flexifeed.app.ui.sdui.ComponentRegistry
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ComponentRegistryTest {

    @Test
    fun testRegisteredComponentsExist() {
        assertTrue(ComponentRegistry.has("CAROUSEL"))
        assertTrue(ComponentRegistry.has("HORIZONTAL_LIST"))
        assertTrue(ComponentRegistry.has("GRID_2X2"))
        assertTrue(ComponentRegistry.has("PRODUCT_CARD_COMPACT"))
        assertTrue(ComponentRegistry.has("PRODUCT_CARD_FULL"))

        assertNotNull(ComponentRegistry.get("CAROUSEL"))
        assertNotNull(ComponentRegistry.get("HORIZONTAL_LIST"))
        assertNotNull(ComponentRegistry.get("GRID_2X2"))
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
