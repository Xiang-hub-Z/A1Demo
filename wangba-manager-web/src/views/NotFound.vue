<template>
  <div class="not-found">
    <div class="content">
      <h1>404</h1>
      <p>页面不存在</p>
      <van-button type="primary" @click="$router.push('/')">
        返回首页
      </van-button>
    </div>
  </div>
</template>

<script setup>
</script>

<style scoped>
.not-found {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f5f5f5;
}

.content {
  text-align: center;
}

.content h1 {
  font-size: 80px;
  margin-bottom: 20px;
  color: #666;
}

.content p {
  font-size: 18px;
  margin-bottom: 30px;
  color: #999;
}
</style>