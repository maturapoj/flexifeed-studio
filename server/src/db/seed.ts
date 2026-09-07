import { db, schema, checkDatabaseConnection, pool } from "./index";
import { DEFAULT_PRESETS } from "../data/defaultPresets";
import { DEFAULT_SCREENS } from "../data/defaultScreens";
import { readCurrentFeed } from "../data/feedStore";

async function seed() {
  console.log("🌱 Starting SDUI PostgreSQL Seeder...");

  const connected = await checkDatabaseConnection();
  if (!connected || !db) {
    console.error("❌ Database connection failed. Please ensure PostgreSQL is running and DATABASE_URL is set.");
    process.exit(1);
  }

  console.log("✔ Connected to PostgreSQL successfully.");

  // 1. Seed Presets
  console.log("📦 Seeding presets...");
  for (const [presetId, presetObj] of Object.entries(DEFAULT_PRESETS)) {
    await db
      .insert(schema.presets)
      .values({
        id: presetId,
        name: presetId === "tech-weekend" ? "Tech Weekend Campaign" : "Mega Sale 2026",
        description: `Preset configuration for ${presetId}`,
        theme: presetObj.theme,
        screen: presetObj,
      })
      .onConflictDoUpdate({
        target: schema.presets.id,
        set: {
          theme: presetObj.theme,
          screen: presetObj,
          updatedAt: new Date(),
        },
      });
    console.log(`  ✔ Seeded preset: ${presetId}`);
  }

  // 2. Seed Screens
  console.log("📱 Seeding screens...");
  const currentFeed = readCurrentFeed();
  
  // Seed HOME_FEED from current feed
  await db
    .insert(schema.screens)
    .values({
      id: "HOME_FEED",
      version: currentFeed.version || "1.0",
      presetId: currentFeed.presetId || "mega-sale",
      theme: currentFeed.theme,
      sections: currentFeed.sections,
    })
    .onConflictDoUpdate({
      target: schema.screens.id,
      set: {
        version: currentFeed.version || "1.0",
        presetId: currentFeed.presetId || "mega-sale",
        theme: currentFeed.theme,
        sections: currentFeed.sections,
        updatedAt: new Date(),
      },
    });
  console.log("  ✔ Seeded screen: HOME_FEED");

  // Seed default product and campaign screens
  for (const [screenId, screenObj] of Object.entries(DEFAULT_SCREENS)) {
    await db
      .insert(schema.screens)
      .values({
        id: screenId,
        version: screenObj.version,
        presetId: screenObj.presetId || null,
        theme: screenObj.theme,
        sections: screenObj.sections,
      })
      .onConflictDoUpdate({
        target: schema.screens.id,
        set: {
          version: screenObj.version,
          theme: screenObj.theme,
          sections: screenObj.sections,
          updatedAt: new Date(),
        },
      });
    console.log(`  ✔ Seeded screen: ${screenId}`);
  }

  // 3. Seed App Settings
  await db
    .insert(schema.appSettings)
    .values({
      key: "active_preset",
      value: currentFeed.presetId || "mega-sale",
    })
    .onConflictDoUpdate({
      target: schema.appSettings.key,
      set: {
        value: currentFeed.presetId || "mega-sale",
        updatedAt: new Date(),
      },
    });
  console.log("  ✔ Seeded app_settings: active_preset");

  console.log("🎉 All SDUI data seeded successfully into PostgreSQL!");
  if (pool) {
    await pool.end();
  }
}

seed().catch((err) => {
  console.error("Seeding failed:", err);
  process.exit(1);
});
