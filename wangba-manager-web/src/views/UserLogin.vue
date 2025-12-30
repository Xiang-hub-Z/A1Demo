<template>
  <div class="user-login-page">
    <div class="login-container">
      <div class="header-section">
        <h2>用户登录</h2>
        <p>欢迎使用睿控网咖管理系统</p>
      </div>
      
      <van-form @submit="onSubmit" class="login-form">
        <van-cell-group inset>
          <van-field
            v-model="form.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="[{ required: true, message: '请填写用户名' }]"
            left-icon="user-o"
          />
          <van-field
            v-model="form.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="[{ required: true, message: '请填写密码' }]"
            left-icon="lock-o"
          />
        </van-cell-group>
        
        <div class="form-actions">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            登录
          </van-button>
        </div>
      </van-form>
      
      <div class="login-footer">
        <p>还没有账户？ <span class="link" @click="goToRegister">立即注册</span></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const form = ref({
  username: '',
  password: ''
})

const goToRegister = () => {
  router.push('/register')
}

const onSubmit = async () => {
  loading.value = true
  
  try {
    // 调用真实的登录接口
    const result = await userStore.login(form.value)
    
    if (result.success) {
      showToast('登录成功！')  // 修改这里，去掉 .success
      // 登录成功后跳转到用户首页
      router.push('/home')
    } else {
      showToast(result.message || '登录失败')  // 这里也直接使用 showToast
    }
  } catch (error) {
    showToast('登录失败，请重试')  // 这里也直接使用 showToast
    console.error('登录错误:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.user-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  background: white;
  padding: 30px;
  border-radius: 15px;
  box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 400px;
}

.header-section {
  text-align: center;
  margin-bottom: 30px;
  
  h2 {
    color: #333;
    margin-bottom: 8px;
    font-size: 24px;
  }
  
  p {
    color: #666;
    margin: 0;
    font-size: 14px;
  }
}

.form-actions {
  margin: 30px 0;
  
  .van-button {
    height: 44px;
    font-size: 16px;
  }
}

.login-footer {
  text-align: center;
  
  p {
    margin: 8px 0;
    color: #666;
    font-size: 14px;
  }
  
  .link {
    color: #409EFF;
    cursor: pointer;
    
    &:hover {
      text-decoration: underline;
    }
  }
}

// 移动端适配 - 修复背景色问题
@media (max-width: 768px) {
  .user-login-page {
    padding: 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    min-height: 100vh;
  }
  
  .login-container {
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
    border-radius: 15px;
    height: auto;
    display: block;
  }
}
</style>