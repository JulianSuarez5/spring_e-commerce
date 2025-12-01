/**
 * Configuración de Vite para servir la interfaz Apple Style.
 * Define alias, servidor y build para integrarse con Spring Boot.
 * Se usa desde scripts npm (dev/build) y permite proxy hacia el backend.
 * Limitación: requiere Node instalado localmente; aquí solo dejamos la config lista.
 */
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
  server: {
    port: 5173,
    proxy: {
      '/api': 'http://localhost:8080'
    }
  },
  build: {
    outDir: 'dist',
    emptyOutDir: true
  }
});

