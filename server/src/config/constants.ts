import path from 'path';

export const PORT = parseInt(process.env.PORT || '8080', 10);

export const BASE_DIR = path.resolve(__dirname, '..', '..');
export const DATA_DIR = path.join(BASE_DIR, 'data');
export const PRESETS_DIR = path.join(DATA_DIR, 'presets');
export const FEED_FILE = path.join(DATA_DIR, 'feed.json');
export const PUBLIC_DIR = path.join(BASE_DIR, 'public');

export const MIME_TYPES: Record<string, string> = {
  '.html': 'text/html; charset=utf-8',
  '.css': 'text/css; charset=utf-8',
  '.js': 'application/javascript; charset=utf-8',
  '.json': 'application/json; charset=utf-8',
  '.png': 'image/png',
  '.jpg': 'image/jpeg',
  '.jpeg': 'image/jpeg',
  '.gif': 'image/gif',
  '.svg': 'image/svg+xml',
  '.ico': 'image/x-icon',
  '.webp': 'image/webp',
};

export const CORS_HEADERS: Record<string, string> = {
  'Access-Control-Allow-Origin': '*',
  'Access-Control-Allow-Methods': 'GET, POST, PUT, DELETE, OPTIONS',
  'Access-Control-Allow-Headers': 'Content-Type, Authorization',
};
