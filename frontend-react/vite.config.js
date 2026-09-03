import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// Proxy: durante el desarrollo, las peticiones a /api se redirigen al
// backend prototipo Express (puerto 3000). Cuando el backend Spring Boot
// esté listo (Fase 3), solo se cambia el target a http://localhost:8080.
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      '/api': 'http://localhost:8080'
    }
  }
});
