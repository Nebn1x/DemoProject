/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_URL: string;
  readonly VITE_WS_URL: string;
}

interface ImportMeta {
  readonly env: ImportMetaEnv;
}

// імпорти стилів і ассетів
declare module '*.css';
declare module '*.svg';
declare module '*.png';
declare module '*.jpg';
