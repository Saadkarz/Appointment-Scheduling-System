/// <reference types="vite/client" />

interface ImportMetaEnv {
  readonly VITE_API_BASE_URL: string
  readonly VITE_WS_BASE_URL: string
  readonly VITE_ENABLE_GOOGLE_CALENDAR: string
  readonly VITE_ENABLE_MS_CALENDAR: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
