package com.flexifeed.app

import com.flexifeed.app.data.remote.MockSDUIService
import com.flexifeed.app.data.repository.SDUIRepository
import com.flexifeed.app.domain.action.SDUIAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SDUIRepositoryTest {

    private lateinit var repository: SDUIRepository

    @Before
    fun setup() {
        repository = SDUIRepository()
    }

    @Test
    fun testParseExactReadmePayload() {
        val json = """
        {
          "screen": "HOME_FEED",
          "version": "1.0",
          "sections": [
            {
              "id": "sec_carousel_01",
              "type": "CAROUSEL",
              "props": {
                "autoScrollInterval": 4000
              },
              "items": [
                {
                  "id": "banner_mega_sale",
                  "imageUrl": "https://picsum.photos/800/400",
                  "action": {
                    "type": "NAVIGATE",
                    "payload": { "target": "flexifeed://campaign/mega-sale" }
                  }
                }
              ]
            },
            {
              "id": "sec_flash_sale_02",
              "type": "HORIZONTAL_LIST",
              "props": {
                "title": "⚡ Flash Sale",
                "countdownRemainingSec": 7200
              },
              "items": [
                {
                  "id": "prod_101",
                  "type": "PRODUCT_CARD_COMPACT",
                  "props": {
                    "name": "หูฟังบลูทูธไร้สาย",
                    "price": "฿890",
                    "originalPrice": "฿1,590",
                    "thumbnailUrl": "https://picsum.photos/200/200"
                  },
                  "action": {
                    "type": "NAVIGATE",
                    "payload": { "target": "flexifeed://product/101" }
                  }
                }
              ]
            },
            {
              "id": "sec_grid_products_03",
              "type": "GRID_2X2",
              "props": {
                "title": "สินค้าแนะนำสำหรับคุณ"
              },
              "items": [
                {
                  "id": "prod_201",
                  "type": "PRODUCT_CARD_FULL",
                  "props": {
                    "name": "คีย์บอร์ดไร้สาย Mechanical",
                    "price": "฿2,490",
                    "rating": 4.8,
                    "thumbnailUrl": "https://picsum.photos/300/300"
                  },
                  "action": {
                    "type": "ADD_TO_CART",
                    "payload": { "productId": "201", "quantity": 1 }
                  }
                }
              ]
            }
          ]
        }
        """.trimIndent()

        val screen = repository.parseJsonToScreen(json)

        // Verify Screen
        assertEquals("HOME_FEED", screen.screen)
        assertEquals("1.0", screen.version)
        assertEquals(3, screen.sections.size)

        // Section 1: CAROUSEL
        val carouselSection = screen.sections[0]
        assertEquals("sec_carousel_01", carouselSection.id)
        assertEquals("CAROUSEL", carouselSection.type)
        assertEquals(4000L, carouselSection.getLong("autoScrollInterval"))
        assertEquals(1, carouselSection.items.size)

        val banner = carouselSection.items[0]
        assertEquals("banner_mega_sale", banner.id)
        assertEquals("https://picsum.photos/800/400", banner.resolvedImageUrl)
        assertNotNull(banner.action)
        assertEquals(SDUIAction.TYPE_NAVIGATE, banner.action?.type)
        assertEquals("flexifeed://campaign/mega-sale", banner.action?.getTargetUrl())

        // Section 2: HORIZONTAL_LIST
        val flashSaleSection = screen.sections[1]
        assertEquals("sec_flash_sale_02", flashSaleSection.id)
        assertEquals("HORIZONTAL_LIST", flashSaleSection.type)
        assertEquals("⚡ Flash Sale", flashSaleSection.getString("title"))
        assertEquals(7200L, flashSaleSection.getLong("countdownRemainingSec"))
        assertEquals(1, flashSaleSection.items.size)

        val product101 = flashSaleSection.items[0]
        assertEquals("prod_101", product101.id)
        assertEquals("PRODUCT_CARD_COMPACT", product101.type)
        assertEquals("หูฟังบลูทูธไร้สาย", product101.getString("name"))
        assertEquals("฿890", product101.getString("price"))
        assertEquals("฿1,590", product101.getString("originalPrice"))
        assertEquals("flexifeed://product/101", product101.action?.getTargetUrl())

        // Section 3: GRID_2X2
        val gridSection = screen.sections[2]
        assertEquals("sec_grid_products_03", gridSection.id)
        assertEquals("GRID_2X2", gridSection.type)
        assertEquals("สินค้าแนะนำสำหรับคุณ", gridSection.getString("title"))
        assertEquals(1, gridSection.items.size)

        val product201 = gridSection.items[0]
        assertEquals("prod_201", product201.id)
        assertEquals("PRODUCT_CARD_FULL", product201.type)
        assertEquals("คีย์บอร์ดไร้สาย Mechanical", product201.getString("name"))
        assertEquals("฿2,490", product201.getString("price"))
        assertEquals(4.8, product201.getDouble("rating"), 0.001)
        assertEquals(SDUIAction.TYPE_ADD_TO_CART, product201.action?.type)
        assertEquals("201", product201.action?.getProductId())
        assertEquals(1, product201.action?.getQuantity())
    }

    @Test
    fun testDefaultMockFeedParsesSuccessfully() {
        val screen = repository.parseJsonToScreen(MockSDUIService.DEFAULT_HOME_FEED_JSON)
        assertEquals("HOME_FEED", screen.screen)
        assertTrue(screen.sections.isNotEmpty())
        assertEquals(3, screen.sections.size)
    }
}
