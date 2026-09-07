/**
 * FlexiFeed SDUI Server
 * Modular TypeScript Server Entrypoint
 *
 * For development: npm start (via tsx src/server.ts)
 * For production:  npm run build && npm run serve (via dist/server.js)
 */

const fs = require('fs');
const path = require('path');

const distServer = path.join(__dirname, 'dist', 'server.js');
if (!fs.existsSync(distServer)) {
  console.log('⚡ Building TypeScript server...');
  require('child_process').execSync('npm run build', { cwd: __dirname, stdio: 'inherit' });
}

require('./dist/server.js');
