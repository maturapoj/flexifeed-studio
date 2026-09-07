import http from "http";
import { PORT, CORS_HEADERS } from "./config/constants";
import { initializeStore } from "./data/feedStore";
import { handleApiRoutes } from "./routes/apiRoutes";
import { serveStaticFile } from "./routes/staticRoutes";

// Initialize data store (PostgreSQL via Drizzle or file store fallback)
initializeStore().catch(err => {
  console.error("[SDUI Server] Store initialization warning:", err);
});

export const server = http.createServer(async (req, res) => {
  // CORS Headers for API calls
  for (const [header, value] of Object.entries(CORS_HEADERS)) {
    res.setHeader(header, value);
  }

  if (req.method === "OPTIONS") {
    res.writeHead(204);
    res.end();
    return;
  }

  const host = req.headers.host || `localhost:${PORT}`;
  const urlObj = new URL(req.url || "/", `http://${host}`);

  try {
    const handled = await handleApiRoutes(req, res, urlObj);
    if (!handled) {
      serveStaticFile(req, res, urlObj.pathname);
    }
  } catch (err: unknown) {
    const errorMsg = err instanceof Error ? err.message : String(err);
    console.error("[SDUI Server] Unhandled Request Error:", err);
    if (!res.headersSent) {
      res.writeHead(500, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: "Internal Server Error", message: errorMsg }));
    }
  }
});

// Start listening if executed directly
if (require.main === module || process.env.NODE_ENV !== "test") {
  server.listen(PORT, "0.0.0.0", () => {
    console.log(`=======================================================`);
    console.log(`🚀 FlexiFeed SDUI Server (TypeScript) running on port ${PORT}`);
    console.log(`👉 Web Backoffice:   http://localhost:${PORT}`);
    console.log(`👉 SDUI Endpoint:    http://localhost:${PORT}/api/v1/home-feed`);
    console.log(`📱 Android Emulator:  http://10.0.2.2:${PORT}/api/v1/home-feed`);
    console.log(`=======================================================`);
  });
}

export default server;
