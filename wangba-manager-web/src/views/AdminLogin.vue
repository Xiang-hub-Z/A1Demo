<template>
  <div class="admin-login-page">
    <div class="login-container">
      <div class="header-section">
        <div class="logo">
          <el-icon><Monitor /></el-icon>
          <h2>管理员登录</h2>
        </div>
        <p>睿控网咖管理系统管理后台</p>
      </div>
      
      <el-form
        :model="form"
        @submit.prevent="onSubmit"
        class="login-form"
        size="large"
      >
        <el-form-item>
          <el-input
            v-model="form.username"
            placeholder="请输入管理员账号"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>
        
        <el-form-item>
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>
        
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="onSubmit"
            style="width: 100%;"
          >
            {{ loading ? '登录中...' : '登录' }}
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const form = reactive({
  username: '',
  password: ''
})

const onSubmit = async () => {
  loading.value = true
  
  try {
    // 确保传递正确的登录数据
    const loginData = {
      username: form.username,
      password: form.password
    }
    
    // 调用真实的管理员登录接口
    const result = await userStore.adminLoginAction(loginData)
    
    if (result.success) {
      ElMessage.success('管理员登录成功！')
      router.push('/admin/dashboard')
    } else {
      ElMessage.error(result.message || '账号或密码错误')
    }
  } catch (error) {
    ElMessage.error('登录失败，请重试')
    console.error('管理员登录错误:', error)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped lang="scss">
.admin-login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  background: white;
  padding: 40px;
  border-radius: 15px;
  box-shadow: 0 15px 50px rgba(0, 0, 0, 0.1);
  width: 100%;
  max-width: 420px;
}

.header-section {
  text-align: center;
  margin-bottom: 40px;
  
  .logo {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 10px;
    margin-bottom: 10px;
    
    .el-icon {
      font-size: 32px;
      color: #409EFF;
    }
    
    h2 {
      margin: 0;
      color: #303133;
      font-size: 24px;
    }
  }
  
  p {
    margin: 0;
    color: #909399;
    font-size: 14px;
  }
}

.login-form {
  :deep(.el-input__wrapper) {
    border-radius: 8px;
  }
  
  :deep(.el-button) {
    border-radius: 8px;
    height: 44px;
    font-size: 16px;
  }
}

.login-footer {
  margin-top: 30px;
  
  .demo-accounts {
    background: #f8f9fa;
    padding: 15px;
    border-radius: 8px;
    border-left: 4px solid #409EFF;
    
    h4 {
      margin: 0 0 10px 0;
      color: #303133;
      font-size: 14px;
    }
    
    p {
      margin: 5px 0;
      color: #606266;
      font-size: 13px;
    }
  }
}
</style>