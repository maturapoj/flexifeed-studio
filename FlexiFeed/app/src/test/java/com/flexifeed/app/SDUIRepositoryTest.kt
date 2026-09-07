package com.flexifeed.app

import com.flexifeed.app.data.remote.MockSDUIService
import com.flexifeed.app.data.repository.SDUIRepositoryImpl
import com.flexifeed.app.domain.action.SDUIAction
import com.flexifeed.app.domain.model.SDUIConstants
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class SDUIRepositoryTest {

    private lateinit var repository: SDUIRepositoryImpl

    @Before
    fun setup() {
        repository = SDUIRepositoryImpl()
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
        assertEquals(SDUIConstants.Screen.HOME_FEED, screen.screen)
        assertEquals(SDUIConstants.Screen.DEFAULT_VERSION, screen.version)
        assertEquals(3, screen.sections.size)

        // Section 1: CAROUSEL
        val carouselSection = screen.sections[0]
        assertEquals("sec_carousel_01", carouselSection.id)
        assertEquals(SDUIConstants.ComponentType.CAROUSEL, carouselSection.type)
        assertEquals(4000L, carouselSection.getLong(SDUIConstants.PropKey.AUTO_SCROLL_INTERVAL))
        assertEquals(1, carouselSection.items.size)

        val banner = carouselSection.items[0]
        assertEquals("banner_mega_sale", banner.id)
        assertEquals("https://picsum.photos/800/400", banner.resolvedImageUrl)
        assertNotNull(banner.action)
        assertEquals(SDUIConstants.ActionType.NAVIGATE, banner.action?.type)
        assertEquals("flexifeed://campaign/mega-sale", banner.action?.getTargetUrl())

        // Section 2: HORIZONTAL_LIST
        val flashSaleSection = screen.sections[1]
        assertEquals("sec_flash_sale_02", flashSaleSection.id)
        assertEquals(SDUIConstants.ComponentType.HORIZONTAL_LIST, flashSaleSection.type)
        assertEquals("⚡ Flash Sale", flashSaleSection.getString(SDUIConstants.PropKey.TITLE))
        assertEquals(7200L, flashSaleSection.getLong(SDUIConstants.PropKey.COUNTDOWN_REMAINING_SEC))
        assertEquals(1, flashSaleSection.items.size)

        val product101 = flashSaleSection.items[0]
        assertEquals("prod_101", product101.id)
        assertEquals(SDUIConstants.ComponentType.PRODUCT_CARD_COMPACT, product101.type)
        assertEquals("หูฟังบลูทูธไร้สาย", product101.getString(SDUIConstants.PropKey.NAME))
        assertEquals("฿890", product101.getString(SDUIConstants.PropKey.PRICE))
        assertEquals("฿1,590", product101.getString(SDUIConstants.PropKey.ORIGINAL_PRICE))
        assertEquals("flexifeed://product/101", product101.action?.getTargetUrl())

        // Section 3: GRID_2X2
        val gridSection = screen.sections[2]
        assertEquals("sec_grid_products_03", gridSection.id)
        assertEquals(SDUIConstants.ComponentType.GRID_2X2, gridSection.type)
        assertEquals("สินค้าแนะนำสำหรับคุณ", gridSection.getString(SDUIConstants.PropKey.TITLE))
        assertEquals(1, gridSection.items.size)

        val product201 = gridSection.items[0]
        assertEquals("prod_201", product201.id)
        assertEquals(SDUIConstants.ComponentType.PRODUCT_CARD_FULL, product201.type)
        assertEquals("คีย์บอร์ดไร้สาย Mechanical", product201.getString(SDUIConstants.PropKey.NAME))
        assertEquals("฿2,490", product201.getString(SDUIConstants.PropKey.PRICE))
        assertEquals(4.8, product201.getDouble(SDUIConstants.PropKey.RATING), 0.001)
        assertEquals(SDUIConstants.ActionType.ADD_TO_CART, product201.action?.type)
        assertEquals("201", product201.action?.getProductId())
        assertEquals(1, product201.action?.getQuantity())
    }

    @Test
    fun testDefaultMockFeedParsesSuccessfully() {
        val screen = repository.parseJsonToScreen(MockSDUIService.DEFAULT_HOME_FEED_JSON)
        assertEquals(SDUIConstants.Screen.HOME_FEED, screen.screen)
        assertTrue(screen.sections.isNotEmpty())
        assertEquals(3, screen.sections.size)
    }

    @Test
    fun testFetchHomeFeedRemoteDirectDTO() = kotlinx.coroutines.runBlocking {
        val fakeApi = object : com.flexifeed.app.data.remote.SDUIApi {
            override suspend fun getHomeFeed(campaign: String?): com.flexifeed.app.data.model.SDUIResponseDTO {
                return com.flexifeed.app.data.model.SDUIResponseDTO(
                    screen = "DIRECT_DTO_SCREEN",
                    version = "2.0",
                    sections = listOf(
                        com.flexifeed.app.data.model.SDUINodeDTO(
                            id = "sec_1",
                            type = "CAROUSEL"
                        )
                    )
                )
            }
            override suspend fun getScreen(screenId: String): com.flexifeed.app.data.model.SDUIResponseDTO {
                return getHomeFeed(null)
            }
        }
        val customRepo = SDUIRepositoryImpl(api = fakeApi)
        val result = customRepo.fetchHomeFeed()

        assertTrue(result.isSuccess)
        val screen = result.getOrNull()
        assertNotNull(screen)
        assertEquals("DIRECT_DTO_SCREEN", screen?.screen)
        assertEquals("2.0", screen?.version)
        assertTrue(customRepo.isLiveServerConnected)
    }

    @Test
    fun testFetchHomeFeedFallbackToMockOnRemoteError() = kotlinx.coroutines.runBlocking {
        val failingApi = object : com.flexifeed.app.data.remote.SDUIApi {
            override suspend fun getHomeFeed(campaign: String?): com.flexifeed.app.data.model.SDUIResponseDTO {
                throw java.io.IOException("Remote network down")
            }
            override suspend fun getScreen(screenId: String): com.flexifeed.app.data.model.SDUIResponseDTO {
                throw java.io.IOException("Remote network down")
            }
        }
        val fallbackRepo = SDUIRepositoryImpl(api = failingApi)
        val result = fallbackRepo.fetchHomeFeed()

        assertTrue(result.isSuccess)
        val screen = result.getOrNull()
        assertNotNull(screen)
        assertEquals(SDUIConstants.Screen.HOME_FEED, screen?.screen)
        org.junit.Assert.assertFalse(fallbackRepo.isLiveServerConnected)
    }
}

