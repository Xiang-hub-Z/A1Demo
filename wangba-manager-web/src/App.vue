<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { useDeviceStore } from '@/stores/device'
import { onMounted } from 'vue'

const deviceStore = useDeviceStore()

onMounted(() => {
  deviceStore.checkDeviceType()
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

#app {
  font-family: 'Helvetica Neue', Helvetica, 'PingFang SC', 'Hiragino Sans GB', 
               'Microsoft YaHei', '微软雅黑', Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  min-height: 100vh;
}

/* 移动端样式 */
@media (max-width: 768px) {
  .pc-only {
    display: none !important;
  }
}

/* PC端样式 */
@media (min-width: 769px) {
  .mobile-only {
    display: none !important;
  }
}
</style>