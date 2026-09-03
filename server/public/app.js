// FlexiFeed Studio - Client Application Logic

let currentFeed = {
  screen: "HOME_FEED",
  version: "1.0",
  sections: []
};

// Clock in mobile status bar
function updateClock() {
  const now = new Date();
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  const clockEl = document.getElementById('status-clock');
  if (clockEl) clockEl.textContent = `${hours}:${minutes}`;
}
setInterval(updateClock, 1000);
updateClock();

// Toast notification helper
function showToast(message, isError = false) {
  const toast = document.getElementById('toast');
  const toastMessage = document.getElementById('toast-message');
  toastMessage.textContent = message;
  toast.style.background = isError ? '#EF4444' : '#10B981';
  toast.classList.remove('hidden');
  setTimeout(() => {
    toast.classList.add('hidden');
  }, 3000);
}

// Fetch active feed from server
async function loadFeedFromServer() {
  try {
    const res = await fetch('/api/v1/home-feed');
    if (!res.ok) throw new Error(`HTTP error! status: ${res.status}`);
    const data = await res.json();
    currentFeed = data;
    renderVisualBuilder();
    syncJsonEditor();
    renderMobilePreview();
  } catch (err) {
    console.error('Failed to load feed from server:', err);
    showToast('Failed to connect to SDUI Server', true);
  }
}

// Publish feed to server
async function publishFeedToServer() {
  try {
    // Read from current form state first
    readFromVisualBuilder();
    const res = await fetch('/api/v1/home-feed', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(currentFeed)
    });
    const result = await res.json();
    if (result.success) {
      showToast('🚀 Changes published to Live Mobile Feed!');
      renderMobilePreview();
      syncJsonEditor();
    } else {
      showToast(result.error || 'Failed to publish', true);
    }
  } catch (err) {
    console.error('Error publishing feed:', err);
    showToast('Network error while publishing feed', true);
  }
}

// Switch Preset
async function loadPreset(presetId) {
  try {
    const res = await fetch(`/api/v1/preset/${presetId}`, { method: 'POST' });
    const result = await res.json();
    if (result.success) {
      showToast(`Loaded preset: ${presetId}`);
      await loadFeedFromServer();
    }
  } catch (err) {
    showToast('Error loading preset', true);
  }
}

// Reset feed
async function resetFeed() {
  if (!confirm('Are you sure you want to reset the feed to default?')) return;
  try {
    const res = await fetch('/api/v1/reset', { method: 'POST' });
    const result = await res.json();
    if (result.success) {
      showToast('Feed reset to default configuration');
      await loadFeedFromServer();
    }
  } catch (err) {
    showToast('Error resetting feed', true);
  }
}

// Synchronize to Raw JSON Editor
function syncJsonEditor() {
  const textarea = document.getElementById('json-editor-textarea');
  if (textarea) {
    textarea.value = JSON.stringify(currentFeed, null, 2);
  }
}

// Read from Raw JSON Editor
function readFromJsonEditor() {
  const textarea = document.getElementById('json-editor-textarea');
  if (!textarea) return false;
  try {
    const parsed = JSON.parse(textarea.value);
    if (!parsed.sections || !Array.isArray(parsed.sections)) {
      throw new Error('Payload must contain sections array');
    }
    currentFeed = parsed;
    renderVisualBuilder();
    renderMobilePreview();
    return true;
  } catch (e) {
    showToast('JSON Syntax Error: ' + e.message, true);
    return false;
  }
}

// Read form values from Visual Builder into currentFeed object
function readFromVisualBuilder() {
  const screenInput = document.getElementById('input-screen-name');
  const versionInput = document.getElementById('input-screen-version');
  if (screenInput) currentFeed.screen = screenInput.value;
  if (versionInput) currentFeed.version = versionInput.value;
}

// Render Visual Builder Sections
function renderVisualBuilder() {
  const container = document.getElementById('sections-container');
  if (!container) return;

  const screenInput = document.getElementById('input-screen-name');
  const versionInput = document.getElementById('input-screen-version');
  if (screenInput) screenInput.value = currentFeed.screen || 'HOME_FEED';
  if (versionInput) versionInput.value = currentFeed.version || '1.0';

  container.innerHTML = '';

  currentFeed.sections.forEach((sec, secIndex) => {
    const card = document.createElement('div');
    card.className = 'section-card';
    card.dataset.index = secIndex;

    const badgeClass = sec.type === 'CAROUSEL' ? 'badge-carousel' : (sec.type === 'HORIZONTAL_LIST' ? 'badge-horizontal' : 'badge-grid');

    card.innerHTML = `
      <div class="section-card-header">
        <div class="section-card-title">
          <span class="section-type-badge ${badgeClass}">${sec.type}</span>
          <span class="section-id-tag">#${sec.id}</span>
        </div>
        <div class="section-card-actions">
          <button class="btn-icon" title="Move Up" onclick="moveSection(${secIndex}, -1)">▲</button>
          <button class="btn-icon" title="Move Down" onclick="moveSection(${secIndex}, 1)">▼</button>
          <button class="btn-icon delete" title="Delete Section" onclick="deleteSection(${secIndex})">✕</button>
        </div>
      </div>
      <div class="section-card-body">
        <!-- Section Props -->
        <div class="section-props-grid">
          <div class="form-group">
            <label>Section ID</label>
            <input type="text" class="input-text" value="${sec.id}" onchange="updateSectionId(${secIndex}, this.value)">
          </div>
          ${renderSectionPropFields(sec, secIndex)}
        </div>

        <!-- Section Items Header -->
        <div class="items-list-header">
          <h4>Items (${(sec.items || []).length})</h4>
          <button class="btn btn-sm btn-secondary" onclick="addItemToSection(${secIndex})">➕ Add Item</button>
        </div>

        <!-- Items Container -->
        <div class="items-container">
          ${(sec.items || []).map((item, itemIndex) => renderItemRow(item, secIndex, itemIndex, sec.type)).join('')}
        </div>
      </div>
    `;

    container.appendChild(card);
  });
}

function renderSectionPropFields(sec, secIndex) {
  const props = sec.props || {};
  if (sec.type === 'CAROUSEL') {
    return `
      <div class="form-group">
        <label>Auto-scroll Interval (ms)</label>
        <input type="number" class="input-text" value="${props.autoScrollInterval || 4000}" step="500" min="1000" onchange="updateProp(${secIndex}, 'autoScrollInterval', Number(this.value))">
      </div>
    `;
  } else if (sec.type === 'HORIZONTAL_LIST') {
    return `
      <div class="form-group">
        <label>Header Title</label>
        <input type="text" class="input-text" value="${props.title || '⚡ Flash Sale'}" onchange="updateProp(${secIndex}, 'title', this.value)">
      </div>
      <div class="form-group">
        <label>Countdown (seconds)</label>
        <input type="number" class="input-text" value="${props.countdownRemainingSec || 7200}" onchange="updateProp(${secIndex}, 'countdownRemainingSec', Number(this.value))">
      </div>
    `;
  } else if (sec.type === 'GRID_2X2') {
    return `
      <div class="form-group">
        <label>Header Title</label>
        <input type="text" class="input-text" value="${props.title || 'สินค้าแนะนำสำหรับคุณ'}" onchange="updateProp(${secIndex}, 'title', this.value)">
      </div>
    `;
  }
  return '';
}

function renderItemRow(item, secIndex, itemIndex, sectionType) {
  const props = item.props || {};
  const imgUrl = item.imageUrl || props.thumbnailUrl || props.imageUrl || 'https://picsum.photos/200/200';
  const name = props.name || item.id;
  const price = props.price || '';
  const origPrice = props.originalPrice || '';

  if (sectionType === 'CAROUSEL') {
    const target = (item.action && item.action.payload && item.action.payload.target) || '';
    return `
      <div class="item-row-card">
        <img class="item-thumb" src="${imgUrl}" alt="" onerror="this.src='https://picsum.photos/200/100'">
        <div class="item-fields" style="grid-template-columns: 1fr 2fr;">
          <div class="form-group">
            <label>Banner Image URL</label>
            <input type="text" class="input-text" value="${item.imageUrl || ''}" onchange="updateCarouselItemImage(${secIndex}, ${itemIndex}, this.value)">
          </div>
          <div class="form-group">
            <label>Click Target URL</label>
            <input type="text" class="input-text" value="${target}" onchange="updateCarouselItemTarget(${secIndex}, ${itemIndex}, this.value)">
          </div>
        </div>
        <button class="btn-icon delete" title="Delete" onclick="deleteItemFromSection(${secIndex}, ${itemIndex})">✕</button>
      </div>
    `;
  }

  return `
    <div class="item-row-card">
      <img class="item-thumb" src="${imgUrl}" alt="" onerror="this.src='https://picsum.photos/200/200'">
      <div class="item-fields">
        <div class="form-group">
          <label>Product Name</label>
          <input type="text" class="input-text" value="${name}" onchange="updateItemProp(${secIndex}, ${itemIndex}, 'name', this.value)">
        </div>
        <div class="form-group">
          <label>Price</label>
          <input type="text" class="input-text" value="${price}" onchange="updateItemProp(${secIndex}, ${itemIndex}, 'price', this.value)">
        </div>
        ${sectionType === 'HORIZONTAL_LIST' ? `
          <div class="form-group">
            <label>Original Price</label>
            <input type="text" class="input-text" value="${origPrice}" onchange="updateItemProp(${secIndex}, ${itemIndex}, 'originalPrice', this.value)">
          </div>
        ` : `
          <div class="form-group">
            <label>Rating (1-5)</label>
            <input type="number" step="0.1" max="5" min="1" class="input-text" value="${props.rating || 4.8}" onchange="updateItemProp(${secIndex}, ${itemIndex}, 'rating', Number(this.value))">
          </div>
        `}
      </div>
      <button class="btn-icon delete" title="Delete" onclick="deleteItemFromSection(${secIndex}, ${itemIndex})">✕</button>
    </div>
  `;
}

// Component mutations
window.moveSection = function(index, direction) {
  const newIndex = index + direction;
  if (newIndex < 0 || newIndex >= currentFeed.sections.length) return;
  const temp = currentFeed.sections[index];
  currentFeed.sections[index] = currentFeed.sections[newIndex];
  currentFeed.sections[newIndex] = temp;
  renderVisualBuilder();
  syncJsonEditor();
  renderMobilePreview();
};

window.deleteSection = function(index) {
  currentFeed.sections.splice(index, 1);
  renderVisualBuilder();
  syncJsonEditor();
  renderMobilePreview();
};

window.updateSectionId = function(secIndex, newId) {
  currentFeed.sections[secIndex].id = newId;
  syncJsonEditor();
};

window.updateProp = function(secIndex, propKey, value) {
  if (!currentFeed.sections[secIndex].props) currentFeed.sections[secIndex].props = {};
  currentFeed.sections[secIndex].props[propKey] = value;
  syncJsonEditor();
  renderMobilePreview();
};

window.updateItemProp = function(secIndex, itemIndex, key, value) {
  const item = currentFeed.sections[secIndex].items[itemIndex];
  if (!item.props) item.props = {};
  item.props[key] = value;
  syncJsonEditor();
  renderMobilePreview();
};

window.updateCarouselItemImage = function(secIndex, itemIndex, value) {
  const item = currentFeed.sections[secIndex].items[itemIndex];
  item.imageUrl = value;
  syncJsonEditor();
  renderMobilePreview();
};

window.updateCarouselItemTarget = function(secIndex, itemIndex, value) {
  const item = currentFeed.sections[secIndex].items[itemIndex];
  if (!item.action) item.action = { type: 'NAVIGATE', payload: {} };
  if (!item.action.payload) item.action.payload = {};
  item.action.payload.target = value;
  syncJsonEditor();
};

window.deleteItemFromSection = function(secIndex, itemIndex) {
  currentFeed.sections[secIndex].items.splice(itemIndex, 1);
  renderVisualBuilder();
  syncJsonEditor();
  renderMobilePreview();
};

window.addItemToSection = function(secIndex) {
  const sec = currentFeed.sections[secIndex];
  if (!sec.items) sec.items = [];
  const nextNum = sec.items.length + 1;

  if (sec.type === 'CAROUSEL') {
    sec.items.push({
      id: `banner_slide_${Date.now()}`,
      imageUrl: `https://picsum.photos/id/${100 + nextNum}/800/400`,
      action: { type: 'NAVIGATE', payload: { target: `flexifeed://campaign/new-${nextNum}` } }
    });
  } else if (sec.type === 'HORIZONTAL_LIST') {
    sec.items.push({
      id: `prod_${Date.now()}`,
      type: 'PRODUCT_CARD_COMPACT',
      props: {
        name: `สินค้า Flash Sale #${nextNum}`,
        price: '฿599',
        originalPrice: '฿1,200',
        thumbnailUrl: `https://picsum.photos/id/${200 + nextNum}/200/200`
      },
      action: { type: 'NAVIGATE', payload: { target: `flexifeed://product/${Date.now()}` } }
    });
  } else if (sec.type === 'GRID_2X2') {
    sec.items.push({
      id: `prod_grid_${Date.now()}`,
      type: 'PRODUCT_CARD_FULL',
      props: {
        name: `สินค้าแนะนำใหม่ #${nextNum}`,
        price: '฿1,890',
        rating: 4.9,
        thumbnailUrl: `https://picsum.photos/id/${300 + nextNum}/300/300`
      },
      action: { type: 'ADD_TO_CART', payload: { productId: `${Date.now()}`, quantity: 1 } }
    });
  }

  renderVisualBuilder();
  syncJsonEditor();
  renderMobilePreview();
};

// Add New Section
function addNewSection(type) {
  const randomId = Math.floor(Math.random() * 900) + 100;
  let newSection;

  if (type === 'CAROUSEL') {
    newSection = {
      id: `sec_carousel_${randomId}`,
      type: 'CAROUSEL',
      props: { autoScrollInterval: 4000 },
      items: [
        {
          id: `banner_${randomId}`,
          imageUrl: 'https://picsum.photos/800/400',
          action: { type: 'NAVIGATE', payload: { target: 'flexifeed://campaign/featured' } }
        }
      ]
    };
  } else if (type === 'HORIZONTAL_LIST') {
    newSection = {
      id: `sec_flash_${randomId}`,
      type: 'HORIZONTAL_LIST',
      props: { title: '⚡ Flash Sale ด่วนพิเศษ', countdownRemainingSec: 5400 },
      items: [
        {
          id: `prod_flash_${randomId}`,
          type: 'PRODUCT_CARD_COMPACT',
          props: {
            name: 'สินค้าลดราคาพิเศษ',
            price: '฿790',
            originalPrice: '฿1,590',
            thumbnailUrl: 'https://picsum.photos/200/200'
          },
          action: { type: 'NAVIGATE', payload: { target: `flexifeed://product/${randomId}` } }
        }
      ]
    };
  } else {
    newSection = {
      id: `sec_grid_${randomId}`,
      type: 'GRID_2X2',
      props: { title: 'สินค้าแนะนำยอดนิยม' },
      items: [
        {
          id: `prod_grid_${randomId}`,
          type: 'PRODUCT_CARD_FULL',
          props: {
            name: 'สินค้าคุณภาพแนะนำ',
            price: '฿1,490',
            rating: 4.9,
            thumbnailUrl: 'https://picsum.photos/300/300'
          },
          action: { type: 'ADD_TO_CART', payload: { productId: `${randomId}`, quantity: 1 } }
        }
      ]
    };
  }

  currentFeed.sections.push(newSection);
  renderVisualBuilder();
  syncJsonEditor();
  renderMobilePreview();
  showToast(`Added ${type} section`);
}

// Render Live Mobile Phone Preview
function renderMobilePreview() {
  const scrollContainer = document.getElementById('mock-feed-scrollable');
  if (!scrollContainer) return;

  scrollContainer.innerHTML = '';

  // SDUI Info Pill
  const pill = document.createElement('div');
  pill.style.padding = '4px 12px';
  pill.style.background = 'rgba(79, 70, 229, 0.08)';
  pill.style.border = '1px solid rgba(79, 70, 229, 0.2)';
  pill.style.borderRadius = '6px';
  pill.style.margin = '8px 14px 0';
  pill.style.fontSize = '9px';
  pill.style.fontWeight = '600';
  pill.style.color = '#4F46E5';
  pill.textContent = `Rendered via SDUI DSL (Screen: ${currentFeed.screen} v${currentFeed.version})`;
  scrollContainer.appendChild(pill);

  (currentFeed.sections || []).forEach(sec => {
    if (sec.type === 'CAROUSEL') {
      const carouselWrap = document.createElement('div');
      carouselWrap.className = 'mock-carousel';
      const items = sec.items || [];
      const firstItem = items[0] || {};
      const imgUrl = firstItem.imageUrl || (firstItem.props && firstItem.props.imageUrl) || 'https://picsum.photos/800/400';

      carouselWrap.innerHTML = `
        <div class="mock-carousel-slide">
          <img src="${imgUrl}" alt="Banner">
        </div>
        <div class="mock-carousel-dots">
          ${items.map((_, i) => `<div class="mock-dot ${i === 0 ? 'active' : ''}"></div>`).join('')}
        </div>
      `;
      scrollContainer.appendChild(carouselWrap);
    } else if (sec.type === 'HORIZONTAL_LIST') {
      const flashWrap = document.createElement('div');
      flashWrap.className = 'mock-flash-sale';
      const props = sec.props || {};
      const title = props.title || '⚡ Flash Sale';
      const secRemaining = props.countdownRemainingSec || 7200;
      const hours = String(Math.floor(secRemaining / 3600)).padStart(2, '0');
      const mins = String(Math.floor((secRemaining % 3600) / 60)).padStart(2, '0');
      const secs = String(secRemaining % 60).padStart(2, '0');

      flashWrap.innerHTML = `
        <div class="mock-section-header">
          <div class="mock-section-title">${title}</div>
          <div class="mock-timer-badge">⏱ ${hours}:${mins}:${secs}</div>
        </div>
        <div class="mock-horizontal-row">
          ${(sec.items || []).map(item => {
            const iprops = item.props || {};
            const img = iprops.thumbnailUrl || iprops.imageUrl || 'https://picsum.photos/200/200';
            return `
              <div class="mock-compact-card">
                <div class="thumb-wrap">
                  <img src="${img}" alt="">
                  <span class="sale-tag">SALE</span>
                </div>
                <div class="p-name">${iprops.name || 'สินค้า'}</div>
                <div class="price-row">
                  <span class="price">${iprops.price || '฿0'}</span>
                  ${iprops.originalPrice ? `<span class="orig-price">${iprops.originalPrice}</span>` : ''}
                </div>
              </div>
            `;
          }).join('')}
        </div>
      `;
      scrollContainer.appendChild(flashWrap);
    } else if (sec.type === 'GRID_2X2') {
      const gridWrap = document.createElement('div');
      gridWrap.className = 'mock-grid-section';
      const props = sec.props || {};
      const title = props.title || 'สินค้าแนะนำสำหรับคุณ';

      gridWrap.innerHTML = `
        <div class="mock-section-header">
          <div class="mock-section-title">${title}</div>
        </div>
        <div class="mock-grid-2x2">
          ${(sec.items || []).map(item => {
            const iprops = item.props || {};
            const img = iprops.thumbnailUrl || iprops.imageUrl || 'https://picsum.photos/300/300';
            return `
              <div class="mock-grid-card">
                <div class="thumb-wrap">
                  <img src="${img}" alt="">
                  ${iprops.rating ? `<span class="rating-tag">★ ${iprops.rating}</span>` : ''}
                </div>
                <div class="p-name">${iprops.name || 'สินค้า'}</div>
                <div class="price">${iprops.price || '฿0'}</div>
                <button class="btn-add">🛒 ใส่ตะกร้า</button>
              </div>
            `;
          }).join('')}
        </div>
      `;
      scrollContainer.appendChild(gridWrap);
    }
  });
}

// Wire Event Listeners
document.addEventListener('DOMContentLoaded', () => {
  // Tabs
  const tabVisual = document.getElementById('tab-btn-visual');
  const tabJson = document.getElementById('tab-btn-json');
  const panelVisual = document.getElementById('panel-visual');
  const panelJson = document.getElementById('panel-json');

  tabVisual.addEventListener('click', () => {
    tabVisual.classList.add('active');
    tabJson.classList.remove('active');
    panelVisual.classList.add('active');
    panelJson.classList.remove('active');
    readFromJsonEditor();
  });

  tabJson.addEventListener('click', () => {
    tabJson.classList.add('active');
    tabVisual.classList.remove('active');
    panelJson.classList.add('active');
    panelVisual.classList.remove('active');
    readFromVisualBuilder();
    syncJsonEditor();
  });

  // Buttons
  document.getElementById('btn-publish-feed').addEventListener('click', publishFeedToServer);
  document.getElementById('btn-reset-default').addEventListener('click', resetFeed);

  document.getElementById('btn-preset-mega').addEventListener('click', function() {
    document.querySelectorAll('.btn-preset').forEach(b => b.classList.remove('active'));
    this.classList.add('active');
    loadPreset('mega-sale');
  });

  document.getElementById('btn-preset-tech').addEventListener('click', function() {
    document.querySelectorAll('.btn-preset').forEach(b => b.classList.remove('active'));
    this.classList.add('active');
    loadPreset('tech-weekend');
  });

  document.getElementById('btn-add-section').addEventListener('click', () => {
    const sel = document.getElementById('select-new-section');
    addNewSection(sel.value);
  });

  document.getElementById('btn-format-json').addEventListener('click', () => {
    const textarea = document.getElementById('json-editor-textarea');
    try {
      const obj = JSON.parse(textarea.value);
      textarea.value = JSON.stringify(obj, null, 2);
      showToast('JSON Formatted');
    } catch (e) {
      showToast('Invalid JSON: ' + e.message, true);
    }
  });

  document.getElementById('btn-validate-json').addEventListener('click', () => {
    if (readFromJsonEditor()) {
      showToast('✓ JSON DSL is valid!');
    }
  });

  // Load initial feed
  loadFeedFromServer();
});
