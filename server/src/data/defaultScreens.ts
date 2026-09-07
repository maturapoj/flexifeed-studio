import { SDUIScreen } from '../types/sdui.types';

/**
 * Preset / Fallback screens for Generic Navigation Flow
 */
export const DEFAULT_SCREENS: Record<string, SDUIScreen> = {
  'product_101': {
    screen: 'PRODUCT_DETAIL',
    version: '1.0',
    theme: { primaryColor: '#4F46E5', accentColor: '#FF3366', mode: 'LIGHT' },
    sections: [
      {
        id: 'img_prod_101',
        type: 'IMAGE',
        props: { imageUrl: 'https://picsum.photos/seed/101/800/800', height: 300 }
      },
      {
        id: 'col_details',
        type: 'COLUMN',
        props: { padding: 16 },
        items: [
          { id: 'txt_title', type: 'TEXT', props: { text: 'หูฟังบลูทูธไร้สาย', style: 'headlineMedium', weight: 'bold' } },
          {
            id: 'row_price',
            type: 'ROW',
            props: { spacing: 8, align: 'bottom' },
            items: [
              { id: 'txt_price', type: 'TEXT', props: { text: '฿890', style: 'headlineSmall', color: 'primary', weight: 'bold' } },
              { id: 'txt_old_price', type: 'TEXT', props: { text: '฿1,590', style: 'bodyLarge', color: 'gray', decoration: 'line-through' } }
            ]
          },
          { id: 'spc_1', type: 'SPACER', props: { height: 24 } },
          { id: 'txt_desc_title', type: 'TEXT', props: { text: 'รายละเอียด', style: 'titleMedium', weight: 'semiBold' } },
          { id: 'spc_2', type: 'SPACER', props: { height: 8 } },
          { id: 'txt_desc', type: 'TEXT', props: { text: 'สินค้านี้คือหูฟังบลูทูธคุณภาพดีมาก เหมาะสำหรับการใช้งานทุกรูปแบบ รับประกันคุณภาพ 1 ปีเต็ม', style: 'bodyMedium' } },
          { id: 'spc_3', type: 'SPACER', props: { height: 24 } },
          {
            id: 'btn_add_cart',
            type: 'BUTTON',
            props: { text: 'เพิ่มลงตะกร้า', icon: 'ShoppingCart' },
            action: { type: 'ADD_TO_CART', payload: { productId: '101', quantity: 1, name: 'หูฟังบลูทูธไร้สาย', price: '฿890', imageUrl: 'https://picsum.photos/seed/101/800/800' } }
          }
        ]
      }
    ]
  },
  'campaign_mega_sale': {
    screen: 'CAMPAIGN',
    version: '1.0',
    theme: { primaryColor: '#4F46E5', accentColor: '#FF3366', mode: 'LIGHT' },
    sections: [
      { id: 'img_banner', type: 'IMAGE', props: { imageUrl: 'https://picsum.photos/seed/mega-sale/800/400', height: 200 } },
      {
        id: 'col_content',
        type: 'COLUMN',
        props: { padding: 16, horizontalAlign: 'center' },
        items: [
          { id: 'spc_1', type: 'SPACER', props: { height: 24 } },
          { id: 'txt_title', type: 'TEXT', props: { text: 'Welcome to MEGA SALE', style: 'headlineMedium', weight: 'bold', color: 'primary' } },
          { id: 'spc_2', type: 'SPACER', props: { height: 16 } },
          { id: 'txt_desc', type: 'TEXT', props: { text: 'Special deals and offers are waiting for you! Explore the latest products in this campaign.', style: 'bodyLarge', align: 'center' } }
        ]
      }
    ]
  },
  'product_201': {
    screen: 'PRODUCT_DETAIL',
    version: '1.0',
    theme: { primaryColor: '#4F46E5', accentColor: '#10B981', mode: 'LIGHT' },
    sections: [
      {
        id: 'img_prod_201',
        type: 'IMAGE',
        props: { imageUrl: 'https://picsum.photos/id/96/800/600', height: 280 }
      },
      {
        id: 'col_details',
        type: 'COLUMN',
        props: { padding: 16 },
        items: [
          { id: 'txt_title', type: 'TEXT', props: { text: 'คีย์บอร์ดไร้สาย Mechanical RGB', style: 'headlineMedium', weight: 'bold' } },
          {
            id: 'row_price',
            type: 'ROW',
            props: { spacing: 8, align: 'bottom' },
            items: [
              { id: 'txt_price', type: 'TEXT', props: { text: '฿2,490', style: 'headlineSmall', color: 'primary', weight: 'bold' } },
              { id: 'txt_old_price', type: 'TEXT', props: { text: '฿3,290', style: 'bodyLarge', color: 'gray', decoration: 'line-through' } },
              { id: 'txt_badge', type: 'TEXT', props: { text: 'ลด 24%', style: 'labelMedium', color: '#EF4444', weight: 'bold' } }
            ]
          },
          { id: 'spc_1', type: 'SPACER', props: { height: 16 } },
          { id: 'txt_desc_title', type: 'TEXT', props: { text: 'จุดเด่นและสเปกสินค้า', style: 'titleMedium', weight: 'semiBold' } },
          { id: 'spc_2', type: 'SPACER', props: { height: 8 } },
          { id: 'txt_desc', type: 'TEXT', props: { text: '• เชื่อมต่อ 3 โหมด: Bluetooth 5.3, 2.4GHz Wireless และ USB Type-C\n• แบตเตอรี่อึด 4,000 mAh ใช้งานได้สูงสุด 200 ชั่วโมง\n• ไฟ RGB 16.8 ล้านสี ปรับแต่งได้ 18 โหมด\n• คีย์แคป PBT Double-shot ทนทาน ตัวอักษรไม่ลอก', style: 'bodyMedium' } },
          { id: 'spc_3', type: 'SPACER', props: { height: 24 } },
          {
            id: 'btn_add_cart',
            type: 'BUTTON',
            props: { text: 'เพิ่มลงตะกร้า • ฿2,490', icon: 'ShoppingCart' },
            action: { type: 'ADD_TO_CART', payload: { productId: '201', quantity: 1, name: 'คีย์บอร์ดไร้สาย Mechanical', price: '฿2,490', imageUrl: 'https://picsum.photos/id/96/300/300' } }
          }
        ]
      }
    ]
  },
  'campaign_gadget_expo': {
    screen: 'CAMPAIGN',
    version: '1.0',
    theme: { primaryColor: '#059669', accentColor: '#F59E0B', mode: 'LIGHT' },
    sections: [
      { id: 'img_banner', type: 'IMAGE', props: { imageUrl: 'https://picsum.photos/id/201/800/400', height: 200 } },
      {
        id: 'col_content',
        type: 'COLUMN',
        props: { padding: 16 },
        items: [
          { id: 'txt_title', type: 'TEXT', props: { text: '⚡ GADGET EXPO 2026', style: 'headlineMedium', weight: 'bold', color: 'primary' } },
          { id: 'txt_sub', type: 'TEXT', props: { text: 'มหกรรมสินค้าไอทีลดสูงสุด 70% ช้อปคุ้มตลอดสัปดาห์!', style: 'bodyLarge' } },
          { id: 'spc_1', type: 'SPACER', props: { height: 16 } },
          {
            id: 'row_vouchers',
            type: 'ROW',
            props: { spacing: 8 },
            items: [
              {
                id: 'btn_v1',
                type: 'BUTTON',
                props: { text: '🎁 โค้ดลด ฿500' },
                action: { type: 'ANALYTICS', payload: { event: 'claim_voucher_500' } }
              },
              {
                id: 'btn_v2',
                type: 'BUTTON',
                props: { text: '🚚 ส่งฟรีทั้งงาน' },
                action: { type: 'ANALYTICS', payload: { event: 'claim_free_shipping' } }
              }
            ]
          },
          { id: 'spc_2', type: 'SPACER', props: { height: 24 } },
          { id: 'txt_highlight', type: 'TEXT', props: { text: 'ดีลเด็ดประจำวัน', style: 'titleMedium', weight: 'bold' } },
          { id: 'spc_3', type: 'SPACER', props: { height: 12 } },
          {
            id: 'btn_view_keyboard',
            type: 'BUTTON',
            props: { text: 'ดู Mechanical Keyboard (฿2,490)' },
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://product/201' } }
          },
          { id: 'spc_4', type: 'SPACER', props: { height: 8 } },
          {
            id: 'btn_view_headphone',
            type: 'BUTTON',
            props: { text: 'ดู หูฟังบลูทูธไร้สาย (฿890)' },
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://product/101' } }
          }
        ]
      }
    ]
  },
  'campaign_super_brand_day': {
    screen: 'CAMPAIGN',
    version: '1.0',
    theme: { primaryColor: '#7C3AED', accentColor: '#EC4899', mode: 'LIGHT' },
    sections: [
      { id: 'img_banner', type: 'IMAGE', props: { imageUrl: 'https://picsum.photos/id/180/800/400', height: 200 } },
      {
        id: 'col_content',
        type: 'COLUMN',
        props: { padding: 16 },
        items: [
          { id: 'txt_title', type: 'TEXT', props: { text: '🏆 SUPER BRAND DAY', style: 'headlineMedium', weight: 'bold', color: 'primary' } },
          { id: 'txt_sub', type: 'TEXT', props: { text: 'การันตีของแท้ 100% รับประกันศูนย์ไทย พร้อมเงินคืนสูงสุด 15% Coins', style: 'bodyLarge' } },
          { id: 'spc_1', type: 'SPACER', props: { height: 20 } },
          {
            id: 'btn_explore_deals',
            type: 'BUTTON',
            props: { text: '🔥 ดีลเด่น: หูฟังบลูทูธ ฿890' },
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://product/101' } }
          },
          { id: 'spc_2', type: 'SPACER', props: { height: 10 } },
          {
            id: 'btn_explore_kb',
            type: 'BUTTON',
            props: { text: '🔥 ดีลเด่น: คีย์บอร์ดไร้สาย ฿2,490' },
            action: { type: 'NAVIGATE', payload: { target: 'flexifeed://product/201' } }
          }
        ]
      }
    ]
  }
};
