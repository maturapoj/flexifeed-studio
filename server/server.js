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

// Preset feeds
const PRESETS = {
  'mega-sale': {
    screen: 'HOME_FEED',
    version: '1.0',
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

function readCurrentFeed() {
  if (fs.existsSync(DATA_FILE)) {
    try {
      const data = fs.readFileSync(DATA_FILE, 'utf8');
      return JSON.parse(data);
    } catch (e) {
      console.error('Error reading data file, using default preset:', e);
    }
  }
  return PRESETS['mega-sale'];
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

  // API: GET /api/v1/home-feed
  if (pathname === '/api/v1/home-feed' && req.method === 'GET') {
    const feed = readCurrentFeed();
    res.writeHead(200, { 'Content-Type': 'application/json; charset=utf-8' });
    res.end(JSON.stringify(feed, null, 2));
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
        writeCurrentFeed(parsed);
        console.log(`[SDUI Server] Updated feed configuration (${parsed.sections.length} sections)`);
        res.writeHead(200, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ success: true, message: 'Configuration saved and published!' }));
      } catch (err) {
        res.writeHead(400, { 'Content-Type': 'application/json' });
        res.end(JSON.stringify({ error: 'Invalid JSON payload: ' + err.message }));
      }
    });
    return;
  }

  // API: POST /api/v1/reset
  if (pathname === '/api/v1/reset' && req.method === 'POST') {
    writeCurrentFeed(PRESETS['mega-sale']);
    res.writeHead(200, { 'Content-Type': 'application/json' });
    res.end(JSON.stringify({ success: true, message: 'Reset to default feed preset' }));
    return;
  }

  // API: POST /api/v1/preset/:id
  if (pathname.startsWith('/api/v1/preset/') && req.method === 'POST') {
    const presetId = pathname.replace('/api/v1/preset/', '');
    if (PRESETS[presetId]) {
      writeCurrentFeed(PRESETS[presetId]);
      res.writeHead(200, { 'Content-Type': 'application/json' });
      res.end(JSON.stringify({ success: true, message: `Loaded preset: ${presetId}` }));
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
      res.writeHead(200, { 'Content-Type': contentType });
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
