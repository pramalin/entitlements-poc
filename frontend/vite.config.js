import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";

// Dev server pinned to 5173 - the Spring Boot backend's CORS config
// (WebConfig.java) only allows that exact origin.
export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    strictPort: true,
  },
});
