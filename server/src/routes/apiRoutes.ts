import { IncomingMessage, ServerResponse } from "http";
import { readCurrentFeed, writeCurrentFeed, getPreset, savePreset, resetAllPresets } from "../data/feedStore";
import { DEFAULT_SCREENS } from "../data/defaultScreens";
import { sseService } from "../services/sseService";

function readRequestBody(req: IncomingMessage): Promise<string> {
  return new Promise((resolve, reject) => {
    let body = "";
    req.on("data", chunk => { body += chunk; });
    req.on("end", () => resolve(body));
    req.on("error", err => reject(err));
  });
}

/**
 * Handles all FlexiFeed /api/v1/* routes.
 * Returns true if the route was matched and handled, false otherwise.
 */
export async function handleApiRoutes(req: IncomingMessage, res: ServerResponse, url: URL): Promise<boolean> {
  const pathname = url.pathname;
  const method = req.method;

  // SSE: GET /api/v1/feed-stream
  if (pathname === "/api/v1/feed-stream" && method === "GET") {
    res.writeHead(200, {
      "Content-Type": "text/event-stream",
      "Cache-Control": "no-cache, no-transform",
      "Connection": "keep-alive",
      "Access-Control-Allow-Origin": "*"
    });
    if (typeof (res as any).flushHeaders === "function") {
      (res as any).flushHeaders();
    }
    res.write(": keepalive\n\n");
    res.write(`event: CONNECTED\ndata: ${JSON.stringify({ message: "Live Hot-Reload SSE Connected", timestamp: Date.now() })}\n\n`);

    sseService.addClient(res);

    req.on("close", () => {
      sseService.removeClient(res);
    });
    return true;
  }

  // GET /api/v1/home-feed
  if (pathname === "/api/v1/home-feed" && method === "GET") {
    const campaign = url.searchParams.get("campaign");
    let feed;
    if (campaign) {
      const lower = campaign.toLowerCase();
      if (lower.includes("tech")) {
        feed = await getPreset("tech-weekend");
      } else if (lower.includes("mega") || lower.includes("default")) {
        feed = await getPreset("mega-sale");
      } else {
        feed = readCurrentFeed();
      }
    } else {
      feed = readCurrentFeed();
    }
    res.writeHead(200, {
      "Content-Type": "application/json; charset=utf-8",
      "Cache-Control": "no-cache, no-store, must-revalidate"
    });
    res.end(JSON.stringify(feed, null, 2));
    return true;
  }

  // GET /api/v1/theme
  if (pathname === "/api/v1/theme" && method === "GET") {
    const feed = readCurrentFeed();
    res.writeHead(200, { "Content-Type": "application/json; charset=utf-8" });
    res.end(JSON.stringify(feed.theme || { primaryColor: "#4F46E5", accentColor: "#FF3366", mode: "LIGHT" }));
    return true;
  }

  // GET /api/v1/screen/:screenId
  if (pathname.startsWith("/api/v1/screen/") && method === "GET") {
    const screenId = pathname.replace("/api/v1/screen/", "");
    if (screenId === "home") {
      res.writeHead(200, { "Content-Type": "application/json; charset=utf-8" });
      res.end(JSON.stringify(readCurrentFeed(), null, 2));
    } else if (DEFAULT_SCREENS[screenId]) {
      res.writeHead(200, { "Content-Type": "application/json; charset=utf-8" });
      res.end(JSON.stringify(DEFAULT_SCREENS[screenId], null, 2));
    } else {
      res.writeHead(404, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: `Screen  not found` }));
    }
    return true;
  }

  // POST /api/v1/theme
  if (pathname === "/api/v1/theme" && method === "POST") {
    try {
      const body = await readRequestBody(req);
      const themeData = JSON.parse(body);
      const currentFeed = readCurrentFeed();
      const activePreset = themeData.presetId || currentFeed.presetId || (currentFeed.version === "1.2" ? "tech-weekend" : "mega-sale");

      currentFeed.presetId = activePreset;
      currentFeed.theme = {
        primaryColor: themeData.primaryColor || currentFeed.theme?.primaryColor || "#4F46E5",
        accentColor: themeData.accentColor || currentFeed.theme?.accentColor || "#FF3366",
        mode: themeData.mode || currentFeed.theme?.mode || "LIGHT"
      };
      await writeCurrentFeed(currentFeed);

      // Also persist theme permanently to this preset
      const presetData = await getPreset(activePreset);
      presetData.theme = currentFeed.theme;
      presetData.presetId = activePreset;
      await savePreset(activePreset, presetData);

      console.log(`[SDUI Server] Updated theme for preset : ${JSON.stringify(currentFeed.theme)}`);
      sseService.broadcastFeedUpdate("THEME_UPDATED", activePreset);

      res.writeHead(200, { "Content-Type": "application/json" });
      res.end(JSON.stringify({
        success: true,
        message: `Theme saved for preset: ${activePreset}!`,
        theme: currentFeed.theme,
        presetId: activePreset
      }));
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : String(err);
      res.writeHead(400, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: "Invalid JSON payload: " + errorMsg }));
    }
    return true;
  }

  // POST /api/v1/home-feed
  if (pathname === "/api/v1/home-feed" && method === "POST") {
    try {
      const body = await readRequestBody(req);
      const parsed = JSON.parse(body);
      if (!parsed.sections || !Array.isArray(parsed.sections)) {
        res.writeHead(400, { "Content-Type": "application/json" });
        res.end(JSON.stringify({ error: "Payload must contain a sections array" }));
        return true;
      }
      const activePreset = parsed.presetId || (parsed.version === "1.2" ? "tech-weekend" : "mega-sale");
      parsed.presetId = activePreset;
      await writeCurrentFeed(parsed);
      await savePreset(activePreset, parsed);

      console.log(`[SDUI Server] Updated feed configuration for preset  (${parsed.sections.length} sections)`);
      sseService.broadcastFeedUpdate("FEED_UPDATED", activePreset);
      res.writeHead(200, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ success: true, message: "Configuration saved and published!", presetId: activePreset }));
    } catch (err: unknown) {
      const errorMsg = err instanceof Error ? err.message : String(err);
      res.writeHead(400, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: "Invalid JSON payload: " + errorMsg }));
    }
    return true;
  }

  // POST /api/v1/reset
  if (pathname === "/api/v1/reset" && method === "POST") {
    await resetAllPresets();
    sseService.broadcastFeedUpdate("FEED_RESET", "mega-sale");
    res.writeHead(200, { "Content-Type": "application/json" });
    res.end(JSON.stringify({ success: true, message: "Reset all presets to default configuration" }));
    return true;
  }

  // POST /api/v1/preset/:id
  if (pathname.startsWith("/api/v1/preset/") && method === "POST") {
    const presetId = pathname.replace("/api/v1/preset/", "");
    if (presetId === "mega-sale" || presetId === "tech-weekend") {
      const presetData = await getPreset(presetId);
      presetData.presetId = presetId;
      await writeCurrentFeed(presetData);
      console.log(`[SDUI Server] Switched to preset  with theme: ${JSON.stringify(presetData.theme)}`);
      sseService.broadcastFeedUpdate("PRESET_SWITCHED", presetId);
      res.writeHead(200, { "Content-Type": "application/json" });
      res.end(JSON.stringify({
        success: true,
        message: `Loaded preset: ${presetId}`,
        presetId,
        theme: presetData.theme
      }));
    } else {
      res.writeHead(404, { "Content-Type": "application/json" });
      res.end(JSON.stringify({ error: `Preset  not found` }));
    }
    return true;
  }

  return false;
}
