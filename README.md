# 🛒 FlexiFeed: Server-Driven UI E-Commerce Home Feed

[![Kotlin Version](https://img.shields.io/badge/Kotlin-2.0+-7F52FF?logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Android Min SDK](https://img.shields.io/badge/Min%20SDK-26-34A853?logo=android&logoColor=white)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpackcompose&logoColor=white)](https://developer.android.com/jetpack/compose)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**FlexiFeed** คือโปรเจกต์แอปพลิเคชัน Android ต้นแบบที่นำสถาปัตยกรรม **Server-Driven UI (SDUI)** มาประยุกต์ใช้กับหน้าหลัก (Home Feed) ของแอป E-Commerce โดยโครงสร้างลำดับ Layout, แบนเนอร์โปรโมชัน, สินค้า Flash Sale และตารางรายการสินค้า ถูกควบคุมและประกอบ (Compose) มาจาก JSON Payload ฝั่ง Server 100% ทำให้สามารถปรับเปลี่ยนหน้าตาแอปและจัดแคมเปญการตลาดได้ทันทีโดยไม่ต้องส่งอัปเดตผ่าน Google Play Store

---

## ✨ ฟีเจอร์หลัก (Key Features)

- 🎠 **Dynamic Promo Carousel:** ป้ายแบนเนอร์ภาพโปรโมชันแนวนอน เลื่อนสไลด์อัตโนมัติ พร้อมรองรับ Click Action
- ⚡ **Flash Sale Horizontal List:** รายการสินค้าลดราคาแบบ LazyRow แนวนอน แสดงป้ายลดราคา (Badge) และราคาพิเศษ
- 🛍️ **Product Catalog Grid:** รายการสินค้าทั่วไปจัดเรียงแบบ Grid 2 คอลัมน์ รองรับการขยาย/ยุบ section จากเซิร์ฟเวอร์
- 🧩 **Pluggable Component Registry:** สถาปัตยกรรมจับคู่ `type` จาก Server เข้ากับ Jetpack Compose Composables แบบ Decoupled
- 🎯 **Universal Action Dispatcher:** ตัวจัดการ Interaction แบบศูนย์กลาง รองรับทั้ง In-app Navigation, Deep Links, การเพิ่มสินค้าลงตะกร้า (Cart API), และ Analytics Tracking
- 💫 **Spec-Driven Shimmer Loading:** แสดง Shimmer Skeleton ชั่วคราวตามโครงร่างจริงของ Component ก่อนที่ข้อมูลจริงจะดาวน์โหลดเสร็จ

---

## 🏛️ สถาปัตยกรรมระบบ (Architecture)

```text
[ Backend / Mock Server ]
        │  (HTTP GET /api/v1/home-feed)
        ▼
[ HomeRepository / Retrofit ]
        │  (Parse JSON to SDUINode Tree)
        ▼
[ HomeViewModel ]
        │  (StateFlow<SDUIState>)
        ▼
[ SDUIRenderer ] (Jetpack Compose)
        ├── ComponentRegistry  ──> [ CarouselNode | FlashSaleNode | GridNode ]
        └── ActionDispatcher   ──> [ Navigation | Cart Mutation | Analytics ]
```

---

## 📋 โครงสร้าง JSON DSL (Data Contract)

ตัวอย่าง Payload ที่ส่งกลับมาจาก Endpoint `/api/v1/home-feed`:

```json
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
```

---

## 📂 โครงสร้างโฟลเดอร์ในโปรเจกต์ (Folder Structure)

```
FlexiFeed/app/src/main/java/com/flexifeed/app/
├── data/
│   ├── model/             # Data Models & Serializers สำหรับ SDUI JSON (SDUIDTO.kt)
│   ├── remote/            # Mock Engine & Endpoints (MockSDUIService.kt)
│   └── repository/        # Data Repository (SDUIRepository.kt)
├── domain/
│   ├── model/             # Clean Domain Models & Component Entities (SDUINode.kt)
│   └── action/            # SDUI Action Contracts (SDUIAction.kt, AnalyticsTracker.kt)
├── ui/
│   ├── sdui/
│   │   ├── SDUIRenderer.kt        # ตัวประมวลผล Tree แบบ Recursion
│   │   ├── ComponentRegistry.kt   # แหล่งลงทะเบียน Type -> Composable
│   │   └── ActionDispatcher.kt    # ตัวดักจับ Action จากทุก Component
│   ├── components/
│   │   ├── CarouselComponent.kt   # แบนเนอร์เลื่อนอัตโนมัติ
│   │   ├── HorizontalListComponent.kt # แถว Flash Sale แนวนอนพร้อม Live Countdown
│   │   ├── GridComponent.kt       # ตาราง 2 คอลัมน์
│   │   └── ShimmerPlaceholders.kt # Skeleton loaders ตามโครงร่าง Spec
│   ├── home/
│   │   ├── CartViewModel.kt       # ตัวจัดการสถานะตะกร้าสินค้า
│   │   ├── HomeScreen.kt          # หน้าจอหลักที่ผูกกับ ViewModel
│   │   └── HomeViewModel.kt
│   └── theme/                     # Color, Type, Theme
└── MainActivity.kt
```

---

## 🛠️ โค้ดส่วนสำคัญ (Key Implementation)

### 1. Component Registry Pattern
```kotlin
typealias SDUIRendererContent = @Composable (node: SDUINode, onAction: (SDUIAction) -> Unit) -> Unit

object ComponentRegistry {
    private val renderers = mutableMapOf<String, SDUIRendererContent>()

    init {
        register("CAROUSEL") { node, onAction -> CarouselComponent(node, onAction) }
        register("HORIZONTAL_LIST") { node, onAction -> HorizontalListComponent(node, onAction) }
        register("GRID_2X2") { node, onAction -> GridComponent(node, onAction) }
    }

    fun register(type: String, renderer: SDUIRendererContent) {
        renderers[type] = renderer
    }

    fun get(type: String): SDUIRendererContent? = renderers[type]
}
```

### 2. Action Dispatcher
```kotlin
class ActionDispatcher(
    private val navController: NavController,
    private val cartViewModel: CartViewModel,
    private val analyticsTracker: AnalyticsTracker
) {
    fun handleAction(action: SDUIAction) {
        when (action.type) {
            "NAVIGATE" -> {
                val url = action.payload["target"] as? String ?: return
                navController.navigate(Uri.parse(url))
            }
            "ADD_TO_CART" -> {
                val productId = action.payload["productId"] as? String ?: return
                cartViewModel.addToCart(productId)
            }
            "ANALYTICS" -> {
                val eventName = action.payload["event"] as? String ?: return
                analyticsTracker.logEvent(eventName, action.payload)
            }
        }
    }
}
```

---

## 🧪 การทดสอบ (Testing)

รัน Unit Tests:
```bash
./gradlew testDebugUnitTest
```