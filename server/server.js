const http = require('http');
const fs = require('fs');
const path = require('path');

const PORT = process.env.PORT || 8080;
const DATA_FILE = path.join(__dirname, 'data', 'feed.json');
const PUBLIC_DIR = path.join(__dirname, 'public');

// Ensure data directory and file exist
if (!fs.existsSync(path.join(__dirname, 'data'))) {
  fs.mkdirSync(path.join(__dirname, 'data'), { recursive: true });
}

// Preset Screens for Generic Flow
const SCREENS = {
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

// Preset feeds
const PRESETS = {
  'mega-sale': {
    screen: 'HOME_FEED',
    version: '1.0',
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

const PRESETS_DIR = path.join(__dirname, 'data', 'presets');
if (!fs.existsSync(PRESETS_DIR)) {
  fs.mkdirSync(PRESETS_DIR, { recursive: true });
}

// Helper to get preset with preserved customizations
function getPreset(presetId) {
  const pId = presetId === 'tech-weekend' ? 'tech-weekend' : 'mega-sale';
  const presetFile = path.join(PRESETS_DIR, `${pId}.json`);
  if (fs.existsSync(presetFile)) {
    try {
      const parsed = JSON.parse(fs.readFileSync(presetFile, 'utf8'));
      if (parsed && parsed.theme && parsed.sections) {
        parsed.presetId = pId;
        return parsed;
      }
    } catch (e) {
      console.error(`Error reading preset file for ${pId}:`, e);
    }
  }
  const defaultObj = JSON.parse(JSON.stringify(PRESETS[pId] || PRESETS['mega-sale']));
  defaultObj.presetId = pId;
  return defaultObj;
}

// Helper to save preset with customizations
function savePreset(presetId, data) {
  const pId = presetId === 'tech-weekend' ? 'tech-weekend' : 'mega-sale';
  const presetFile = path.join(PRESETS_DIR, `${pId}.json`);
  const toSave = { ...data, presetId: pId };
  fs.writeFileSync(presetFile, JSON.stringify(toSave, null, 2), 'utf8');
}

// Seed default preset files if not existing
for (const [id, presetObj] of Object.entries(PRESETS)) {
  const pFile = path.join(PRESETS_DIR, `${id}.json`);
  if (!fs.existsSync(pFile)) {
    fs.writeFileSync(pFile, JSON.stringify({ ...presetObj, presetId: id }, null, 2), 'utf8');
  }
}

function readCurrentFeed() {
  if (fs.existsSync(DATA_FILE)) {
    try {
      const data = fs.readFileSync(DATA_FILE, 'utf8');
      const parsed = JSON.parse(data);
      if (!parsed.presetId) {
        parsed.presetId = (parsed.version === '1.2' || (parsed.screen && parsed.screen.includes('TECH'))) ? 'tech-weekend' : 'mega-sale';
      }
      return parsed;
    } catch (e) {
      console.error('Error reading data file, using default preset:', e);
    }
  }
  return getPreset('mega-sale');
}

function writeCurrentFeed(feedData) {
  fs.writeFileSync(DATA_FILE, JSON.stringify(feedData, null, 2), 'utf8');
}

const MIME_TYPES = {
  '.html': 'text/html',
  '.css': 'text/css',
  '.js': 'text/javascript',
  '.json': 'application/json',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.svg': 'image/svg+xml'
};

// Active SSE clients for Live Hot-Reload
const sseClients = new Set();

function broadcastFeedUpdate(action, presetId) {
  const eventPayload = {
    action: action || 'FEED_UPDATED',
    presetId: presetId || null,
    timestamp: Date.now()
  };
  const message = `event: FEED_UPDATED\ndata: ${JSON.stringify(eventPayload)}\n\n`;
  console.log(`[SDUI Server] ⚡ SSE Broadcast ${action} (preset: ${presetId}) to ${sseClients.size} client(s)`);
  for (const client of sseClients) {
    try {
      client.write(message);
    } catch (err) {
      console.error('[SDUI Server] SSE write error, removing client:', err.message);
      sseClients.delete(client);
    }
  }
}

// Periodic keepalive ping every 15s to keep connections alive
setInterval(() => {
  if (sseClients.size > 0) {
    for (const client of sseClients) {
      try {
        client.write(': keepalive\n\n');
      } catch (err) {
        sseClients.delete(client);
      }
    }
  }
}, 15000);

const server = http.createServer((req, res) => {
  // CORS Headers for API calls
  res.setHeader('Access-Control-Allow-Origin', '*');
  res.setHeader('Access-Control-Allow-Methods', 'GET, POST, PUT, DELETE, OPTIONS');
  res.setHeader('Access-Control-Allow-Headers', 'Content-Type, Authorization');

  if (req.method === 'OPTIONS') {
    res.writeHead(204);
    res.end();
    return;
  }

  const urlObj = new URL(req.url, `http://${req.headers.host}`);
  const pathname = urlObj.pathname;

  // API: GET /api/v1/feed-stream (Server-Sent Events for Live Hot-Reload)
  if (pathname === '/api/v1/feed-stream' && req.method === 'GET') {
    res.writeHead(200, {
      'Content-Type': 'text/event-stream',
      'Cache-Control': 'no-cache, no-transform',
      'Connection': 'keep-alive',
      'Access-Control-Allow-Origin': '*'
    });
    if (typeof res.flushHeaders === 'function') {
      res.flushHeaders();
    }
    res.write(': keepalive\n\n');
    res.write(`event: CONNECTED\ndata: ${JSON.stringify({ message: 'Live Hot-Reload SSE Connected', timestamp: Date.now() })}\n\n`);

    sseClients.add(res);
    console.log(`[SDUI Server] 🟢 SSE client connected. Active connections: ${sseClients.size}`);

    req.on('close', () => {
      sseClients.delete(res);
      console.log(`[SDUI Server] 🔴 SSE client disconnected. Active connections: ${sseClients.size}`);
    });
    return;
  }

  // API: GET /api/v1/home-feed (supports optional ?campaign=tech-weekend or ?campaign=mega-sale)
  if (pathname === '/api/v1/home-feed' && req.method === 'GET') {
    const campaign = urlObj.searchParams.get('campaign');
    let feed;
    if (campaign) {
      const lower = campaign.toLowerCase();
      if (lower.includes('tech')) {
        feed = getPreset('tech-weekend');
      } else if (lower.includes('mega') || lower.includes('default')) {
        feed = getPreset('mega-sale');
      } else {
        feed = readCurrentFeed();
      }
    } else {
      feed = readCurrentFeed();
    }
    res.writeHead(200, {
      'Content-Type': 'application/json; charset=utf-8',
      'Cache-Control': 'no-cache, no-store, must-revalidate'
    });
    res.end(JSON.stringify(feed, null, 2));
    return;
  }

  // API: GET /api/v1/theme
  if (pathname === '/api/v1/theme' && req.method === 'GET') {
    const feed = readCurrentFeed();
    res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify(feed.theme || { primaryColor: '#4F46E5', accentColor: '#FF3366', mode: 'LIGHT' }));
    return;
  }

  // API: GET /api/v1/screen/:screenId
  if (pathname.startsWith('/api/v1/screen/') && req.method === 'GET') {
    const screenId = pathname.replace('/api/v1/screen/', '');
    if (screenId === 'home') {
      res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify(readCurrentFeed(), null, 2));
    } else if (SCREENS[screenId]) {
      res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
      res.end(JSON.stringify(SCREENS[screenId], null, 2));
    } else {
      res.writeHead(404, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: `Screen '${screenId}' not found` }));
    }
    return;
  }

  // API: POST /api/v1/theme
  if (pathname === '/api/v1/theme' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const themeData = JSON.parse(body);
        const currentFeed = readCurrentFeed();
        const activePreset = themeData.presetId || currentFeed.presetId || (currentFeed.version === '1.2' ? 'tech-weekend' : 'mega-sale');

        currentFeed.presetId = activePreset;
        currentFeed.theme = {
          primaryColor: themeData.primaryColor || currentFeed.theme?.primaryColor || '#4F46E5',
          accentColor: themeData.accentColor || currentFeed.theme?.accentColor || '#FF3366',
          mode: themeData.mode || currentFeed.theme?.mode || 'LIGHT'
        };
        writeCurrentFeed(currentFeed);

        // Also persist theme permanently to this preset file!
        const presetData = getPreset(activePreset);
        presetData.theme = currentFeed.theme;
        presetData.presetId = activePreset;
        savePreset(activePreset, presetData);

        console.log(`[SDUI Server] Updated theme for preset '${activePreset}': ${JSON.stringify(currentFeed.theme)}`);
        broadcastFeedUpdate('THEME_UPDATED', activePreset);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({
          success: true,
          message: `Theme saved for preset: ${activePreset}!`,
          theme: currentFeed.theme,
          presetId: activePreset
        }));
      } catch (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON payload: ' + err.message }));
      }
    });
    return;
  }

  // API: POST /api/v1/home-feed
  if (pathname === '/api/v1/home-feed' && req.method === 'POST') {
    let body = '';
    req.on('data', chunk => { body += chunk; });
    req.on('end', () => {
      try {
        const parsed = JSON.parse(body);
        if (!parsed.sections || !Array.isArray(parsed.sections)) {
          res.writeHead(400, { 'Content-Type': 'application/json' });
          res.end(JSON.stringify({ error: 'Payload must contain a sections array' }));
          return;
        }
        const activePreset = parsed.presetId || (parsed.version === '1.2' ? 'tech-weekend' : 'mega-sale');
        parsed.presetId = activePreset;
        writeCurrentFeed(parsed);
        savePreset(activePreset, parsed);

        console.log(`[SDUI Server] Updated feed configuration for preset '${activePreset}' (${parsed.sections.length} sections)`);
        broadcastFeedUpdate('FEED_UPDATED', activePreset);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, message: 'Configuration saved and published!', presetId: activePreset }));
      } catch (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON payload: ' + err.message }));
      }
    });
    return;
  }

  // API: POST /api/v1/reset
  if (pathname === '/api/v1/reset' && req.method === 'POST') {
    const defaultData = JSON.parse(JSON.stringify(PRESETS['mega-sale']));
    defaultData.presetId = 'mega-sale';
    savePreset('mega-sale', PRESETS['mega-sale']);
    savePreset('tech-weekend', PRESETS['tech-weekend']);
    writeCurrentFeed(defaultData);
    broadcastFeedUpdate('FEED_RESET', 'mega-sale');
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ success: true, message: 'Reset all presets to default configuration' }));
    return;
  }

  // API: POST /api/v1/preset/:id
  if (pathname.startsWith('/api/v1/preset/') && req.method === 'POST') {
    const presetId = pathname.replace('/api/v1/preset/', '');
    if (presetId === 'mega-sale' || presetId === 'tech-weekend' || PRESETS[presetId]) {
      const presetData = getPreset(presetId);
      presetData.presetId = presetId;
      writeCurrentFeed(presetData);
      console.log(`[SDUI Server] Switched to preset '${presetId}' with theme: ${JSON.stringify(presetData.theme)}`);
      broadcastFeedUpdate('PRESET_SWITCHED', presetId);
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({
        success: true,
        message: `Loaded preset: ${presetId}`,
        presetId,
        theme: presetData.theme
      }));
    } else {
      res.writeHead(404, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ error: `Preset '${presetId}' not found` }));
    }
    return;
  }

  // Static File Serving for Web Backoffice
  let filePath = path.join(PUBLIC_DIR, pathname === '/' ? 'index.html' : pathname);
  const extname = String(path.extname(filePath)).toLowerCase();
  const contentType = MIME_TYPES[extname] || 'application/octet-stream';

  fs.readFile(filePath, (err, content) => {
    if (err) {
      if (err.code === 'ENOENT') {
        res.writeHead(404, { 'Content-Type': 'text/html' });
        res.end('<h1>404 Not Found</h1>', 'utf-8');
      } else {
        res.writeHead(500);
        res.end('Server Error: ' + err.code);
      }
    } else {
      res.writeHead(200, {
        'Content-Type': contentType,
        'Cache-Control': 'no-cache, no-store, must-revalidate'
      });
      res.end(content, 'utf-8');
    }
  });
});

server.listen(PORT, '0.0.0.0', () => {
  console.log(`=======================================================`);
  console.log(`🚀 FlexiFeed SDUI Server is running on port ${PORT}`);
  console.log(`👉 Web Backoffice:  http://localhost:${PORT}`);
  console.log(`👉 SDUI Endpoint:   http://localhost:${PORT}/api/v1/home-feed`);
  console.log(`📱 Android Emulator: http://10.0.2.2:${PORT}/api/v1/home-feed`);
  console.log(`=======================================================`);
});
