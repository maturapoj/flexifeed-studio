import { pgTable, varchar, text, jsonb, timestamp } from "drizzle-orm/pg-core";
import { SDUIScreen, SDUISection, SDUITheme } from "../types/sdui.types";

/**
 * SDUI Screens Table
 * Stores SDUI tree hierarchies, detail screens, and campaign pages.
 */
export const screens = pgTable("screens", {
  id: varchar("id", { length: 64 }).primaryKey(),
  version: varchar("version", { length: 32 }).notNull().default("1.0"),
  presetId: varchar("preset_id", { length: 64 }),
  theme: jsonb("theme").$type<SDUITheme>(),
  sections: jsonb("sections").notNull().$type<SDUISection[]>(),
  updatedAt: timestamp("updated_at").defaultNow().notNull(),
});

/**
 * SDUI Presets Table
 * Stores campaign configurations such as Mega Sale and Tech Weekend.
 */
export const presets = pgTable("presets", {
  id: varchar("id", { length: 64 }).primaryKey(),
  name: varchar("name", { length: 128 }).notNull(),
  description: text("description"),
  theme: jsonb("theme").$type<SDUITheme>(),
  screen: jsonb("screen").notNull().$type<SDUIScreen>(),
  updatedAt: timestamp("updated_at").defaultNow().notNull(),
});

/**
 * App Settings & Active Configuration Table
 * Stores active preset pointers, system state, and feature flags.
 */
export const appSettings = pgTable("app_settings", {
  key: varchar("key", { length: 64 }).primaryKey(),
  value: text("value").notNull(),
  updatedAt: timestamp("updated_at").defaultNow().notNull(),
});

export type ScreenRecord = typeof screens.$inferSelect;
export type NewScreenRecord = typeof screens.$inferInsert;
export type PresetRecord = typeof presets.$inferSelect;
export type NewPresetRecord = typeof presets.$inferInsert;
