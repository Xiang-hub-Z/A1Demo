<template>
  <div class="edit-profile-page">
    <van-nav-bar
      title="修改信息"
      left-text="返回"
      left-arrow
      @click-left="onClickLeft"
    />
    
    <van-form @submit="onSubmit" class="edit-form">
      <van-cell-group inset>
        <van-field
          v-model="form.username"
          name="username"
          label="用户名"
          placeholder="请输入用户名"
          :rules="usernameRules"
        />
        <van-field
          v-model="form.password"
          type="password"
          name="password"
          label="密码"
          placeholder="请输入新密码（留空不修改）"
          :rules="passwordRules"
        />
        <van-field
          v-model="form.realname"
          name="realname"
          label="真实姓名"
          placeholder="请输入真实姓名"
          :rules="[{ required: true, message: '请输入真实姓名' }]"
        />
        <van-field
          v-model="form.phone"
          name="phone"
          label="手机号"
          placeholder="请输入手机号"
          :rules="phoneRules"
        />
      </van-cell-group>
      
      <div class="submit-btn">
        <van-button round block type="primary" native-type="submit" :loading="loading">
          保存修改
        </van-button>
      </div>
    </van-form>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { updateUserInfo as userUpdate } from '@/api/user'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  realname: '',
  phone: ''
})

// 验证规则
const usernameRules = [
  { required: true, message: '请输入用户名' }
]

const passwordRules = [
  { 
    validator: (value) => !value || value.length >= 6,
    message: '密码长度至少6位'
  }
]

const phoneRules = [
  { 
    validator: (value) => !value || /^1[3-9]\d{9}$/.test(value),
    message: '请输入正确的手机号格式'
  }
]

// 返回上一页
const onClickLeft = () => {
  router.back()
}

// 提交修改
const onSubmit = async () => {
  loading.value = true
  
  try {
    // 构建更新数据
    const updateData = {
      id: userStore.userInfo.id,
      username: form.username,
      realname: form.realname,
      phone: form.phone
    }
    
    // 如果密码不为空，则更新密码
    if (form.password) {
      updateData.password = form.password
    }
    
    // 调用后端更新接口
    const response = await userUpdate(updateData)
    
    if (response === '修改成功') {
      // 更新本地用户信息
      const updatedUserInfo = {
        ...userStore.userInfo,
        username: form.username,
        realname: form.realname,
        phone: form.phone
      }
      
      userStore.setUserInfo(updatedUserInfo)
      showToast('修改成功')
      router.back()
    } else {
      showToast('修改失败')
    }
  } catch (error) {
    showToast('修改失败，请重试')
    console.error('修改信息错误:', error)
  } finally {
    loading.value = false
  }
}

// 页面加载时填充当前用户信息
onMounted(() => {
  if (userStore.userInfo) {
    form.username = userStore.userInfo.username || ''
    form.realname = userStore.userInfo.realname || ''
    form.phone = userStore.userInfo.phone || ''
    // 密码留空，用户可以选择是否修改
    form.password = ''
  }
})
</script>

<style scoped lang="scss">
.edit-profile-page {
  min-height: 100vh;
  background: #f7f8fa;
}

.edit-form {
  margin-top: 20px;
}

.submit-btn {
  margin: 30px 16px;
  
  .van-button {
    height: 44px;
    font-size: 16px;
  }
}
</style>