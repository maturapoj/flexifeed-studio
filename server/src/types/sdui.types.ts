/**
 * SDUI Domain Types and Contracts matching the FlexiFeed Android Client.
 */

export interface SDUILogoTheme {
  bgColor?: string;
  iconColor?: string;
  titleColor?: string;
  subtitleColor?: string;
}

export interface SDUITheme {
  primaryColor: string;
  accentColor: string;
  mode: 'LIGHT' | 'DARK' | string;
  logo?: SDUILogoTheme;
  logoBgColor?: string;
  logoIconColor?: string;
  logoSubtitleColor?: string;
}

export interface SDUIAction {
  type: string;
  payload: Record<string, unknown>;
}

export interface SDUINode {
  id: string;
  type?: string;
  imageUrl?: string;
  props?: Record<string, unknown>;
  action?: SDUIAction;
  items?: SDUINode[];
}

export interface SDUISection {
  id: string;
  type: string;
  props?: Record<string, unknown>;
  items?: SDUINode[];
}

export interface SDUIScreen {
  screen: string;
  version: string;
  presetId?: string;
  theme?: SDUITheme;
  sections: SDUISection[];
}

export type CampaignPreset = 'mega-sale' | 'tech-weekend' | string;
