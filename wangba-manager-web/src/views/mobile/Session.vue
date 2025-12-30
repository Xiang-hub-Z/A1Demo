<template>
  <div class="session-page">
    <van-nav-bar
      title="我的上机"
      left-text="返回"
      left-arrow
      @click-left="$router.back()"
      fixed
      placeholder
    />
    
    <div v-if="currentSession" class="current-session">
      <van-card class="session-card">
        <template #title>
          <div class="session-title">
            <div class="title-left">
              <span>当前上机</span>
              <van-tag type="success" size="medium">进行中</van-tag>
            </div>
            <div class="status-indicator">
              <span class="dot" :class="{ 'online': computerState.isPoweredOn, 'offline': !computerState.isPoweredOn }"></span>
              {{ computerState.isPoweredOn ? (computerState.isLocked ? '已锁定' : '运行中') : '已关机' }}
            </div>
          </div>
        </template>
        <template #desc>
          <div class="session-info">
            <div class="info-item">
              <span class="label">电脑编号:</span>
              <span class="value">{{ currentSession.computerNo || '加载中...' }}</span>
            </div>
            <div class="info-item">
              <span class="label">开始时间:</span>
              <span class="value">{{ formatTime(currentSession.startTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">已使用:</span>
              <span class="value">{{ getDuration(currentSession.startTime) }}</span>
            </div>
            <div class="info-item">
              <span class="label">计费时长:</span>
              <span class="value">{{ getBillingHours(currentSession.startTime) }}小时</span>
            </div>
            <div class="info-item">
              <span class="label">每小时价格:</span>
              <span class="value">¥{{ currentSession.pricePerHour || 10 }}</span>
            </div>
            <div class="info-item">
              <span class="label">计费规则:</span>
              <span class="value rule">不足1小时按1小时计算</span>
            </div>
            <div class="info-item">
              <span class="label">预估费用:</span>
              <span class="value price">¥{{ calculateCurrentAmount(currentSession) }}</span>
            </div>
          </div>
        </template>
        
        <template #footer>
          <div class="control-panel">
            <div class="panel-title">远程控制</div>
            <van-grid :column-num="2" :gutter="10" clickable>
              <van-grid-item>
                <van-button 
                  block 
                  color="#07c160" 
                  icon="play-circle-o" 
                  :loading="loadingState.powerOn"
                  :disabled="computerState.isPoweredOn"
                  @click="handleComputerAction('powerOn')"
                >
                  开机
                </van-button>
              </van-grid-item>

              <van-grid-item>
                <van-button 
                  block 
                  color="#323233" 
                  plain
                  icon="close" 
                  :loading="loadingState.powerOff"
                  :disabled="!computerState.isPoweredOn"
                  @click="handleComputerAction('powerOff')"
                >
                  关机
                </van-button>
              </van-grid-item>

              <van-grid-item>
                <van-button 
                  block 
                  color="#1989fa" 
                  icon="replay" 
                  :loading="loadingState.restart"
                  :disabled="!computerState.isPoweredOn"
                  @click="handleComputerAction('restart')"
                >
                  重启
                </van-button>
              </van-grid-item>

              <van-grid-item>
                <van-button 
                  block 
                  :color="computerState.isLocked ? '#07c160' : '#ff976a'" 
                  :icon="computerState.isLocked ? 'lock-open' : 'lock'" 
                  :loading="loadingState.lock"
                  :disabled="!computerState.isPoweredOn"
                  @click="handleComputerAction('toggleLock')"
                >
                  {{ computerState.isLocked ? '解锁' : '锁定' }}
                </van-button>
              </van-grid-item>
            </van-grid>
          </div>
        </template>
      </van-card>
    </div>
    
    <van-empty v-else description="暂无上机会话">
      <van-button type="primary" @click="goToAreas">去上机</van-button>
    </van-empty>

    <div class="fixed-bottom-bar" v-if="currentSession">
      <div class="bar-content">
        <div class="bar-info">
          <span>当前消费: </span>
          <span class="price">¥{{ calculateCurrentAmount(currentSession) }}</span>
        </div>
        <van-button 
          type="danger" 
          round 
          class="end-btn"
          @click="handleEndSession"
        >
          结束上机
        </van-button>
      </div>
    </div>

    <van-dialog
      v-model:show="showEndDialog"
      title="结束上机"
      show-cancel-button
      @confirm="confirmEndSession"
    >
      <div class="end-dialog">
        <p>确定要结束当前上机会话吗？</p>
        <div class="amount-info" v-if="currentSession">
          <p class="amount">预估费用: ¥{{ calculateCurrentAmount(currentSession) }}</p>
          <p class="tip">* 实际费用以下机时系统计算为准</p>
          <p class="duration">预估时长: {{ getBillingHours(currentSession.startTime) }}小时</p>
          <p class="rule-tip">* 不足1小时按1小时计算</p>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue' // 引入 reactive
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast, showLoadingToast, closeToast } from 'vant' // 引入更多Toast

// API
import { getCurrentSession, endSession } from '@/api/computerSession'
import { getComputerById } from '@/api/computer'

const router = useRouter()
const userStore = useUserStore()

const currentSession = ref(null)
const showEndDialog = ref(false)
let timer = null

// --- 新增：模拟电脑状态和控制逻辑 ---

// 模拟电脑状态（默认开机、未锁定）
const computerState = reactive({
  isPoweredOn: true,
  isLocked: false
})

// 按钮 Loading 状态管理
const loadingState = reactive({
  powerOn: false,
  powerOff: false,
  restart: false,
  lock: false
})

// 处理电脑控制操作
const handleComputerAction = (action) => {
  // 模拟网络请求延迟
  const simulateRequest = (loadingKey, duration = 1500) => {
    loadingState[loadingKey] = true
    return new Promise(resolve => setTimeout(() => {
      loadingState[loadingKey] = false
      resolve()
    }, duration))
  }

  switch (action) {
    case 'powerOn':
      simulateRequest('powerOn').then(() => {
        computerState.isPoweredOn = true
        computerState.isLocked = false // 开机默认不锁定
        showToast({ type: 'success', message: '开机指令已发送', position: 'top' })
      })
      break
      
    case 'powerOff':
      simulateRequest('powerOff').then(() => {
        computerState.isPoweredOn = false
        showToast({ type: 'success', message: '电脑已关机', position: 'top' })
      })
      break
      
    case 'restart':
      simulateRequest('restart', 2000).then(() => {
        // 重启过程模拟：先关后开
        computerState.isPoweredOn = true 
        computerState.isLocked = false
        showToast({ type: 'success', message: '系统重启成功', position: 'top' })
      })
      break
      
    case 'toggleLock':
      simulateRequest('lock', 800).then(() => {
        computerState.isLocked = !computerState.isLocked
        const msg = computerState.isLocked ? '电脑已锁定' : '电脑已解锁'
        showToast({ type: 'success', message: msg, position: 'top' })
      })
      break
  }
}
// --- 新增逻辑结束 ---


// 格式化时间
const formatTime = (time) => {
  if (!time) return '--'
  return new Date(time).toLocaleString()
}

// 计算持续时间（显示用）
const getDuration = (startTime) => {
  if (!startTime) return '0分钟'
  
  const start = new Date(startTime)
  const now = new Date()
  const diff = now - start
  
  // 处理负值情况
  if (diff <= 0) return '0分钟'
  
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  const seconds = Math.floor((diff % (1000 * 60)) / 1000)
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟${seconds}秒`
  } else if (minutes > 0) {
    return `${minutes}分钟${seconds}秒`
  } else {
    return `${seconds}秒`
  }
}

// 计算计费时长（不足一小时按一小时计算）
const getBillingHours = (startTime) => {
  if (!startTime) return 1
  
  const start = new Date(startTime)
  const now = new Date()
  const diff = now - start
  
  // 处理异常情况
  if (diff <= 0) return 1 // 至少1小时
  
  const totalSeconds = Math.ceil(diff / 1000) // 总秒数，向上取整
  const totalMinutes = Math.ceil(totalSeconds / 60) // 总分钟数，向上取整
  
  // 计算计费小时数：不足1小时按1小时计算
  const billingHours = Math.ceil(totalMinutes / 60)
  
  return Math.max(billingHours, 1) // 至少1小时
}

// 计算预估费用（前端估算）
const calculateCurrentAmount = (session) => {
  if (!session || !session.startTime) return '0.00'
  
  // 获取计费小时数
  const billingHours = getBillingHours(session.startTime)
  
  // 使用会话中的价格，如果没有则使用默认价格
  const pricePerHour = session.pricePerHour || 10
  
  const amount = billingHours * pricePerHour
  
  return amount.toFixed(2)
}

// 获取电脑信息并合并到会话数据中
const fetchComputerInfo = async (session) => {
  if (!session || !session.computerId) return session
  
  try {
    const computerResponse = await getComputerById(session.computerId)
    if (computerResponse.code === 200 && computerResponse.data) {
      const computer = computerResponse.data
      return {
        ...session,
        computerNo: computer.computerNo,
        pricePerHour: computer.pricePerHour
      }
    }
  } catch (error) {
    console.error('获取电脑信息失败:', error)
  }
  
  return session
}

// 获取当前会话
const fetchCurrentSession = async () => {
  if (!userStore.userInfo || !userStore.userInfo.id) {
    showToast('请先登录')
    router.push('/login')
    return
  }

  try {
    const response = await getCurrentSession(userStore.userInfo.id)
    console.log('获取当前会话响应:', response)
    
    if (response.code === 200 && response.data) {
      // 获取电脑信息并合并到会话数据中
      const sessionWithComputer = await fetchComputerInfo(response.data)
      currentSession.value = sessionWithComputer
      
      // 如果有进行中的会话，开始定时刷新
      if (currentSession.value.status === 1) {
        startTimer()
      }
    } else {
      currentSession.value = null
      stopTimer()
    }
  } catch (error) {
    console.error('获取当前会话失败:', error)
    currentSession.value = null
  }
}

// 开始定时器
const startTimer = () => {
  stopTimer()
  timer = setInterval(() => {
    // 每30秒刷新一次会话信息
    fetchCurrentSession()
  }, 30000)
}

// 停止定时器
const stopTimer = () => {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

// 结束会话
const handleEndSession = () => {
  showEndDialog.value = true
}

// 确认结束会话
const confirmEndSession = async () => {
  if (!currentSession.value) return

  try {
    const response = await endSession({
      sessionId: currentSession.value.id
    })
    
    console.log('结束会话响应:', response)
    
    if (response.code === 200) {
      // 手动更新本地余额（估算）
      if (userStore.userInfo && userStore.userInfo.balance !== undefined) {
        const estimatedAmount = calculateCurrentAmount(currentSession.value)
        const currentBalance = parseFloat(userStore.userInfo.balance)
        const newBalance = (currentBalance - parseFloat(estimatedAmount)).toFixed(2)
        userStore.userInfo.balance = newBalance
      }
      
      showToast('下机成功')
      currentSession.value = null
      stopTimer()
    } else {
      showToast(response.message || '下机失败')
    }
  } catch (error) {
    console.error('结束会话失败:', error)
    showToast('结束会话失败，请重试')
  } finally {
    showEndDialog.value = false
  }
}

// 去上机
const goToAreas = () => {
  router.push('/areas')
}

// 页面加载时获取数据
onMounted(() => {
  fetchCurrentSession()
})

// 页面卸载时清除定时器
onUnmounted(() => {
  stopTimer()
})
</script>

<style scoped lang="scss">
.session-page {
  background-color: #f7f8fa;
  min-height: 100vh;
  /* 增加底部内边距，防止内容被固定按钮遮挡 */
  padding: 12px 12px 90px 12px;
}

.current-session {
  .session-card {
    border-radius: 12px;
    box-shadow: 0 4px 12px rgba(0,0,0,0.05);
  }

  .session-title {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .title-left {
      display: flex;
      align-items: center;
      gap: 8px;
      font-size: 16px;
      font-weight: bold;
    }

    .status-indicator {
      font-size: 12px;
      color: #969799;
      display: flex;
      align-items: center;
      
      .dot {
        width: 6px;
        height: 6px;
        border-radius: 50%;
        background-color: #c8c9cc;
        margin-right: 4px;
        
        &.online { background-color: #07c160; }
        &.offline { background-color: #ee0a24; }
      }
    }
  }
  
  .session-info {
    margin-top: 10px;
    
    .info-item {
      display: flex;
      justify-content: space-between;
      margin-bottom: 8px;
      font-size: 14px;
      
      &:last-child {
        margin-bottom: 0;
      }
      
      .label {
        color: #646566;
      }
      
      .value {
        color: #323233;
        font-weight: 500;
        
        &.price {
          color: #ee0a24;
          font-weight: bold;
          font-size: 16px;
        }
        
        &.rule {
          color: #ff976a;
          font-size: 12px;
        }
      }
    }
  }

  /* 控制面板样式 */
  .control-panel {
    margin-top: 16px;
    padding-top: 16px;
    border-top: 1px solid #ebedf0;

    .panel-title {
      font-size: 14px;
      color: #969799;
      margin-bottom: 12px;
      padding-left: 4px;
    }
    
    /* 调整按钮内部样式 */
    :deep(.van-button) {
      border-radius: 8px;
      height: 44px;
      font-size: 14px;
      box-shadow: 0 2px 4px rgba(0,0,0,0.05);
    }
  }
}

/* 底部固定操作栏 */
.fixed-bottom-bar {
  position: fixed;
  bottom: 0;
  left: 0;
  width: 100%;
  background-color: #fff;
  padding: 10px 16px;
  box-shadow: 0 -2px 10px rgba(0,0,0,0.05);
  z-index: 99;
  box-sizing: border-box;
  padding-bottom: calc(10px + env(safe-area-inset-bottom)); /* 适配 iPhone X 底部 */

  .bar-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    max-width: 768px; /* 限制大屏宽度 */
    margin: 0 auto;

    .bar-info {
      font-size: 14px;
      color: #323233;
      
      .price {
        color: #ee0a24;
        font-weight: bold;
        font-size: 18px;
      }
    }

    .end-btn {
      width: 120px;
      font-weight: bold;
      box-shadow: 0 4px 12px rgba(238, 10, 36, 0.3);
    }
  }
}

.end-dialog {
  padding: 16px 0;
  text-align: center;
  
  .amount-info {
    margin-top: 12px;
    padding: 12px;
    background: #f7f8fa;
    border-radius: 8px;
    
    .amount {
      margin: 0;
      font-size: 16px;
      font-weight: bold;
      color: #ee0a24;
    }
    
    .tip {
      margin: 4px 0;
      font-size: 12px;
      color: #ff976a;
      font-style: italic;
    }
    
    .duration {
      margin: 4px 0;
      font-size: 14px;
      color: #323233;
    }
    
    .rule-tip {
      margin: 4px 0 0 0;
      font-size: 12px;
      color: #ff976a;
    }
  }
}
</style>