import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src')
    }
  },
  server: {
    port: 8001,
    proxy: {
      '/api': {
        target: 'http://localhost:8090', // Spring Boot 后端地址localhost--47999152
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, '')
      }
    }
  },
  // 项目部署的基本路径，默认 "/"
  publicPath: process.env.NODE_ENV === "production" ? "/": "/",
  // 项目打包的根目录，默认 "dist"
  outputDir: "dist",
  // 项目打包的静态资源存放目录，默认 ""
  assetsDir: "",
  // 项目打包的index.html输出路径，默认 "index.html"
  indexPath: "index.html",
  // 项目打包是否生成js的 source map，调试包，默认 true，生产部署设置为false
  productionSourceMap: false,
})