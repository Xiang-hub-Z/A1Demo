<template>
  <div class="mobile-layout">
    <!-- 顶部导航栏 -->
    <van-nav-bar
      :title="currentTitle"
      fixed
      safe-area-inset-top
      placeholder
      :left-arrow="showBackButton"
      @click-left="handleBack"
    >
      <template #right>
        <van-icon name="search" size="18" @click="handleSearch" />
      </template>
    </van-nav-bar>
    
    <!-- 主要内容区域 -->
    <main class="mobile-content">
      <router-view v-slot="{ Component }">
        <transition name="fade" mode="out-in">
          <component :is="Component" />
        </transition>
      </router-view>
    </main>
    
    <!-- 底部导航 -->
    <van-tabbar v-model="activeTab" fixed route safe-area-inset-bottom>
      <van-tabbar-item to="/home/index" name="home" icon="home-o">首页</van-tabbar-item>
      <van-tabbar-item to="/home/recharge" name="recharge" icon="cash-back-record-o">充值</van-tabbar-item>
      <van-tabbar-item to="/home/orders" name="orders" icon="orders-o">订单</van-tabbar-item>
      <van-tabbar-item to="/home/profile" name="profile" icon="user-o">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast } from 'vant'

const route = useRoute()
const router = useRouter()

const activeTab = ref('home')

const currentTitle = computed(() => {
  return route.meta.title || '睿控网咖管理系统'
})

const showBackButton = computed(() => {
  return route.path !== '/home/index'
})

const handleBack = () => {
  if (showBackButton.value) {
    router.back()
  }
}

const handleSearch = () => {
  showToast('搜索功能开发中')
}
</script>

<style scoped lang="scss">
.mobile-layout {
  min-height: 100vh;
  background-color: #f7f8fa;
}

.mobile-content {
  padding: 12px;
  padding-top: calc(46px + 12px);
  padding-bottom: calc(50px + 12px);
  min-height: 100vh;
}

// 过渡动画
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

// Vant 组件样式定制
:deep(.van-nav-bar) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  
  .van-nav-bar__title {
    color: white;
    font-weight: bold;
    font-size: 16px;
  }
  
  .van-icon {
    color: white;
  }
}

:deep(.van-tabbar) {
  border-top: 1px solid #ebedf0;
  background: white;
  
  .van-tabbar-item--active {
    color: #1989fa;
  }
}
</style>