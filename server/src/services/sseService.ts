import { ServerResponse } from 'http';

/**
 * SSEService manages active Server-Sent Events connections for Live Hot-Reload.
 */
export class SSEService {
  private clients: Set<ServerResponse> = new Set();
  private heartbeatTimer: NodeJS.Timeout | null = null;

  constructor() {
    this.startHeartbeat();
  }

  public addClient(res: ServerResponse): void {
    this.clients.add(res);
    console.log(`[SDUI Server] 🟢 SSE client connected. Active connections: ${this.clients.size}`);
  }

  public removeClient(res: ServerResponse): void {
    this.clients.delete(res);
    console.log(`[SDUI Server] 🔴 SSE client disconnected. Active connections: ${this.clients.size}`);
  }

  public broadcastFeedUpdate(action?: string, presetId?: string | null): void {
    const eventPayload = {
      action: action || 'FEED_UPDATED',
      presetId: presetId || null,
      timestamp: Date.now()
    };
    const message = `event: FEED_UPDATED\ndata: ${JSON.stringify(eventPayload)}\n\n`;
    console.log(`[SDUI Server] ⚡ SSE Broadcast ${action} (preset: ${presetId}) to ${this.clients.size} client(s)`);

    for (const client of this.clients) {
      try {
        client.write(message);
      } catch (err: unknown) {
        const errorMsg = err instanceof Error ? err.message : String(err);
        console.error('[SDUI Server] SSE write error, removing client:', errorMsg);
        this.clients.delete(client);
      }
    }
  }

  public startHeartbeat(intervalMs: number = 15000): void {
    if (this.heartbeatTimer) return;
    this.heartbeatTimer = setInterval(() => {
      if (this.clients.size > 0) {
        for (const client of this.clients) {
          try {
            client.write(': keepalive\n\n');
          } catch {
            this.clients.delete(client);
          }
        }
      }
    }, intervalMs);
  }

  public stopHeartbeat(): void {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer);
      this.heartbeatTimer = null;
    }
  }

  public get clientCount(): number {
    return this.clients.size;
  }
}

export const sseService = new SSEService();
