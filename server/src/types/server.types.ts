import { IncomingMessage, ServerResponse } from 'http';
import { SDUIScreen, SDUITheme } from './sdui.types';

export type HttpHandler = (req: IncomingMessage, res: ServerResponse, url: URL) => Promise<void> | void;

export type SSEEventType =
  | 'FEED_UPDATED'
  | 'THEME_UPDATED'
  | 'PRESET_SWITCHED'
  | 'PING';

export interface SSEBroadcastMessage {
  event: SSEEventType;
  data: Record<string, unknown>;
}

export interface PresetData {
  id: string;
  name: string;
  description: string;
  theme: SDUITheme;
  feed: SDUIScreen;
}
