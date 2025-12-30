import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useDeviceStore = defineStore('device', () => {
  const isMobile = ref(false)
  const isLoggedIn = ref(false)
  
  const checkDeviceType = () => {
    isMobile.value = window.innerWidth <= 768
    
    window.addEventListener('resize', () => {
      isMobile.value = window.innerWidth <= 768
    })
  }
  
  const setLoginStatus = (status) => {
    isLoggedIn.value = status
  }
  
  return {
    isMobile,
    isLoggedIn,
    checkDeviceType,
    setLoginStatus
  }
})