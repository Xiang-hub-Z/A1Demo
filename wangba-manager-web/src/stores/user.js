import { defineStore } from 'pinia'
import { ref } from 'vue'
import { userLogin, userRegister } from '@/api/user'
import { adminLogin } from '@/api/admin'  // 导入管理员登录API

export const useUserStore = defineStore('user', () => {
  const userInfo = ref(null)
  const adminInfo = ref(null)
  const token = ref('')
  const userType = ref('') // 'user' 或 'admin'
  
  // 用户登录
  const login = async (loginData) => {
    try {
      const response = await userLogin(loginData)
      if (response.code === 200) {
        userInfo.value = response.data
        userType.value = 'user'
        setToken(`user-${response.data.id}-${Date.now()}`)
        return { success: true, data: response.data }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      return { success: false, message: '登录失败，请重试' }
    }
  }
  
  // 管理员登录
const adminLoginAction = async (loginData) => {
  try {
    // 确保字段名与后端一致
    const requestData = {
      username: loginData.username,
      password: loginData.password
    }
    
    console.log('发送管理员登录请求:', requestData)
    const response = await adminLogin(requestData)
    console.log('管理员登录响应:', response)
    
    if (response.success) {
      // 设置管理员信息 - 确保字段正确
      adminInfo.value = {
        id: response.user.id,
        username: response.user.username,
        name: response.user.name,
        phone: response.user.phone
      }
      userType.value = 'admin'
      setToken(response.token)
      return { success: true, data: response.user }
    } else {
      return { success: false, message: response.message }
    }
  } catch (error) {
    console.error('管理员登录请求错误:', error)
    return { success: false, message: '管理员登录失败' }
  }
}
  
  // 用户注册
  const register = async (registerData) => {
    try {
      const response = await userRegister(registerData)
      if (response.code === 200) {
        return { success: true, message: response.message }
      } else {
        return { success: false, message: response.message }
      }
    } catch (error) {
      return { success: false, message: '注册失败，请重试' }
    }
  }
  
  const setUserInfo = (info) => {
    userInfo.value = info
    userType.value = 'user'
  }
  
  const setAdminInfo = (info) => {
    adminInfo.value = info
    userType.value = 'admin'
  }
  
  const setToken = (newToken) => {
    token.value = newToken
    localStorage.setItem('token', newToken)
  }
  
  const logout = () => {
    userInfo.value = null
    adminInfo.value = null
    token.value = ''
    userType.value = ''
    localStorage.removeItem('token')
  }
  
  // 初始化时从 localStorage 读取 token
  const initToken = () => {
    const savedToken = localStorage.getItem('token')
    if (savedToken) {
      token.value = savedToken
    }
  }
  
  return {
    userInfo,
    adminInfo,
    token,
    userType,
    login,
    adminLoginAction,  // 导出管理员登录方法
    register,
    setUserInfo,
    setAdminInfo,
    setToken,
    logout,
    initToken
  }
})