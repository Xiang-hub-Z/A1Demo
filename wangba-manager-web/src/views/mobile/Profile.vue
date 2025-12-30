<template>
  <div class="profile-page">
    <van-cell-group>
      <van-cell center>
        <template #title>
          <div class="user-info">
            <van-image
              round
              width="60"
              height="60"
              :src="userInfo.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
            />
            <div class="user-detail">
              <div class="username">{{ userInfo.username }}</div>
              <div class="realname">姓名: {{ userInfo.realname }}</div>
              <div class="balance">余额: ¥{{ userInfo.balance }}</div>
            </div>
          </div>
        </template>
      </van-cell>
    </van-cell-group>

    <van-cell-group style="margin-top: 20px;">
      <van-cell title="手机号" :value="userInfo.phone || '未设置'" />
      <van-cell title="注册时间" :value="formatTime(userInfo.createtime)" />
    </van-cell-group>

    <van-cell-group style="margin-top: 20px;">
      <van-cell title="我的订单" is-link to="/home/orders" />
      <van-cell title="在线充值" is-link to="/home/recharge" />
      <van-cell title="修改信息" is-link to="/edit-profile" />
      <van-cell title="帮助中心" is-link />
      <van-cell title="设置" is-link />
    </van-cell-group>

    <div class="logout-btn">
      <van-button type="default" block @click="handleLogout">
        退出登录
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showConfirmDialog, showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

// 使用计算属性获取实时用户信息
const userInfo = computed(() => {
  return userStore.userInfo || {
    username: '用户',
    realname: '',
    balance: '0.00',
    phone: '',
    createtime: ''
  }
})

// 格式化时间显示
const formatTime = (time) => {
  if (!time) return '未知'
  return new Date(time).toLocaleString('zh-CN')
}

const handleLogout = async () => {
  try {
    await showConfirmDialog({
      title: '确认退出',
      message: '确定要退出登录吗？'
    })
    
    userStore.logout()
    showToast('已退出登录')
    router.push('/login')
  } catch {
    // 用户取消
  }
}

// 页面加载时检查登录状态
onMounted(() => {
  if (!userStore.token || !userStore.userInfo) {
    showToast('请先登录')
    router.push('/login')
  }
})
</script>

<style scoped lang="scss">
.profile-page {
  min-height: 100vh;
  background: #f7f8fa;
  padding-bottom: 80px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-detail {
  margin-left: 12px;
}

.username {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 4px;
}

.realname {
  font-size: 14px;
  color: #666;
  margin-bottom: 4px;
}

.balance {
  font-size: 16px;
  color: #ee0a24;
  font-weight: bold;
}

.logout-btn {
  position: fixed;
  bottom: 40px;
  left: 0;
  right: 0;
  padding: 16px;
  background: white;
  border-top: 1px solid #ebedf0;
}
</style>