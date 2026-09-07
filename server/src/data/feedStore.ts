import fs from "fs";
import path from "path";
import { eq } from "drizzle-orm";
import { DATA_DIR, PRESETS_DIR, FEED_FILE } from "../config/constants";
import { SDUIScreen } from "../types/sdui.types";
import { DEFAULT_PRESETS } from "./defaultPresets";
import { db, schema, isDatabaseConfigured, checkDatabaseConnection, isDbConnected } from "../db";

// In-memory active cache for instant synchronous access
let cachedFeed: (SDUIScreen & { presetId: string }) | null = null;

/**
 * Initializes the store, sets up directories, and verifies database connectivity.
 */
export async function initializeStore(): Promise<void> {
  ensureDirectories();

  if (isDatabaseConfigured()) {
    const connected = await checkDatabaseConnection();
    if (connected && db) {
      console.log("[FeedStore] 🗄️ PostgreSQL database connected via Drizzle ORM");
      try {
        await syncFromDatabase();
        return;
      } catch (err: unknown) {
        const msg = err instanceof Error ? err.message : String(err);
        console.warn("[FeedStore] Failed to sync from database, falling back to file:", msg);
      }
    } else {
      console.log("[FeedStore] 📁 Database offline or not reachable. Operating in local file mode.");
    }
  } else {
    console.log("[FeedStore] 📁 Operating in local file mode (DATABASE_URL not set).");
  }

  // Fallback: load from local file
  cachedFeed = readFromFile();
}

/**
 * Synchronizes in-memory cache and local file from PostgreSQL
 */
async function syncFromDatabase(): Promise<void> {
  if (!db) return;
  const homeScreen = await db.query.screens.findFirst({
    where: eq(schema.screens.id, "HOME_FEED"),
  });

  if (homeScreen) {
    cachedFeed = {
      screen: "HOME_FEED",
      version: homeScreen.version,
      presetId: homeScreen.presetId || "mega-sale",
      theme: homeScreen.theme || undefined,
      sections: homeScreen.sections,
    };
    // Sync backup to local file
    writeToFile(cachedFeed);
  } else {
    // Initial bootstrap in DB
    const defaultData = getPresetFromFile("mega-sale");
    await saveToDatabase(defaultData);
    cachedFeed = defaultData;
  }
}

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
      fs.writeFileSync(pFile, JSON.stringify({ ...presetObj, presetId: id }, null, 2), "utf8");
    }
  }
}

/**
 * Reads preset with fallback to in-memory DEFAULT_PRESETS
 */
export async function getPreset(presetId: string): Promise<SDUIScreen & { presetId: string }> {
  const pId = presetId === "tech-weekend" ? "tech-weekend" : "mega-sale";

  if (isDbConnected() && db) {
    try {
      const record = await db.query.presets.findFirst({
        where: eq(schema.presets.id, pId),
      });
      if (record && record.screen) {
        return { ...record.screen, presetId: pId };
      }
    } catch (err) {
      console.warn(`[FeedStore] DB error fetching preset ${pId}, falling back to file:`, err);
    }
  }

  return getPresetFromFile(pId);
}

function getPresetFromFile(presetId: string): SDUIScreen & { presetId: string } {
  const pId = presetId === "tech-weekend" ? "tech-weekend" : "mega-sale";
  const presetFile = path.join(PRESETS_DIR, `${pId}.json`);
  if (fs.existsSync(presetFile)) {
    try {
      const parsed = JSON.parse(fs.readFileSync(presetFile, "utf8"));
      if (parsed && parsed.theme && parsed.sections) {
        parsed.presetId = pId;
        return parsed;
      }
    } catch (e) {
      console.error(`Error reading preset file for ${pId}:`, e);
    }
  }
  const defaultObj = JSON.parse(JSON.stringify(DEFAULT_PRESETS[pId] || DEFAULT_PRESETS["mega-sale"]));
  defaultObj.presetId = pId;
  return defaultObj;
}

/**
 * Persists preset data to Database and File
 */
export async function savePreset(presetId: string, data: SDUIScreen): Promise<void> {
  const pId = presetId === "tech-weekend" ? "tech-weekend" : "mega-sale";
  const toSave = { ...data, presetId: pId };

  // 1. Save to File
  const presetFile = path.join(PRESETS_DIR, `${pId}.json`);
  fs.writeFileSync(presetFile, JSON.stringify(toSave, null, 2), "utf8");

  // 2. Save to Database if connected
  if (isDbConnected() && db) {
    try {
      await db
        .insert(schema.presets)
        .values({
          id: pId,
          name: pId === "tech-weekend" ? "Tech Weekend Campaign" : "Mega Sale 2026",
          theme: toSave.theme,
          screen: toSave,
        })
        .onConflictDoUpdate({
          target: schema.presets.id,
          set: {
            theme: toSave.theme,
            screen: toSave,
            updatedAt: new Date(),
          },
        });
    } catch (err) {
      console.warn(`[FeedStore] Could not persist preset ${pId} to database:`, err);
    }
  }
}

/**
 * Reads active feed (from cache / DB / file fallback)
 */
export function readCurrentFeed(): SDUIScreen & { presetId: string } {
  if (cachedFeed) {
    return cachedFeed;
  }
  cachedFeed = readFromFile();
  return cachedFeed;
}

/**
 * Asynchronously reads active feed from database if connected
 */
export async function readCurrentFeedAsync(): Promise<SDUIScreen & { presetId: string }> {
  if (isDbConnected() && db) {
    try {
      const record = await db.query.screens.findFirst({
        where: eq(schema.screens.id, "HOME_FEED"),
      });
      if (record) {
        cachedFeed = {
          screen: "HOME_FEED",
          version: record.version,
          presetId: record.presetId || "mega-sale",
          theme: record.theme || undefined,
          sections: record.sections,
        };
        return cachedFeed;
      }
    } catch (err) {
      console.warn("[FeedStore] DB read error, using cache/file:", err);
    }
  }
  return readCurrentFeed();
}

/**
 * Writes updated feed data to Cache, File, and Database
 */
export async function writeCurrentFeed(feedData: SDUIScreen & { presetId?: string }): Promise<void> {
  const normalized: SDUIScreen & { presetId: string } = {
    ...feedData,
    presetId: feedData.presetId || "mega-sale",
  };
  cachedFeed = normalized;

  // 1. Write to local file
  writeToFile(normalized);

  // 2. Write to Database
  await saveToDatabase(normalized);
}

function readFromFile(): SDUIScreen & { presetId: string } {
  if (fs.existsSync(FEED_FILE)) {
    try {
      const data = fs.readFileSync(FEED_FILE, "utf8");
      const parsed = JSON.parse(data);
      if (!parsed.presetId) {
        parsed.presetId =
          parsed.version === "1.2" || (parsed.screen && parsed.screen.includes("TECH"))
            ? "tech-weekend"
            : "mega-sale";
      }
      return parsed;
    } catch (e) {
      console.error("Error reading data file, using default preset:", e);
    }
  }
  return getPresetFromFile("mega-sale");
}

function writeToFile(feedData: SDUIScreen & { presetId?: string }): void {
  fs.writeFileSync(FEED_FILE, JSON.stringify(feedData, null, 2), "utf8");
}

async function saveToDatabase(feedData: SDUIScreen & { presetId: string }): Promise<void> {
  if (!isDbConnected() || !db) return;
  try {
    await db
      .insert(schema.screens)
      .values({
        id: "HOME_FEED",
        version: feedData.version || "1.0",
        presetId: feedData.presetId,
        theme: feedData.theme,
        sections: feedData.sections,
      })
      .onConflictDoUpdate({
        target: schema.screens.id,
        set: {
          version: feedData.version || "1.0",
          presetId: feedData.presetId,
          theme: feedData.theme,
          sections: feedData.sections,
          updatedAt: new Date(),
        },
      });

    await db
      .insert(schema.appSettings)
      .values({
        key: "active_preset",
        value: feedData.presetId,
      })
      .onConflictDoUpdate({
        target: schema.appSettings.key,
        set: {
          value: feedData.presetId,
          updatedAt: new Date(),
        },
      });
  } catch (err) {
    console.warn("[FeedStore] Could not persist feed to database:", err);
  }
}

/**
 * Resets all presets and current feed to default baseline.
 */
export async function resetAllPresets(): Promise<void> {
  const defaultMega = JSON.parse(JSON.stringify(DEFAULT_PRESETS["mega-sale"]));
  defaultMega.presetId = "mega-sale";

  await savePreset("mega-sale", DEFAULT_PRESETS["mega-sale"]);
  await savePreset("tech-weekend", DEFAULT_PRESETS["tech-weekend"]);
  await writeCurrentFeed(defaultMega);
}
