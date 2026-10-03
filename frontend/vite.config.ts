import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import tailwindcss from '@tailwindcss/vite'

const proxy = { '/api': { target: 'http://127.0.0.1:8080', changeOrigin: false } }
export default defineConfig({ plugins: [vue(), tailwindcss()], server: { proxy }, preview: { proxy } })
