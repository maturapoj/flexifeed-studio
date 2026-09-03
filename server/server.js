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
    theme: {
      primaryColor: '#4F46E5',
      accentColor: '#FF3366',
      mode: 'LIGHT'
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
      mode: 'LIGHT'
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
