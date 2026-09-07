package com.flexifeed.app.data.remote

import com.flexifeed.app.data.model.SDUIResponseDTO
import com.flexifeed.app.domain.model.CampaignType
import com.google.gson.Gson
import kotlinx.coroutines.delay

/**
 * Offline Mock implementation of SDUIService providing bundled DSL presets.
 */
class MockSDUIService(
    private val gson: Gson = Gson()
) {

    suspend fun getHomeFeed(campaign: CampaignType): SDUIResponseDTO {
        delay(1000)
        val jsonString = when (campaign) {
            CampaignType.TECH_WEEKEND -> TECH_WEEKEND_FEED_JSON
            else -> DEFAULT_HOME_FEED_JSON
        }
        return gson.fromJson(jsonString, SDUIResponseDTO::class.java)
    }

    suspend fun getScreen(screenId: String): SDUIResponseDTO {
        delay(800)
        val jsonString = when (screenId) {
            "home" -> DEFAULT_HOME_FEED_JSON
            "product_101" -> PRODUCT_101_JSON
            "product_201" -> PRODUCT_201_JSON
            "campaign_mega_sale" -> CAMPAIGN_MEGA_SALE_JSON
            "campaign_gadget_expo" -> CAMPAIGN_GADGET_EXPO_JSON
            "campaign_super_brand_day" -> CAMPAIGN_SUPER_BRAND_DAY_JSON
            else -> DEFAULT_HOME_FEED_JSON
        }
        return gson.fromJson(jsonString, SDUIResponseDTO::class.java)
    }

    fun getRawJson(campaign: CampaignType): String {
        return when (campaign) {
            CampaignType.DEFAULT_FEED -> DEFAULT_HOME_FEED_JSON
            CampaignType.TECH_WEEKEND -> TECH_WEEKEND_FEED_JSON
        }
    }

    companion object {
        val DEFAULT_HOME_FEED_JSON = """
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
                  "imageUrl": "https://picsum.photos/id/1060/800/400",
                  "action": {
                    "type": "NAVIGATE",
                    "payload": { "target": "flexifeed://campaign/mega-sale" }
                  }
                },
                {
                  "id": "banner_gadget_expo",
                  "imageUrl": "https://picsum.photos/id/201/800/400",
                  "action": {
                    "type": "NAVIGATE",
                    "payload": { "target": "flexifeed://campaign/gadget-expo" }
                  }
                },
                {
                  "id": "banner_super_brand_day",
                  "imageUrl": "https://picsum.photos/id/180/800/400",
                  "action": {
                    "type": "NAVIGATE",
                    "payload": { "target": "flexifeed://campaign/super-brand-day" }
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
                    "thumbnailUrl": "https://picsum.photos/id/1/200/200"
                  },
                  "action": {
                    "type": "NAVIGATE",
                    "payload": { "target": "flexifeed://product/101" }
                  }
                },
                {
                  "id": "prod_102",
                  "type": "PRODUCT_CARD_COMPACT",
                  "props": {
                    "name": "สมาร์ตวอทช์ Ultra Fit",
                    "price": "฿1,290",
                    "originalPrice": "฿2,990",
                    "thumbnailUrl": "https://picsum.photos/id/250/200/200"
                  },
                  "action": {
                    "type": "NAVIGATE",
                    "payload": { "target": "flexifeed://product/102" }
                  }
                },
                {
                  "id": "prod_103",
                  "type": "PRODUCT_CARD_COMPACT",
                  "props": {
                    "name": "พาวเวอร์แบงค์ 20000mAh",
                    "price": "฿499",
                    "originalPrice": "฿990",
                    "thumbnailUrl": "https://picsum.photos/id/367/200/200"
                  },
                  "action": {
                    "type": "NAVIGATE",
                    "payload": { "target": "flexifeed://product/103" }
                  }
                },
                {
                  "id": "prod_104",
                  "type": "PRODUCT_CARD_COMPACT",
                  "props": {
                    "name": "ลำโพงบลูทูธเบสแน่น",
                    "price": "฿750",
                    "originalPrice": "฿1,490",
                    "thumbnailUrl": "https://picsum.photos/id/145/200/200"
                  },
                  "action": {
                    "type": "NAVIGATE",
                    "payload": { "target": "flexifeed://product/104" }
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
                    "thumbnailUrl": "https://picsum.photos/id/96/300/300"
                  },
                  "action": {
                    "type": "ADD_TO_CART",
                    "payload": { "productId": "201", "quantity": 1 }
                  }
                },
                {
                  "id": "prod_202",
                  "type": "PRODUCT_CARD_FULL",
                  "props": {
                    "name": "เมาส์ Ergonomic ไร้สาย",
                    "price": "฿1,190",
                    "rating": 4.9,
                    "thumbnailUrl": "https://picsum.photos/id/160/300/300"
                  },
                  "action": {
                    "type": "ADD_TO_CART",
                    "payload": { "productId": "202", "quantity": 1 }
                  }
                },
                {
                  "id": "prod_203",
                  "type": "PRODUCT_CARD_FULL",
                  "props": {
                    "name": "จอ Monitor 27 นิ้ว 165Hz",
                    "price": "฿5,990",
                    "rating": 4.7,
                    "thumbnailUrl": "https://picsum.photos/id/0/300/300"
                  },
                  "action": {
                    "type": "ADD_TO_CART",
                    "payload": { "productId": "203", "quantity": 1 }
                  }
                },
                {
                  "id": "prod_204",
                  "type": "PRODUCT_CARD_FULL",
                  "props": {
                    "name": "แผ่นรองเมาส์ RGB Oversize",
                    "price": "฿390",
                    "rating": 4.6,
                    "thumbnailUrl": "https://picsum.photos/id/119/300/300"
                  },
                  "action": {
                    "type": "ADD_TO_CART",
                    "payload": { "productId": "204", "quantity": 1 }
                  }
                }
              ]
            }
          ]
        }
        """.trimIndent()

        val PRODUCT_101_JSON = """
        {
          "screen": "PRODUCT_DETAIL",
          "version": "1.0",
          "theme": { "primaryColor": "#4F46E5", "accentColor": "#FF3366", "mode": "LIGHT" },
          "sections": [
            {
              "id": "img_prod_101",
              "type": "IMAGE",
              "props": { "imageUrl": "https://picsum.photos/seed/101/800/800", "height": 300 }
            },
            {
              "id": "col_details",
              "type": "COLUMN",
              "props": { "padding": 16 },
              "items": [
                { "id": "txt_title", "type": "TEXT", "props": { "text": "หูฟังบลูทูธไร้สาย", "style": "headlineMedium", "weight": "bold" } },
                {
                  "id": "row_price",
                  "type": "ROW",
                  "props": { "spacing": 8, "align": "bottom" },
                  "items": [
                    { "id": "txt_price", "type": "TEXT", "props": { "text": "฿890", "style": "headlineSmall", "color": "primary", "weight": "bold" } },
                    { "id": "txt_old_price", "type": "TEXT", "props": { "text": "฿1,590", "style": "bodyLarge", "color": "gray", "decoration": "line-through" } }
                  ]
                },
                { "id": "spc_1", "type": "SPACER", "props": { "height": 24 } },
                { "id": "txt_desc_title", "type": "TEXT", "props": { "text": "รายละเอียด", "style": "titleMedium", "weight": "semiBold" } },
                { "id": "spc_2", "type": "SPACER", "props": { "height": 8 } },
                { "id": "txt_desc", "type": "TEXT", "props": { "text": "สินค้านี้คือหูฟังบลูทูธคุณภาพดีมาก เหมาะสำหรับการใช้งานทุกรูปแบบ รับประกันคุณภาพ 1 ปีเต็ม", "style": "bodyMedium" } },
                { "id": "spc_3", "type": "SPACER", "props": { "height": 24 } },
                {
                  "id": "btn_add_cart",
                  "type": "BUTTON",
                  "props": { "text": "เพิ่มลงตะกร้า", "icon": "ShoppingCart" },
                  "action": { "type": "ADD_TO_CART", "payload": { "productId": "101", "quantity": 1, "name": "หูฟังบลูทูธไร้สาย", "price": "฿890", "imageUrl": "https://picsum.photos/seed/101/800/800" } }
                }
              ]
            }
          ]
        }
        """.trimIndent()

        val CAMPAIGN_MEGA_SALE_JSON = """
        {
          "screen": "CAMPAIGN",
          "version": "1.0",
          "theme": { "primaryColor": "#4F46E5", "accentColor": "#FF3366", "mode": "LIGHT" },
          "sections": [
            { "id": "img_banner", "type": "IMAGE", "props": { "imageUrl": "https://picsum.photos/seed/mega-sale/800/400", "height": 200 } },
            {
              "id": "col_content",
              "type": "COLUMN",
              "props": { "padding": 16, "horizontalAlign": "center" },
              "items": [
                { "id": "spc_1", "type": "SPACER", "props": { "height": 24 } },
                { "id": "txt_title", "type": "TEXT", "props": { "text": "Welcome to MEGA SALE", "style": "headlineMedium", "weight": "bold", "color": "primary" } },
                { "id": "spc_2", "type": "SPACER", "props": { "height": 16 } },
                { "id": "txt_desc", "type": "TEXT", "props": { "text": "Special deals and offers are waiting for you! Explore the latest products in this campaign.", "style": "bodyLarge", "align": "center" } }
              ]
            }
          ]
        }
        """.trimIndent()

        val PRODUCT_201_JSON = """
        {
          "screen": "PRODUCT_DETAIL",
          "version": "1.0",
          "theme": { "primaryColor": "#4F46E5", "accentColor": "#10B981", "mode": "LIGHT" },
          "sections": [
            {
              "id": "img_prod_201",
              "type": "IMAGE",
              "props": { "imageUrl": "https://picsum.photos/id/96/800/600", "height": 280 }
            },
            {
              "id": "col_details",
              "type": "COLUMN",
              "props": { "padding": 16 },
              "items": [
                { "id": "txt_title", "type": "TEXT", "props": { "text": "คีย์บอร์ดไร้สาย Mechanical RGB", "style": "headlineMedium", "weight": "bold" } },
                {
                  "id": "row_price",
                  "type": "ROW",
                  "props": { "spacing": 8, "align": "bottom" },
                  "items": [
                    { "id": "txt_price", "type": "TEXT", "props": { "text": "฿2,490", "style": "headlineSmall", "color": "primary", "weight": "bold" } },
                    { "id": "txt_old_price", "type": "TEXT", "props": { "text": "฿3,290", "style": "bodyLarge", "color": "gray", "decoration": "line-through" } },
                    { "id": "txt_badge", "type": "TEXT", "props": { "text": "ลด 24%", "style": "labelMedium", "color": "#EF4444", "weight": "bold" } }
                  ]
                },
                { "id": "spc_1", "type": "SPACER", "props": { "height": 16 } },
                { "id": "txt_desc_title", "type": "TEXT", "props": { "text": "จุดเด่นและสเปกสินค้า", "style": "titleMedium", "weight": "semiBold" } },
                { "id": "spc_2", "type": "SPACER", "props": { "height": 8 } },
                { "id": "txt_desc", "type": "TEXT", "props": { "text": "• เชื่อมต่อ 3 โหมด: Bluetooth 5.3, 2.4GHz Wireless และ USB Type-C\n• แบตเตอรี่อึด 4,000 mAh ใช้งานได้สูงสุด 200 ชั่วโมง\n• ไฟ RGB 16.8 ล้านสี ปรับแต่งได้ 18 โหมด\n• คีย์แคป PBT Double-shot ทนทาน ตัวอักษรไม่ลอก", "style": "bodyMedium" } },
                { "id": "spc_3", "type": "SPACER", "props": { "height": 24 } },
                {
                  "id": "btn_add_cart",
                  "type": "BUTTON",
                  "props": { "text": "เพิ่มลงตะกร้า • ฿2,490", "icon": "ShoppingCart" },
                  "action": { "type": "ADD_TO_CART", "payload": { "productId": "201", "quantity": 1, "name": "คีย์บอร์ดไร้สาย Mechanical", "price": "฿2,490", "imageUrl": "https://picsum.photos/id/96/300/300" } }
                }
              ]
            }
          ]
        }
        """.trimIndent()

        val CAMPAIGN_GADGET_EXPO_JSON = """
        {
          "screen": "CAMPAIGN",
          "version": "1.0",
          "theme": { "primaryColor": "#059669", "accentColor": "#F59E0B", "mode": "LIGHT" },
          "sections": [
            { "id": "img_banner", "type": "IMAGE", "props": { "imageUrl": "https://picsum.photos/id/201/800/400", "height": 200 } },
            {
              "id": "col_content",
              "type": "COLUMN",
              "props": { "padding": 16 },
              "items": [
                { "id": "txt_title", "type": "TEXT", "props": { "text": "⚡ GADGET EXPO 2026", "style": "headlineMedium", "weight": "bold", "color": "primary" } },
                { "id": "txt_sub", "type": "TEXT", "props": { "text": "มหกรรมสินค้าไอทีลดสูงสุด 70% ช้อปคุ้มตลอดสัปดาห์!", "style": "bodyLarge" } },
                { "id": "spc_1", "type": "SPACER", "props": { "height": 16 } },
                {
                  "id": "row_vouchers",
                  "type": "ROW",
                  "props": { "spacing": 8 },
                  "items": [
                    {
                      "id": "btn_v1",
                      "type": "BUTTON",
                      "props": { "text": "🎁 โค้ดลด ฿500" },
                      "action": { "type": "ANALYTICS", "payload": { "event": "claim_voucher_500" } }
                    },
                    {
                      "id": "btn_v2",
                      "type": "BUTTON",
                      "props": { "text": "🚚 ส่งฟรีทั้งงาน" },
                      "action": { "type": "ANALYTICS", "payload": { "event": "claim_free_shipping" } }
                    }
                  ]
                },
                { "id": "spc_2", "type": "SPACER", "props": { "height": 24 } },
                { "id": "txt_highlight", "type": "TEXT", "props": { "text": "ดีลเด็ดประจำวัน", "style": "titleMedium", "weight": "bold" } },
                { "id": "spc_3", "type": "SPACER", "props": { "height": 12 } },
                {
                  "id": "btn_view_keyboard",
                  "type": "BUTTON",
                  "props": { "text": "ดู Mechanical Keyboard (฿2,490)" },
                  "action": { "type": "NAVIGATE", "payload": { "target": "flexifeed://product/201" } }
                },
                { "id": "spc_4", "type": "SPACER", "props": { "height": 8 } },
                {
                  "id": "btn_view_headphone",
                  "type": "BUTTON",
                  "props": { "text": "ดู หูฟังบลูทูธไร้สาย (฿890)" },
                  "action": { "type": "NAVIGATE", "payload": { "target": "flexifeed://product/101" } }
                }
              ]
            }
          ]
        }
        """.trimIndent()

        val CAMPAIGN_SUPER_BRAND_DAY_JSON = """
        {
          "screen": "CAMPAIGN",
          "version": "1.0",
          "theme": { "primaryColor": "#7C3AED", "accentColor": "#EC4899", "mode": "LIGHT" },
          "sections": [
            { "id": "img_banner", "type": "IMAGE", "props": { "imageUrl": "https://picsum.photos/id/180/800/400", "height": 200 } },
            {
              "id": "col_content",
              "type": "COLUMN",
              "props": { "padding": 16 },
              "items": [
                { "id": "txt_title", "type": "TEXT", "props": { "text": "🏆 SUPER BRAND DAY", "style": "headlineMedium", "weight": "bold", "color": "primary" } },
                { "id": "txt_sub", "type": "TEXT", "props": { "text": "การันตีของแท้ 100% รับประกันศูนย์ไทย พร้อมเงินคืนสูงสุด 15% Coins", "style": "bodyLarge" } },
                { "id": "spc_1", "type": "SPACER", "props": { "height": 20 } },
                {
                  "id": "btn_explore_deals",
                  "type": "BUTTON",
                  "props": { "text": "🔥 ดีลเด่น: หูฟังบลูทูธ ฿890" },
                  "action": { "type": "NAVIGATE", "payload": { "target": "flexifeed://product/101" } }
                },
                { "id": "spc_2", "type": "SPACER", "props": { "height": 10 } },
                {
                  "id": "btn_explore_kb",
                  "type": "BUTTON",
                  "props": { "text": "🔥 ดีลเด่น: คีย์บอร์ดไร้สาย ฿2,490" },
                  "action": { "type": "NAVIGATE", "payload": { "target": "flexifeed://product/201" } }
                }
              ]
            }
          ]
        }
        """.trimIndent()

        val TECH_WEEKEND_FEED_JSON = """
        {
          "screen": "HOME_FEED",
          "version": "1.1",
          "sections": [
            {
              "id": "sec_carousel_tech",
              "type": "CAROUSEL",
              "props": {
                "autoScrollInterval": 3000
              },
              "items": [
                {
                  "id": "banner_tech_weekend",
                  "imageUrl": "https://picsum.photos/id/3/800/400",
                  "action": {
                    "type": "NAVIGATE",
                    "payload": { "target": "flexifeed://campaign/tech-weekend" }
                  }
                },
                {
                  "id": "banner_cyber_deals",
                  "imageUrl": "https://picsum.photos/id/48/800/400",
                  "action": {
                    "type": "NAVIGATE",
                    "payload": { "target": "flexifeed://campaign/cyber-deals" }
                  }
                }
              ]
            },
            {
              "id": "sec_flash_sale_tech",
              "type": "HORIZONTAL_LIST",
              "props": {
                "title": "⚡ Midday Flash Deals",
                "countdownRemainingSec": 3600
              },
              "items": [
                {
                  "id": "prod_301",
                  "type": "PRODUCT_CARD_COMPACT",
                  "props": {
                    "name": "ไมโครโฟน คอนเดนเซอร์ USB",
                    "price": "฿1,350",
                    "originalPrice": "฿2,200",
                    "thumbnailUrl": "https://picsum.photos/id/1082/200/200"
                  },
                  "action": {
                    "type": "NAVIGATE",
                    "payload": { "target": "flexifeed://product/301" }
                  }
                },
                {
                  "id": "prod_302",
                  "type": "PRODUCT_CARD_COMPACT",
                  "props": {
                    "name": "หูฟัง Gaming 7.1 Surround",
                    "price": "฿990",
                    "originalPrice": "฿1,890",
                    "thumbnailUrl": "https://picsum.photos/id/2/200/200"
                  },
                  "action": {
                    "type": "NAVIGATE",
                    "payload": { "target": "flexifeed://product/302" }
                  }
                }
              ]
            },
            {
              "id": "sec_grid_products_tech",
              "type": "GRID_2X2",
              "props": {
                "title": "สินค้าไอที Best Sellers"
              },
              "items": [
                {
                  "id": "prod_401",
                  "type": "PRODUCT_CARD_FULL",
                  "props": {
                    "name": "เก้าอี้ Ergonomic ทำงานเพื่อสุขภาพ",
                    "price": "฿4,290",
                    "rating": 5.0,
                    "thumbnailUrl": "https://picsum.photos/id/42/300/300"
                  },
                  "action": {
                    "type": "ADD_TO_CART",
                    "payload": { "productId": "401", "quantity": 1 }
                  }
                },
                {
                  "id": "prod_402",
                  "type": "PRODUCT_CARD_FULL",
                  "props": {
                    "name": "โคมไฟหน้าจอคอมพิวเตอร์ ScreenBar",
                    "price": "฿950",
                    "rating": 4.9,
                    "thumbnailUrl": "https://picsum.photos/id/60/300/300"
                  },
                  "action": {
                    "type": "ADD_TO_CART",
                    "payload": { "productId": "402", "quantity": 1 }
                  }
                }
              ]
            }
          ]
        }
        """.trimIndent()
    }
}
