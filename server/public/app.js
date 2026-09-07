// FlexiFeed Studio - Client Application Logic with Multi-Theme & Resizing Engine

let currentFeed = {
  screen: "HOME_FEED",
  version: "1.0",
  theme: {
    primaryColor: "#4F46E5",
    accentColor: "#FF3366",
    mode: "LIGHT"
  },
  sections: []
};

let currentZoom = 'fit';

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
    if (!currentFeed.theme) {
      currentFeed.theme = { primaryColor: "#4F46E5", accentColor: "#FF3366", mode: "LIGHT" };
    }
    const activePreset = currentFeed.presetId || (currentFeed.version === '1.2' ? 'tech-weekend' : 'mega-sale');
    document.querySelectorAll('.btn-preset').forEach(b => {
      b.classList.toggle('active', b.dataset.preset === activePreset);
    });
    renderVisualBuilder();
    syncThemeInputs();
    syncJsonEditor();
    renderMobilePreview();
    updatePhoneZoom();
  } catch (err) {
    console.error('Failed to load feed from server:', err);
    showToast('Failed to connect to SDUI Server', true);
  }
}

// Publish feed to server (Smart Tab Awareness)
async function publishFeedToServer() {
  try {
    const isJsonActive = document.getElementById('panel-json')?.classList.contains('active');
    const isThemeActive = document.getElementById('panel-theme')?.classList.contains('active');

    if (isJsonActive) {
      if (!readFromJsonEditor()) return;
    } else if (isThemeActive) {
      readFromThemeBuilder();
    } else {
      readFromVisualBuilder();
      readFromThemeBuilder();
    }

    const activePreset = currentFeed.presetId || (currentFeed.version === '1.2' ? 'tech-weekend' : 'mega-sale');
    currentFeed.presetId = activePreset;

    const res = await fetch('/api/v1/home-feed', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(currentFeed)
    });
    const result = await res.json();
    if (result.success) {
      showToast(`🚀 บันทึกและ Publish ลง Server [${activePreset}] สำเร็จ!`);
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

// Dedicated Save Theme to Server function
async function saveThemeToServer() {
  try {
    readFromThemeBuilder();
    const activePreset = currentFeed.presetId || (currentFeed.version === '1.2' ? 'tech-weekend' : 'mega-sale');
    const res = await fetch('/api/v1/theme', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        ...currentFeed.theme,
        presetId: activePreset
      })
    });
    const result = await res.json();
    if (result.success) {
      showToast(`🎨 บันทึก Theme สำหรับ [${activePreset === 'tech-weekend' ? 'Tech Weekend' : 'Mega Sale'}] สำเร็จแล้ว!`);
      syncJsonEditor();
      renderMobilePreview();
    } else {
      showToast(result.error || 'บันทึก Theme ไม่สำเร็จ', true);
    }
  } catch (err) {
    console.error('Error saving theme to server:', err);
    showToast('Network error while saving theme', true);
  }
}

// Switch Preset
async function loadPreset(presetId) {
  try {
    readFromThemeBuilder();
    // Auto-save active theme if it changed before switching
    const activePreset = currentFeed.presetId || (currentFeed.version === '1.2' ? 'tech-weekend' : 'mega-sale');
    if (currentFeed.theme && activePreset && activePreset !== presetId) {
      await fetch('/api/v1/theme', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ ...currentFeed.theme, presetId: activePreset })
      });
    }

    const res = await fetch(`/api/v1/preset/${presetId}`, { method: 'POST' });
    const result = await res.json();
    if (result.success) {
      showToast(`✨ สลับ Preset เป็น: ${presetId === 'tech-weekend' ? 'Tech Weekend' : 'Mega Sale'} (โหลด Theme เฉพาะตัวสำเร็จ)`);
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
    if (!currentFeed.theme) {
      currentFeed.theme = { primaryColor: "#4F46E5", accentColor: "#FF3366", mode: "LIGHT" };
    }
    renderVisualBuilder();
    syncThemeInputs();
    renderMobilePreview();
    return true;
  } catch (e) {
    showToast('Invalid JSON: ' + e.message, true);
    return false;
  }
}

// Sync Theme inputs from state
function syncThemeInputs() {
  const theme = currentFeed.theme || { primaryColor: "#4F46E5", accentColor: "#FF3366", mode: "LIGHT" };
  const inputPrimary = document.getElementById('input-theme-primary');
  const textPrimary = document.getElementById('text-theme-primary');
  const inputAccent = document.getElementById('input-theme-accent');
  const textAccent = document.getElementById('text-theme-accent');
  const selectMode = document.getElementById('select-theme-mode');

  if (inputPrimary) inputPrimary.value = theme.primaryColor || "#4F46E5";
  if (textPrimary) textPrimary.value = theme.primaryColor || "#4F46E5";
  if (inputAccent) inputAccent.value = theme.accentColor || "#FF3366";
  if (textAccent) textAccent.value = theme.accentColor || "#FF3366";
  if (selectMode) selectMode.value = theme.mode || "LIGHT";

  // Sync separate Logo theme inputs
  const inputLogoBg = document.getElementById('input-logo-bg');
  const textLogoBg = document.getElementById('text-logo-bg');
  const inputLogoIcon = document.getElementById('input-logo-icon');
  const textLogoIcon = document.getElementById('text-logo-icon');
  const inputLogoSub = document.getElementById('input-logo-sub');
  const textLogoSub = document.getElementById('text-logo-sub');

  const logoTheme = theme.logo || {};
  const logoBg = logoTheme.bgColor || theme.logoBgColor || theme.primaryColor || "#4F46E5";
  const logoIcon = logoTheme.iconColor || theme.logoIconColor || "#FFFFFF";
  const logoSub = logoTheme.subtitleColor || theme.logoSubtitleColor || theme.primaryColor || "#4F46E5";

  if (inputLogoBg) inputLogoBg.value = logoBg;
  if (textLogoBg) textLogoBg.value = logoBg;
  if (inputLogoIcon) inputLogoIcon.value = logoIcon;
  if (textLogoIcon) textLogoIcon.value = logoIcon;
  if (inputLogoSub) inputLogoSub.value = logoSub;
  if (textLogoSub) textLogoSub.value = logoSub;

  applyThemeToPreview(theme);
}

// Read Theme inputs into state
function readFromThemeBuilder() {
  const inputPrimary = document.getElementById('input-theme-primary');
  const inputAccent = document.getElementById('input-theme-accent');
  const selectMode = document.getElementById('select-theme-mode');
  const inputLogoBg = document.getElementById('input-logo-bg');
  const inputLogoIcon = document.getElementById('input-logo-icon');
  const inputLogoSub = document.getElementById('input-logo-sub');

  currentFeed.theme = {
    primaryColor: inputPrimary ? inputPrimary.value : "#4F46E5",
    accentColor: inputAccent ? inputAccent.value : "#FF3366",
    mode: selectMode ? selectMode.value : "LIGHT",
    logo: {
      bgColor: inputLogoBg ? inputLogoBg.value : (inputPrimary ? inputPrimary.value : "#4F46E5"),
      iconColor: inputLogoIcon ? inputLogoIcon.value : "#FFFFFF",
      subtitleColor: inputLogoSub ? inputLogoSub.value : (inputPrimary ? inputPrimary.value : "#4F46E5")
    }
  };
  applyThemeToPreview(currentFeed.theme);
}

// Apply App Theme colors to Phone Screen
function applyThemeToPreview(theme) {
  const phoneScreen = document.getElementById('phone-screen');
  if (!phoneScreen) return;

  phoneScreen.style.setProperty('--app-primary', theme.primaryColor || '#4F46E5');
  phoneScreen.style.setProperty('--app-accent', theme.accentColor || '#FF3366');

  if (theme.mode === 'DARK') {
    phoneScreen.classList.add('theme-dark');
  } else {
    phoneScreen.classList.remove('theme-dark');
  }

  // Separate Logo Theme rendering
  const logoTheme = theme.logo || {};
  const logoBg = logoTheme.bgColor || theme.logoBgColor || theme.primaryColor || '#4F46E5';
  const logoIcon = logoTheme.iconColor || theme.logoIconColor || '#FFFFFF';
  const logoSubColor = logoTheme.subtitleColor || theme.logoSubtitleColor || theme.primaryColor || '#4F46E5';

  const logoSub = document.getElementById('mock-logo-sub');
  if (logoSub) logoSub.style.color = logoSubColor;

  const logoBox = document.getElementById('mock-logo-box');
  if (logoBox) {
    logoBox.style.background = logoBg;
    logoBox.style.color = logoIcon;
  }

  const cartBadge = document.getElementById('mock-cart-badge');
  if (cartBadge) cartBadge.style.background = theme.accentColor;
}

// Read Screen Meta & Sections from Visual Builder inputs
function readFromVisualBuilder() {
  const screenNameInput = document.getElementById('input-screen-name');
  const screenVersionInput = document.getElementById('input-screen-version');
  if (screenNameInput) currentFeed.screen = screenNameInput.value.trim();
  if (screenVersionInput) currentFeed.version = screenVersionInput.value.trim();

  // Read props and items from rendered DOM
  const sectionCards = document.querySelectorAll('.section-card');
  const updatedSections = [];

  sectionCards.forEach((card) => {
    const sectionIndex = parseInt(card.dataset.sectionIndex, 10);
    const existingSection = currentFeed.sections[sectionIndex];
    if (!existingSection) return;

    const newSection = { ...existingSection, props: { ...existingSection.props } };

    // Read props
    card.querySelectorAll('[data-prop-key]').forEach(input => {
      const key = input.dataset.propKey;
      let val = input.value;
      if (input.type === 'number') val = parseInt(val, 10) || 0;
      newSection.props[key] = val;
    });

    // Read items
    const itemRows = card.querySelectorAll('.item-row-card');
    const newItems = [];
    itemRows.forEach((row) => {
      const itemIndex = parseInt(row.dataset.itemIndex, 10);
      const existingItem = existingSection.items[itemIndex] || {};
      const newItem = { ...existingItem, props: { ...(existingItem.props || {}) } };

      row.querySelectorAll('[data-item-prop]').forEach(field => {
        const propName = field.dataset.itemProp;
        if (propName === 'imageUrl') {
          newItem.imageUrl = field.value;
        } else if (propName === 'target') {
          if (!newItem.action) newItem.action = { type: 'NAVIGATE', payload: {} };
          if (!newItem.action.payload) newItem.action.payload = {};
          newItem.action.payload.target = field.value;
        } else {
          newItem.props[propName] = field.value;
        }
      });
      newItems.push(newItem);
    });

    newSection.items = newItems;
    updatedSections.push(newSection);
  });

  currentFeed.sections = updatedSections;
}

// Render Visual Builder Form
function renderVisualBuilder() {
  const container = document.getElementById('sections-container');
  if (!container) return;
  container.innerHTML = '';

  const screenNameInput = document.getElementById('input-screen-name');
  const screenVersionInput = document.getElementById('input-screen-version');
  if (screenNameInput) screenNameInput.value = currentFeed.screen || 'HOME_FEED';
  if (screenVersionInput) screenVersionInput.value = currentFeed.version || '1.0';

  currentFeed.sections.forEach((section, sIndex) => {
    const card = document.createElement('div');
    card.className = 'section-card';
    card.dataset.sectionIndex = sIndex;

    const badgeClass = section.type === 'CAROUSEL' ? 'badge-carousel' :
      section.type === 'HORIZONTAL_LIST' ? 'badge-horizontal' : 'badge-grid';

    // Card Header
    card.innerHTML = `
      <div class="section-card-header">
        <div class="section-card-title">
          <span class="section-type-badge ${badgeClass}">${section.type}</span>
          <span class="section-id-tag">${section.id}</span>
        </div>
        <div class="section-card-actions">
          <button class="btn-icon" onclick="moveSection(${sIndex}, -1)" title="Move Up" ${sIndex === 0 ? 'disabled style="opacity:0.3"' : ''}>⬆️</button>
          <button class="btn-icon" onclick="moveSection(${sIndex}, 1)" title="Move Down" ${sIndex === currentFeed.sections.length - 1 ? 'disabled style="opacity:0.3"' : ''}>⬇️</button>
          <button class="btn-icon delete" onclick="deleteSection(${sIndex})" title="Delete Section">🗑️</button>
        </div>
      </div>
      <div class="section-card-body">
        <div class="section-props-grid" id="props-grid-${sIndex}">
          <!-- Section Props dynamically generated below -->
        </div>

        <div class="section-items-header">
          <span>Items (${section.items ? section.items.length : 0})</span>
          <button class="btn btn-sm btn-ghost" onclick="addItemToSection(${sIndex})">➕ Add Item</button>
        </div>
        <div class="items-list-container" id="items-list-${sIndex}">
          <!-- Items dynamically generated below -->
        </div>
      </div>
    `;

    container.appendChild(card);

    // Populate Props
    const propsGrid = card.querySelector(`#props-grid-${sIndex}`);
    if (section.type === 'CAROUSEL') {
      propsGrid.innerHTML = `
        <div class="form-group">
          <label>Auto-Scroll Interval (ms)</label>
          <input type="number" class="input-text" data-prop-key="autoScrollInterval" value="${section.props?.autoScrollInterval || 4000}" step="500">
        </div>
      `;
    } else if (section.type === 'HORIZONTAL_LIST') {
      propsGrid.innerHTML = `
        <div class="form-group">
          <label>Section Title</label>
          <input type="text" class="input-text" data-prop-key="title" value="${section.props?.title || '⚡ Flash Sale'}">
        </div>
        <div class="form-group">
          <label>Countdown Timer (sec)</label>
          <input type="number" class="input-text" data-prop-key="countdownRemainingSec" value="${section.props?.countdownRemainingSec || 7200}">
        </div>
      `;
    } else if (section.type === 'GRID_2X2') {
      propsGrid.innerHTML = `
        <div class="form-group">
          <label>Grid Section Title</label>
          <input type="text" class="input-text" data-prop-key="title" value="${section.props?.title || 'สินค้าแนะนำสำหรับคุณ'}">
        </div>
      `;
    }

    // Attach live change listener to props
    propsGrid.querySelectorAll('input').forEach(inp => {
      inp.addEventListener('input', () => {
        readFromVisualBuilder();
        renderMobilePreview();
        syncJsonEditor();
      });
    });

    // Populate Items
    const itemsList = card.querySelector(`#items-list-${sIndex}`);
    (section.items || []).forEach((item, iIndex) => {
      const itemRow = document.createElement('div');
      itemRow.className = 'item-row-card';
      itemRow.dataset.itemIndex = iIndex;

      const imgUrl = item.imageUrl || item.props?.thumbnailUrl || 'https://picsum.photos/200/200';

      if (section.type === 'CAROUSEL') {
        itemRow.innerHTML = `
          <img src="${imgUrl}" class="item-thumb-preview" alt="Slide">
          <div class="item-details-grid">
            <div class="form-group item-fields-full">
              <label>🖼️ Banner Image URL</label>
              <input type="text" class="input-text" data-item-prop="imageUrl" value="${item.imageUrl || ''}" placeholder="https://picsum.photos/...">
            </div>
            <div class="form-group item-fields-full">
              <label>🔗 Navigation Target Deep Link</label>
              <input type="text" class="input-text" data-item-prop="target" value="${item.action?.payload?.target || ''}" placeholder="flexifeed://campaign/...">
            </div>
          </div>
          <div class="item-card-actions">
            <button class="btn-icon" onclick="moveItem(${sIndex}, ${iIndex}, -1)" title="เลื่อนขึ้น" ${iIndex === 0 ? 'disabled' : ''}>⬆️</button>
            <button class="btn-icon" onclick="moveItem(${sIndex}, ${iIndex}, 1)" title="เลื่อนลง" ${iIndex === (section.items.length - 1) ? 'disabled' : ''}>⬇️</button>
            <button class="btn-icon delete" onclick="deleteItemFromSection(${sIndex}, ${iIndex})" title="ลบ Item นี้">🗑️</button>
          </div>
        `;
      } else if (section.type === 'HORIZONTAL_LIST') {
        itemRow.innerHTML = `
          <img src="${imgUrl}" class="item-thumb-preview" alt="Product">
          <div class="item-details-grid">
            <div class="form-group item-fields-full">
              <label>🏷️ Product Name</label>
              <input type="text" class="input-text" data-item-prop="name" value="${item.props?.name || ''}" placeholder="ชื่อสินค้า">
            </div>
            <div class="item-fields-row">
              <div class="form-group">
                <label>💰 Price</label>
                <input type="text" class="input-text" data-item-prop="price" value="${item.props?.price || ''}" placeholder="฿890">
              </div>
              <div class="form-group">
                <label>🏷️ Original Price</label>
                <input type="text" class="input-text" data-item-prop="originalPrice" value="${item.props?.originalPrice || ''}" placeholder="฿1,590">
              </div>
              <div class="form-group" style="flex: 2;">
                <label>🖼️ Thumbnail URL</label>
                <input type="text" class="input-text" data-item-prop="thumbnailUrl" value="${item.props?.thumbnailUrl || ''}" placeholder="https://...">
              </div>
            </div>
          </div>
          <div class="item-card-actions">
            <button class="btn-icon" onclick="moveItem(${sIndex}, ${iIndex}, -1)" title="เลื่อนขึ้น" ${iIndex === 0 ? 'disabled' : ''}>⬆️</button>
            <button class="btn-icon" onclick="moveItem(${sIndex}, ${iIndex}, 1)" title="เลื่อนลง" ${iIndex === (section.items.length - 1) ? 'disabled' : ''}>⬇️</button>
            <button class="btn-icon delete" onclick="deleteItemFromSection(${sIndex}, ${iIndex})" title="ลบ Item นี้">🗑️</button>
          </div>
        `;
      } else if (section.type === 'GRID_2X2') {
        itemRow.innerHTML = `
          <img src="${imgUrl}" class="item-thumb-preview" alt="Product">
          <div class="item-details-grid">
            <div class="form-group item-fields-full">
              <label>🏷️ Product Name</label>
              <input type="text" class="input-text" data-item-prop="name" value="${item.props?.name || ''}" placeholder="ชื่อสินค้า">
            </div>
            <div class="item-fields-row">
              <div class="form-group">
                <label>💰 Price</label>
                <input type="text" class="input-text" data-item-prop="price" value="${item.props?.price || ''}" placeholder="฿1,490">
              </div>
              <div class="form-group" style="flex: 2;">
                <label>🖼️ Thumbnail URL</label>
                <input type="text" class="input-text" data-item-prop="thumbnailUrl" value="${item.props?.thumbnailUrl || ''}" placeholder="https://...">
              </div>
            </div>
          </div>
          <div class="item-card-actions">
            <button class="btn-icon" onclick="moveItem(${sIndex}, ${iIndex}, -1)" title="เลื่อนขึ้น" ${iIndex === 0 ? 'disabled' : ''}>⬆️</button>
            <button class="btn-icon" onclick="moveItem(${sIndex}, ${iIndex}, 1)" title="เลื่อนลง" ${iIndex === (section.items.length - 1) ? 'disabled' : ''}>⬇️</button>
            <button class="btn-icon delete" onclick="deleteItemFromSection(${sIndex}, ${iIndex})" title="ลบ Item นี้">🗑️</button>
          </div>
        `;
      }

      itemRow.querySelectorAll('input').forEach(inp => {
        inp.addEventListener('input', () => {
          readFromVisualBuilder();
          renderMobilePreview();
          syncJsonEditor();
        });
      });

      itemsList.appendChild(itemRow);
    });
  });
}

// Section & Item Reordering Actions
window.moveSection = function(index, direction) {
  readFromVisualBuilder();
  const targetIndex = index + direction;
  if (targetIndex < 0 || targetIndex >= currentFeed.sections.length) return;
  const temp = currentFeed.sections[index];
  currentFeed.sections[index] = currentFeed.sections[targetIndex];
  currentFeed.sections[targetIndex] = temp;
  renderVisualBuilder();
  renderMobilePreview();
  syncJsonEditor();
};

window.moveItem = function(sectionIndex, itemIndex, direction) {
  readFromVisualBuilder();
  const section = currentFeed.sections[sectionIndex];
  if (!section || !section.items) return;
  const targetIndex = itemIndex + direction;
  if (targetIndex < 0 || targetIndex >= section.items.length) return;

  const temp = section.items[itemIndex];
  section.items[itemIndex] = section.items[targetIndex];
  section.items[targetIndex] = temp;

  renderVisualBuilder();
  renderMobilePreview();
  syncJsonEditor();
  showToast('↕️ สลับลำดับ Item เรียบร้อย');
};

window.deleteSection = function(index) {
  readFromVisualBuilder();
  currentFeed.sections.splice(index, 1);
  renderVisualBuilder();
  renderMobilePreview();
  syncJsonEditor();
};

window.addItemToSection = function(sectionIndex) {
  readFromVisualBuilder();
  const sec = currentFeed.sections[sectionIndex];
  if (!sec) return;
  if (!sec.items) sec.items = [];

  const randId = Date.now();
  if (sec.type === 'CAROUSEL') {
    sec.items.push({
      id: `banner_${randId}`,
      imageUrl: `https://picsum.photos/id/${Math.floor(Math.random() * 50) + 100}/800/400`,
      action: { type: 'NAVIGATE', payload: { target: `flexifeed://campaign/new-${sec.items.length + 1}` } }
    });
  } else if (sec.type === 'HORIZONTAL_LIST') {
    sec.items.push({
      id: `prod_${randId}`,
      type: 'PRODUCT_CARD_COMPACT',
      props: {
        name: `สินค้า Flash Sale #${sec.items.length + 1}`,
        price: '฿599',
        originalPrice: '฿1,200',
        thumbnailUrl: `https://picsum.photos/id/${Math.floor(Math.random() * 50) + 200}/200/200`
      },
      action: { type: 'NAVIGATE', payload: { target: `flexifeed://product/${randId}` } }
    });
  } else if (sec.type === 'GRID_2X2') {
    sec.items.push({
      id: `prod_grid_${randId}`,
      type: 'PRODUCT_CARD_FULL',
      props: {
        name: `สินค้าแนะนำใหม่ #${sec.items.length + 1}`,
        price: '฿1,890',
        rating: 4.9,
        thumbnailUrl: `https://picsum.photos/id/${Math.floor(Math.random() * 50) + 300}/300/300`
      },
      action: { type: 'ADD_TO_CART', payload: { productId: `${randId}`, quantity: 1 } }
    });
  }
  renderVisualBuilder();
  renderMobilePreview();
  syncJsonEditor();
};

window.deleteItemFromSection = function(sectionIndex, itemIndex) {
  readFromVisualBuilder();
  const sec = currentFeed.sections[sectionIndex];
  if (sec && sec.items) {
    sec.items.splice(itemIndex, 1);
    renderVisualBuilder();
    renderMobilePreview();
    syncJsonEditor();
  }
};

function addNewSection(type) {
  readFromVisualBuilder();
  const randId = Date.now();
  if (type === 'CAROUSEL') {
    currentFeed.sections.push({
      id: `sec_carousel_${randId}`,
      type: 'CAROUSEL',
      props: { autoScrollInterval: 4000 },
      items: [
        {
          id: `banner_${randId}`,
          imageUrl: 'https://picsum.photos/id/1018/800/400',
          action: { type: 'NAVIGATE', payload: { target: 'flexifeed://campaign/new-banner' } }
        }
      ]
    });
  } else if (type === 'HORIZONTAL_LIST') {
    currentFeed.sections.push({
      id: `sec_flash_${randId}`,
      type: 'HORIZONTAL_LIST',
      props: { title: '⚡ Flash Sale พิเศษ', countdownRemainingSec: 3600 },
      items: [
        {
          id: `prod_${randId}`,
          type: 'PRODUCT_CARD_COMPACT',
          props: {
            name: 'สินค้า Flash Sale ใหม่',
            price: '฿390',
            originalPrice: '฿890',
            thumbnailUrl: 'https://picsum.photos/id/20/200/200'
          },
          action: { type: 'NAVIGATE', payload: { target: `flexifeed://product/${randId}` } }
        }
      ]
    });
  } else if (type === 'GRID_2X2') {
    currentFeed.sections.push({
      id: `sec_grid_${randId}`,
      type: 'GRID_2X2',
      props: { title: 'สินค้าแนะนำยอดนิยม' },
      items: [
        {
          id: `prod_g_${randId}`,
          type: 'PRODUCT_CARD_FULL',
          props: {
            name: 'สินค้าแนะนำพิเศษ',
            price: '฿1,490',
            rating: 5.0,
            thumbnailUrl: 'https://picsum.photos/id/40/300/300'
          },
          action: { type: 'ADD_TO_CART', payload: { productId: `${randId}`, quantity: 1 } }
        }
      ]
    });
  }
  renderVisualBuilder();
  renderMobilePreview();
  syncJsonEditor();
}

// Render Real-Time Mobile Emulator Preview
function renderMobilePreview() {
  const scrollContainer = document.getElementById('mock-feed-scrollable');
  if (!scrollContainer) return;
  scrollContainer.innerHTML = '';

  const bannerText = document.getElementById('mock-banner-text');
  if (bannerText) {
    bannerText.textContent = `🟢 [DEV] Live SDUI Server • ${currentFeed.screen || 'HOME_FEED'} v${currentFeed.version || '1.0'}`;
  }

  applyThemeToPreview(currentFeed.theme || {});

  currentFeed.sections.forEach(section => {
    if (section.type === 'CAROUSEL') {
      const carouselWrap = document.createElement('div');
      carouselWrap.className = 'mock-carousel';
      const firstSlide = (section.items && section.items[0]) ? section.items[0].imageUrl : 'https://picsum.photos/800/400';
      const dotCount = Math.min((section.items ? section.items.length : 1), 5);
      let dotsHtml = '';
      for (let i = 0; i < dotCount; i++) {
        dotsHtml += `<span class="mock-dot ${i === 0 ? 'active' : ''}"></span>`;
      }
      carouselWrap.innerHTML = `
        <div class="mock-carousel-slide">
          <img src="${firstSlide}" alt="Banner">
        </div>
        <div class="mock-carousel-dots">
          ${dotsHtml}
        </div>
      `;
      scrollContainer.appendChild(carouselWrap);
    } else if (section.type === 'HORIZONTAL_LIST') {
      const flashWrap = document.createElement('div');
      flashWrap.className = 'mock-flash-sale';

      const secTitle = section.props?.title || '⚡ Flash Sale';
      const remainingSec = section.props?.countdownRemainingSec || 7200;
      const hours = String(Math.floor(remainingSec / 3600)).padStart(2, '0');
      const mins = String(Math.floor((remainingSec % 3600) / 60)).padStart(2, '0');
      const secs = String(remainingSec % 60).padStart(2, '0');

      let productsHtml = '';
      (section.items || []).forEach(item => {
        const thumb = item.props?.thumbnailUrl || 'https://picsum.photos/200/200';
        const name = item.props?.name || 'สินค้าโปรโมชั่น';
        const price = item.props?.price || '฿0';
        const origPrice = item.props?.originalPrice || '';
        productsHtml += `
          <div class="product-compact-card">
            <div class="compact-thumb-wrap">
              <span class="sale-pill">SALE</span>
              <img src="${thumb}" alt="${name}">
            </div>
            <div class="compact-title">${name}</div>
            <div class="compact-price-row">
              <span class="price-active">${price}</span>
              ${origPrice ? `<span class="price-strike">${origPrice}</span>` : ''}
            </div>
          </div>
        `;
      });

      flashWrap.innerHTML = `
        <div class="flash-header">
          <div class="flash-title">${secTitle}</div>
          <div class="flash-countdown">⏱️ ${hours}:${mins}:${secs}</div>
        </div>
        <div class="flash-scroll-row">
          ${productsHtml}
        </div>
      `;
      scrollContainer.appendChild(flashWrap);
    } else if (section.type === 'GRID_2X2') {
      const gridWrap = document.createElement('div');
      gridWrap.className = 'mock-grid-section';
      const secTitle = section.props?.title || 'สินค้าแนะนำ';

      let itemsHtml = '';
      (section.items || []).forEach(item => {
        const thumb = item.props?.thumbnailUrl || 'https://picsum.photos/300/300';
        const name = item.props?.name || 'สินค้า';
        const price = item.props?.price || '฿0';
        const rating = item.props?.rating || 4.8;
        itemsHtml += `
          <div class="product-full-card">
            <div class="full-thumb-wrap">
              <img src="${thumb}" alt="${name}">
            </div>
            <div class="full-title">${name}</div>
            <div class="full-rating">★ ${rating}</div>
            <div class="full-footer-row">
              <span class="full-price">${price}</span>
              <button class="btn-add-cart-mock" title="Add to Cart">＋</button>
            </div>
          </div>
        `;
      });

      gridWrap.innerHTML = `
        <div class="grid-section-title">${secTitle}</div>
        <div class="mock-grid-container">
          ${itemsHtml}
        </div>
      `;
      scrollContainer.appendChild(gridWrap);
    }
  });
}

// Dynamic Phone Auto-Fit & Zoom Calculation
function updatePhoneZoom() {
  const container = document.getElementById('phone-viewport-container');
  const frame = document.getElementById('phone-frame');
  const wrapper = document.getElementById('phone-wrapper');
  if (!container || !frame || !wrapper) return;

  const isSmallScreen = window.innerWidth < 1024;
  const availH = container.clientHeight - 24;
  const availW = container.clientWidth - 24;

  if (currentZoom === 'fit' || isSmallScreen) {
    if (availH > 40 && availW > 40) {
      const scaleH = availH / frame.offsetHeight;
      const scaleW = availW / frame.offsetWidth;
      const fitScale = Math.min(1.0, Math.max(0.3, Math.min(scaleH, scaleW)));
      wrapper.style.transform = `scale(${fitScale})`;
    }
  } else {
    wrapper.style.transform = `scale(${parseFloat(currentZoom)})`;
  }
}
window.addEventListener('resize', updatePhoneZoom);

// Wire Up Everything on DOM Ready
document.addEventListener('DOMContentLoaded', () => {
  // Mobile / Tablet Workspace View Switcher (Editor vs Live App)
  const btnShowEditor = document.getElementById('btn-show-editor');
  const btnShowPreview = document.getElementById('btn-show-preview');
  const studioWorkspace = document.getElementById('studio-workspace');

  function setMobileWorkspaceView(view) {
    if (!studioWorkspace) return;
    if (view === 'preview') {
      studioWorkspace.classList.remove('view-editor');
      studioWorkspace.classList.add('view-preview');
      btnShowEditor?.classList.remove('active');
      btnShowPreview?.classList.add('active');
      setTimeout(() => {
        updatePhoneZoom();
      }, 50);
    } else {
      studioWorkspace.classList.remove('view-preview');
      studioWorkspace.classList.add('view-editor');
      btnShowEditor?.classList.add('active');
      btnShowPreview?.classList.remove('active');
    }
  }

  btnShowEditor?.addEventListener('click', () => setMobileWorkspaceView('editor'));
  btnShowPreview?.addEventListener('click', () => setMobileWorkspaceView('preview'));

  // 1. Studio Theme Switcher
  const themeSelect = document.getElementById('select-studio-theme');
  const savedTheme = localStorage.getItem('flexifeed_studio_theme') || 'neon';
  if (themeSelect) {
    themeSelect.value = savedTheme;
    document.documentElement.setAttribute('data-theme', savedTheme);
    themeSelect.addEventListener('change', (e) => {
      const selected = e.target.value;
      document.documentElement.setAttribute('data-theme', selected);
      localStorage.setItem('flexifeed_studio_theme', selected);
    });
  }

  // 2. Draggable Split Resizer (Exact 60 / 40 Split)
  const resizer = document.getElementById('panel-resizer');
  const configPanel = document.getElementById('config-panel');
  const savedWidth = localStorage.getItem('flexifeed_panel_width_60_40');

  function get60PercentWidth() {
    return Math.round(window.innerWidth * 0.60);
  }

  function applyPanelWidth() {
    if (!configPanel) return;
    if (window.innerWidth >= 1024) {
      if (savedWidth) {
        configPanel.style.width = `${savedWidth}px`;
      } else {
        configPanel.style.width = `${get60PercentWidth()}px`;
      }
    } else {
      configPanel.style.width = '';
    }
  }

  applyPanelWidth();

  // Keep 60/40 responsive on window resize
  window.addEventListener('resize', () => {
    applyPanelWidth();
    updatePhoneZoom();
  });

  if (resizer && configPanel) {
    let isDragging = false;
    resizer.addEventListener('mousedown', (e) => {
      isDragging = true;
      resizer.classList.add('dragging');
      document.body.style.userSelect = 'none';
      document.body.style.cursor = 'col-resize';
    });

    document.addEventListener('mousemove', (e) => {
      if (!isDragging) return;
      const minW = Math.max(500, Math.round(window.innerWidth * 0.35));
      const maxW = Math.min(window.innerWidth - 360, Math.round(window.innerWidth * 0.80));
      const newWidth = Math.min(Math.max(e.clientX, minW), maxW);
      configPanel.style.width = `${newWidth}px`;
      updatePhoneZoom();
    });

    document.addEventListener('mouseup', () => {
      if (isDragging) {
        isDragging = false;
        resizer.classList.remove('dragging');
        document.body.style.userSelect = '';
        document.body.style.cursor = '';
        localStorage.setItem('flexifeed_panel_width_60_40', configPanel.offsetWidth);
        updatePhoneZoom();
      }
    });
  }

  // 3. Tabs (Visual / Theme / JSON)
  const tabVisual = document.getElementById('tab-btn-visual');
  const tabTheme = document.getElementById('tab-btn-theme');
  const tabJson = document.getElementById('tab-btn-json');
  const panelVisual = document.getElementById('panel-visual');
  const panelTheme = document.getElementById('panel-theme');
  const panelJson = document.getElementById('panel-json');

  function activateTab(activeTabBtn, activePanel) {
    [tabVisual, tabTheme, tabJson].forEach(b => b.classList.remove('active'));
    [panelVisual, panelTheme, panelJson].forEach(p => p.classList.remove('active'));
    activeTabBtn.classList.add('active');
    activePanel.classList.add('active');
  }

  tabVisual.addEventListener('click', () => {
    activateTab(tabVisual, panelVisual);
    readFromJsonEditor();
  });

  tabTheme.addEventListener('click', () => {
    activateTab(tabTheme, panelTheme);
    syncThemeInputs();
  });

  tabJson.addEventListener('click', () => {
    activateTab(tabJson, panelJson);
    readFromVisualBuilder();
    readFromThemeBuilder();
    syncJsonEditor();
  });

  // 4. Device Sizing & Zoom Controls
  const deviceSelect = document.getElementById('select-device-size');
  const phoneFrame = document.getElementById('phone-frame');
  if (deviceSelect && phoneFrame) {
    deviceSelect.addEventListener('change', (e) => {
      phoneFrame.className = `phone-frame device-${e.target.value}`;
      updatePhoneZoom();
    });
  }

  const zoomBtns = document.querySelectorAll('.btn-zoom');
  zoomBtns.forEach(btn => {
    btn.addEventListener('click', () => {
      zoomBtns.forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      currentZoom = btn.dataset.zoom;
      updatePhoneZoom();
    });
  });

  // 5. App Dynamic Theme Color Pickers & Palette Swatches
  const inPrimary = document.getElementById('input-theme-primary');
  const txtPrimary = document.getElementById('text-theme-primary');
  const inAccent = document.getElementById('input-theme-accent');
  const txtAccent = document.getElementById('text-theme-accent');
  const selMode = document.getElementById('select-theme-mode');

  function onThemeColorChanged() {
    readFromThemeBuilder();
    renderMobilePreview();
    syncJsonEditor();
  }

  if (inPrimary && txtPrimary) {
    inPrimary.addEventListener('input', (e) => {
      txtPrimary.value = e.target.value;
      onThemeColorChanged();
    });
    txtPrimary.addEventListener('input', (e) => {
      if (/^#[0-9A-Fa-f]{6}$/.test(e.target.value)) {
        inPrimary.value = e.target.value;
        onThemeColorChanged();
      }
    });
  }

  if (inAccent && txtAccent) {
    inAccent.addEventListener('input', (e) => {
      txtAccent.value = e.target.value;
      onThemeColorChanged();
    });
    txtAccent.addEventListener('input', (e) => {
      if (/^#[0-9A-Fa-f]{6}$/.test(e.target.value)) {
        inAccent.value = e.target.value;
        onThemeColorChanged();
      }
    });
  }

  if (selMode) {
    selMode.addEventListener('change', onThemeColorChanged);
  }

  document.querySelectorAll('.palette-btn').forEach(pBtn => {
    pBtn.addEventListener('click', async () => {
      const primary = pBtn.dataset.primary;
      const accent = pBtn.dataset.accent;
      const mode = pBtn.dataset.mode;
      if (inPrimary) inPrimary.value = primary;
      if (txtPrimary) txtPrimary.value = primary;
      if (inAccent) inAccent.value = accent;
      if (txtAccent) txtAccent.value = accent;
      if (selMode) selMode.value = mode;
      onThemeColorChanged();
      await saveThemeToServer();
    });
  });

  // Dedicated Logo Theme Pickers & Presets
  const inLogoBg = document.getElementById('input-logo-bg');
  const txtLogoBg = document.getElementById('text-logo-bg');
  const inLogoIcon = document.getElementById('input-logo-icon');
  const txtLogoIcon = document.getElementById('text-logo-icon');
  const inLogoSub = document.getElementById('input-logo-sub');
  const txtLogoSub = document.getElementById('text-logo-sub');

  function bindColorPair(colorInput, textInput) {
    if (!colorInput || !textInput) return;
    colorInput.addEventListener('input', (e) => {
      textInput.value = e.target.value;
      onThemeColorChanged();
    });
    textInput.addEventListener('input', (e) => {
      if (/^#[0-9A-Fa-f]{6}$/.test(e.target.value)) {
        colorInput.value = e.target.value;
        onThemeColorChanged();
      }
    });
  }

  bindColorPair(inLogoBg, txtLogoBg);
  bindColorPair(inLogoIcon, txtLogoIcon);
  bindColorPair(inLogoSub, txtLogoSub);

  document.querySelectorAll('.logo-preset-btn').forEach(lBtn => {
    lBtn.addEventListener('click', async () => {
      const bg = lBtn.dataset.bg;
      const icon = lBtn.dataset.icon;
      const sub = lBtn.dataset.sub;
      if (inLogoBg) inLogoBg.value = bg;
      if (txtLogoBg) txtLogoBg.value = bg;
      if (inLogoIcon) inLogoIcon.value = icon;
      if (txtLogoIcon) txtLogoIcon.value = icon;
      if (inLogoSub) inLogoSub.value = sub;
      if (txtLogoSub) txtLogoSub.value = sub;
      onThemeColorChanged();
      await saveThemeToServer();
    });
  });

  // 6. Action Buttons
  const btnSaveTheme = document.getElementById('btn-save-theme');
  if (btnSaveTheme) {
    btnSaveTheme.addEventListener('click', saveThemeToServer);
  }

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
