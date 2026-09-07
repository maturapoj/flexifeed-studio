import { SDUIScreen } from '../types/sdui.types';

export const DEFAULT_PRESETS: Record<string, SDUIScreen> = {
  'mega-sale': {
    screen: 'HOME_FEED',
    version: '1.0',
    presetId: 'mega-sale',
    theme: {
      primaryColor: '#4F46E5',
      accentColor: '#FF3366',
      mode: 'LIGHT',
      logo: {
        bgColor: '#4F46E5',
        iconColor: '#FFFFFF',
        subtitleColor: '#4F46E5'
      }
    },
    sections: [
      {
        id: 'sec_carousel_01',
        type: 'CAROUSEL',
        props: { autoScrollInterval: 4000 },
        items: [
          {
            id: 'banner_mega_sale',
            imageUrl: 'https://picsum.photos/id/1060/800/400',
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://campaign/mega-sale' } }
          },
          {
            id: 'banner_gadget_expo',
            imageUrl: 'https://picsum.photos/id/201/800/400',
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://campaign/gadget-expo' } }
          },
          {
            id: 'banner_super_brand_day',
            imageUrl: 'https://picsum.photos/id/180/800/400',
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://campaign/super-brand-day' } }
          }
        ]
      },
      {
        id: 'sec_flash_sale_02',
        type: 'HORIZONTAL_LIST',
        props: { title: '⚡ Flash Sale', countdownRemainingSec: 7200 },
        items: [
          {
            id: 'prod_101',
            type: 'PRODUCT_CARD_COMPACT',
            props: { name: 'หูฟังบลูทูธไร้สาย', price: '฿890', originalPrice: '฿1,590', thumbnailUrl: 'https://picsum.photos/id/1/200/200' },
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://product/101' } }
          },
          {
            id: 'prod_102',
            type: 'PRODUCT_CARD_COMPACT',
            props: { name: 'สมาร์ตวอทช์ Ultra Fit', price: '฿1,290', originalPrice: '฿2,990', thumbnailUrl: 'https://picsum.photos/id/250/200/200' },
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://product/102' } }
          },
          {
            id: 'prod_103',
            type: 'PRODUCT_CARD_COMPACT',
            props: { name: 'พาวเวอร์แบงค์ 20000mAh', price: '฿499', originalPrice: '฿990', thumbnailUrl: 'https://picsum.photos/id/367/200/200' },
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://product/103' } }
          }
        ]
      },
      {
        id: 'sec_grid_products_03',
        type: 'GRID_2X2',
        props: { title: 'สินค้าแนะนำสำหรับคุณ' },
        items: [
          {
            id: 'prod_201',
            type: 'PRODUCT_CARD_FULL',
            props: { name: 'คีย์บอร์ดไร้สาย Mechanical', price: '฿2,490', rating: 4.8, thumbnailUrl: 'https://picsum.photos/id/96/300/300' },
            action: { type: 'ADD_TO_CART', payload: { productId: '201', quantity: 1 } }
          },
          {
            id: 'prod_202',
            type: 'PRODUCT_CARD_FULL',
            props: { name: 'เมาส์ Ergonomic ไร้สาย', price: '฿1,190', rating: 4.9, thumbnailUrl: 'https://picsum.photos/id/160/300/300' },
            action: { type: 'ADD_TO_CART', payload: { productId: '202', quantity: 1 } }
          },
          {
            id: 'prod_203',
            type: 'PRODUCT_CARD_FULL',
            props: { name: 'จอ Monitor 27 นิ้ว 165Hz', price: '฿5,990', rating: 4.7, thumbnailUrl: 'https://picsum.photos/id/0/300/300' },
            action: { type: 'ADD_TO_CART', payload: { productId: '203', quantity: 1 } }
          },
          {
            id: 'prod_204',
            type: 'PRODUCT_CARD_FULL',
            props: { name: 'แผ่นรองเมาส์ RGB Oversize', price: '฿390', rating: 4.6, thumbnailUrl: 'https://picsum.photos/id/119/300/300' },
            action: { type: 'ADD_TO_CART', payload: { productId: '204', quantity: 1 } }
          }
        ]
      }
    ]
  },
  'tech-weekend': {
    screen: 'HOME_FEED',
    version: '1.2',
    presetId: 'tech-weekend',
    theme: {
      primaryColor: '#0EA5E9',
      accentColor: '#F97316',
      mode: 'LIGHT',
      logo: {
        bgColor: '#0EA5E9',
        iconColor: '#FFFFFF',
        subtitleColor: '#0EA5E9'
      }
    },
    sections: [
      {
        id: 'sec_carousel_tech',
        type: 'CAROUSEL',
        props: { autoScrollInterval: 3000 },
        items: [
          {
            id: 'banner_tech_weekend',
            imageUrl: 'https://picsum.photos/id/3/800/400',
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://campaign/tech-weekend' } }
          },
          {
            id: 'banner_cyber_deals',
            imageUrl: 'https://picsum.photos/id/48/800/400',
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://campaign/cyber-deals' } }
          }
        ]
      },
      {
        id: 'sec_flash_sale_tech',
        type: 'HORIZONTAL_LIST',
        props: { title: '⚡ Midday Tech Flash', countdownRemainingSec: 3600 },
        items: [
          {
            id: 'prod_301',
            type: 'PRODUCT_CARD_COMPACT',
            props: { name: 'ไมโครโฟน คอนเดนเซอร์ USB', price: '฿1,350', originalPrice: '฿2,200', thumbnailUrl: 'https://picsum.photos/id/1082/200/200' },
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://product/301' } }
          },
          {
            id: 'prod_302',
            type: 'PRODUCT_CARD_COMPACT',
            props: { name: 'หูฟัง Gaming 7.1 Surround', price: '฿990', originalPrice: '฿1,890', thumbnailUrl: 'https://picsum.photos/id/2/200/200' },
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://product/302' } }
          }
        ]
      },
      {
        id: 'sec_grid_products_tech',
        type: 'GRID_2X2',
        props: { title: 'สินค้าไอที Best Sellers' },
        items: [
          {
            id: 'prod_401',
            type: 'PRODUCT_CARD_FULL',
            props: { name: 'เก้าอี้ Ergonomic เพื่อสุขภาพ', price: '฿4,290', rating: 5.0, thumbnailUrl: 'https://picsum.photos/id/42/300/300' },
            action: { type: 'ADD_TO_CART', payload: { productId: '401', quantity: 1 } }
          },
          {
            id: 'prod_402',
            type: 'PRODUCT_CARD_FULL',
            props: { name: 'โคมไฟหน้าจอ ScreenBar', price: '฿950', rating: 4.9, thumbnailUrl: 'https://picsum.photos/id/60/300/300' },
            action: { type: 'ADD_TO_CART', payload: { productId: '402', quantity: 1 } }
          }
        ]
      }
    ]
  }
};
