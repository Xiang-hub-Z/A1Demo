<template>
  <div class="register-page">
    <div class="register-container">
      <div class="header-section">
        <div class="back-btn" @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
        </div>
        <h2>用户注册</h2>
        <p>创建您的睿控网咖管理系统账户</p>
      </div>
      
      <van-form @submit="onSubmit" class="register-form">
        <van-cell-group inset>
          <van-field
            v-model="form.username"
            name="username"
            label="用户名"
            placeholder="请输入用户名"
            :rules="usernameRules"
            left-icon="user-o"
          />
          <van-field
            v-model="form.realname"
            name="realname"
            label="真实姓名"
            placeholder="请输入真实姓名"
            :rules="realnameRules"
            left-icon="user-o"
          />
          <van-field
            v-model="form.phone"
            name="phone"
            label="手机号"
            placeholder="请输入手机号"
            :rules="phoneRules"
            left-icon="phone-o"
          />
          <van-field
            v-model="form.password"
            type="password"
            name="password"
            label="密码"
            placeholder="请输入密码"
            :rules="passwordRules"
            left-icon="lock-o"
          />
        </van-cell-group>
        
        <div class="form-actions">
          <van-button round block type="primary" native-type="submit" :loading="loading">
            注册
          </van-button>
        </div>
      </van-form>
      
      <div class="register-footer">
        <p>已有账户？ <span class="link" @click="goToLogin">立即登录</span></p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const form = reactive({
  username: '',
  realname: '',
  phone: '',
  password: ''
})

// 验证规则
const usernameRules = [
  { required: true, message: '请输入用户名' }
]

const realnameRules = [
  { required: true, message: '请输入真实姓名' }
]

const phoneRules = [
  { required: true, message: '请输入手机号' },
  { 
    validator: (value) => !value || /^1[3-9]\d{9}$/.test(value),
    message: '请输入正确的手机号格式'
  }
]

const passwordRules = [
  { required: true, message: '请输入密码' }
]

const goBack = () => {
  router.back()
}

const goToLogin = () => {
  router.push('/login')
}

const onSubmit = async () => {
  loading.value = true
  
  try {
    // 准备注册数据，余额默认为0
    const registerData = {
      ...form,
      balance: 0.00,  // 默认余额为0
    //   createtime: new Date().toISOString().replace('T', ' ').substring(0, 19)
    }
    
    // 调用真实的注册接口
    const result = await userStore.register(registerData)
    
    if (result.success) {
      showToast(result.message || '注册成功！')
      
      // 注册成功后跳转到登录页面
      setTimeout(() => {
        router.push('/login')
      }, 1000)
    } else {
      showToast(result.message || '注册失败')
    }
  } catch (error) {
    showToast('注册失败，请重试')
    console.error('注册错误:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.register-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.register-container {
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
  position: relative;
  
  .back-btn {
    position: absolute;
    left: 0;
    top: 0;
    width: 32px;
    height: 32px;
    border-radius: 50%;
    background: #f5f5f5;
    display: flex;
    align-items: center;
    justify-content: center;
    cursor: pointer;
    color: #666;
    transition: all 0.3s;
    
    &:hover {
      background: #e8e8e8;
    }
  }
  
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

.register-footer {
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

// 移动端适配
@media (max-width: 768px) {
  .register-page {
    padding: 20px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    min-height: 100vh;
  }
  
  .register-container {
    box-shadow: 0 10px 40px rgba(0, 0, 0, 0.1);
    border-radius: 15px;
    height: auto;
    display: block;
  }
}
</style>