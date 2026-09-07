import { drizzle } from "drizzle-orm/node-postgres";
import { Pool } from "pg";
import dotenv from "dotenv";
import * as schema from "./schema";

dotenv.config();

const connectionString = process.env.DATABASE_URL;

let pool: Pool | null = null;
let dbInstance: ReturnType<typeof drizzle<typeof schema>> | null = null;
let isConnected = false;

if (connectionString) {
  const isSsl = connectionString.includes("neon.tech") || connectionString.includes("sslmode=require");
  pool = new Pool({
    connectionString,
    ssl: isSsl ? { rejectUnauthorized: false } : false,
    max: 10,
    idleTimeoutMillis: 30000,
    connectionTimeoutMillis: 5000,
  });

  // Prevent unhandled errors from terminating Node process
  pool.on("error", (err) => {
    console.error("[Postgres Pool] Unexpected error on idle client:", err.message);
  });

  dbInstance = drizzle(pool, { schema });
}

/**
 * Returns whether DATABASE_URL is configured in environment
 */
export function isDatabaseConfigured(): boolean {
  return Boolean(connectionString && dbInstance);
}

/**
 * Verifies live connection to PostgreSQL database
 */
export async function checkDatabaseConnection(): Promise<boolean> {
  if (!pool || !dbInstance) return false;
  try {
    const client = await pool.connect();
    try {
      await client.query("SELECT 1");
      isConnected = true;
      return true;
    } finally {
      client.release();
    }
  } catch (err: unknown) {
    const msg = err instanceof Error ? err.message : String(err);
    console.warn("[Postgres] Could not connect to database:", msg);
    isConnected = false;
    return false;
  }
}

/**
 * Returns whether the database connection has been established
 */
export function isDbConnected(): boolean {
  return isConnected;
}

export const db = dbInstance;
export { schema, pool };
