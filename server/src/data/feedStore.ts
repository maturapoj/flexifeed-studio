import fs from 'fs';
import path from 'path';
import { DATA_DIR, PRESETS_DIR, FEED_FILE } from '../config/constants';
import { SDUIScreen } from '../types/sdui.types';
import { DEFAULT_PRESETS } from './defaultPresets';

/**
 * Ensures data/ and data/presets/ directories exist and seeds initial presets if missing.
 */
export function ensureDirectories(): void {
  if (!fs.existsSync(DATA_DIR)) {
    fs.mkdirSync(DATA_DIR, { recursive: true });
  }
  if (!fs.existsSync(PRESETS_DIR)) {
    fs.mkdirSync(PRESETS_DIR, { recursive: true });
  }
  seedPresets();
}

/**
 * Seeds default preset JSON files to data/presets/ if they do not exist.
 */
export function seedPresets(): void {
  for (const [id, presetObj] of Object.entries(DEFAULT_PRESETS)) {
    const pFile = path.join(PRESETS_DIR, `${id}.json`);
    if (!fs.existsSync(pFile)) {
      fs.writeFileSync(pFile, JSON.stringify({ ...presetObj, presetId: id }, null, 2), 'utf8');
    }
  }
}

/**
 * Reads preset JSON file with fallback to in-memory DEFAULT_PRESETS.
 */
export function getPreset(presetId: string): SDUIScreen & { presetId: string } {
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
  const defaultObj = JSON.parse(JSON.stringify(DEFAULT_PRESETS[pId] || DEFAULT_PRESETS['mega-sale']));
  defaultObj.presetId = pId;
  return defaultObj;
}

/**
 * Persists preset data to data/presets/<presetId>.json.
 */
export function savePreset(presetId: string, data: SDUIScreen): void {
  const pId = presetId === 'tech-weekend' ? 'tech-weekend' : 'mega-sale';
  const presetFile = path.join(PRESETS_DIR, `${pId}.json`);
  const toSave = { ...data, presetId: pId };
  fs.writeFileSync(presetFile, JSON.stringify(toSave, null, 2), 'utf8');
}

/**
 * Reads the active feed from data/feed.json. Defaults to mega-sale preset if missing.
 */
export function readCurrentFeed(): SDUIScreen & { presetId: string } {
  if (fs.existsSync(FEED_FILE)) {
    try {
      const data = fs.readFileSync(FEED_FILE, 'utf8');
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

/**
 * Writes updated feed data to data/feed.json.
 */
export function writeCurrentFeed(feedData: SDUIScreen & { presetId?: string }): void {
  fs.writeFileSync(FEED_FILE, JSON.stringify(feedData, null, 2), 'utf8');
}

/**
 * Resets all presets and current feed to default baseline.
 */
export function resetAllPresets(): void {
  const defaultMega = JSON.parse(JSON.stringify(DEFAULT_PRESETS['mega-sale']));
  defaultMega.presetId = 'mega-sale';
  savePreset('mega-sale', DEFAULT_PRESETS['mega-sale']);
  savePreset('tech-weekend', DEFAULT_PRESETS['tech-weekend']);
  writeCurrentFeed(defaultMega);
}
