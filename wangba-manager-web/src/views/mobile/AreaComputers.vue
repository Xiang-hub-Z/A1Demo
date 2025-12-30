<template>
  <div class="computers-page">
    <!-- 页面标题 -->
    <van-nav-bar
      :title="areaName + ' - 选择电脑'"
      left-text="返回"
      left-arrow
      @click-left="$router.back()"
    />

    <!-- 区域信息 -->
    <div class="area-info">
      <div class="info-card">
        <div class="info-item">
          <van-icon name="desktop-o" />
          <span>总共 {{ totalComputers }} 台电脑</span>
        </div>
        <div class="info-item">
          <van-icon name="success" />
          <span>{{ availableComputers }} 台可用</span>
        </div>
        <div class="info-item">
          <van-icon name="clock-o" />
          <span>价格范围: ¥{{ minPrice }}-{{ maxPrice }}/小时</span>
        </div>
      </div>
    </div>

    <!-- 计算机列表 -->
    <div class="computers-list">
      <div 
        v-for="computer in computers" 
        :key="computer.id"
        class="computer-item"
        :class="{ 
          'available': computer.status === 1,
          'in-use': computer.status === 2,
          'maintenance': computer.status === 3
        }"
        @click="handleComputerSelect(computer)"
      >
        <div class="computer-header">
          <h3 class="computer-name">{{ computer.computerNo }}</h3>
          <van-tag :type="getComputerStatusType(computer.status)" size="medium">
            {{ getComputerStatusText(computer.status) }}
          </van-tag>
        </div>
        
        <div class="computer-details">
          <p class="computer-config">{{ computer.configuration }}</p>
          <p class="computer-ip">IP: {{ computer.ipAddress }}</p>
        </div>
        
        <div class="computer-footer">
          <div class="computer-price">¥{{ computer.pricePerHour }}/小时</div>
          <div class="computer-action">
            <van-button 
              v-if="computer.status === 1" 
              type="primary" 
              size="small"
              @click.stop="handleStartSession(computer)"
            >
              立即上机
            </van-button>
            <van-button 
              v-else 
              type="default" 
              size="small"
              disabled
            >
              暂不可用
            </van-button>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <van-loading v-if="loading" size="24px" vertical>加载中...</van-loading>

    <!-- 空状态 -->
    <van-empty 
      v-if="!loading && computers.length === 0" 
      description="该区域暂无电脑" 
    />

    <!-- 上机确认对话框 -->
    <van-dialog
      v-model:show="showConfirmDialog"
      title="确认上机"
      show-cancel-button
      @confirm="confirmStartSession"
    >
      <div class="confirm-dialog">
        <p>确定要使用以下电脑开始上机吗？</p>
        <div class="computer-info" v-if="selectedComputer">
          <div class="info-row">
            <span class="label">电脑编号:</span>
            <span class="value">{{ selectedComputer.computerNo }}</span>
          </div>
          <div class="info-row">
            <span class="label">配置信息:</span>
            <span class="value">{{ selectedComputer.configuration }}</span>
          </div>
          <div class="info-row">
            <span class="label">价格:</span>
            <span class="value price">¥{{ selectedComputer.pricePerHour }}/小时</span>
          </div>
        </div>
      </div>
    </van-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast, showConfirmDialog as showConfirmDialogFn } from 'vant'

// API
import { getComputersByArea } from '@/api/computer'
import { startSession, getCurrentSession } from '@/api/computerSession'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const loading = ref(false)
const showConfirmDialog = ref(false)  // 确保这个只声明一次
const selectedComputer = ref(null)
const areaName = ref('')
const computers = ref([])

// 计算属性
const totalComputers = computed(() => computers.value.length)
const availableComputers = computed(() => {
  return computers.value.filter(computer => computer.status === 1).length
})
const minPrice = computed(() => {
  if (computers.value.length === 0) return 0
  const prices = computers.value.map(c => parseFloat(c.pricePerHour) || 0)
  return Math.min(...prices)
})
const maxPrice = computed(() => {
  if (computers.value.length === 0) return 0
  const prices = computers.value.map(c => parseFloat(c.pricePerHour) || 0)
  return Math.max(...prices)
})

// 获取计算机状态文本
const getComputerStatusText = (status) => {
  const statusMap = {
    1: '空闲',
    2: '使用中', 
    3: '维护中'
  }
  return statusMap[status] || '未知'
}

// 获取计算机状态标签类型
const getComputerStatusType = (status) => {
  const typeMap = {
    1: 'success',
    2: 'danger',
    3: 'warning'
  }
  return typeMap[status] || 'default'
}

// 计算机选择处理
const handleComputerSelect = (computer) => {
  console.log('选择计算机:', computer)
}

// 开始上机
const handleStartSession = async (computer) => {
  // 检查用户是否登录
  if (!userStore.userInfo || !userStore.userInfo.id) {
    showToast('请先登录')
    router.push('/login')
    return
  }

  // 检查用户是否已有进行中的会话
  try {
    const response = await getCurrentSession(userStore.userInfo.id)
    if (response.code === 200 && response.data) {
      showToast('您已有进行中的上机会话，请先结束当前会话')
      router.push('/user/session')
      return
    }
  } catch (error) {
    console.error('检查当前会话失败:', error)
  }

  selectedComputer.value = computer
  showConfirmDialog.value = true
}

// 确认开始上机
const confirmStartSession = async () => {
  if (!selectedComputer.value) return
  
  try {
    const response = await startSession({
      userId: userStore.userInfo.id,
      computerId: selectedComputer.value.id
    })
    
    if (response.code === 200) {
      showToast('上机成功')
      // 刷新计算机列表
      await fetchComputers()
      // 跳转到上机详情页面
      router.push('/user/session')
    } else {
      showToast(response.message || '上机失败')
    }
  } catch (error) {
    console.error('上机失败:', error)
    showToast('上机失败，请重试')
  } finally {
    showConfirmDialog.value = false
    selectedComputer.value = null
  }
}

// 获取计算机列表
const fetchComputers = async () => {
  const areaId = route.params.id
  if (!areaId) {
    showToast('区域ID不存在')
    return
  }
  
  loading.value = true
  try {
    const response = await getComputersByArea(areaId)
    if (response.code === 200) {
      computers.value = response.data
      // 设置区域名称（这里需要你从区域接口获取）
      areaName.value = await getAreaNameById(areaId)
    } else {
      showToast(response.message || '获取计算机列表失败')
    }
  } catch (error) {
    console.error('获取计算机列表失败:', error)
    showToast('获取计算机列表失败')
  } finally {
    loading.value = false
  }
}

// 根据区域ID获取区域名称
const getAreaNameById = async (areaId) => {
  // 这里需要你调用区域接口获取区域名称
  // 暂时使用模拟数据
  const areaMap = {
    '1': '大厅区域',
    '2': 'VIP包厢', 
    '3': '竞技区',
    '4': '休闲区'
  }
  return areaMap[areaId] || '未知区域'
}

// 页面加载时获取数据
onMounted(() => {
  fetchComputers()
})
</script>

<style scoped lang="scss">
.computers-page {
  background-color: #f7f8fa;
  min-height: 100vh;
}

.area-info {
  padding: 12px;
  
  .info-card {
    background: white;
    border-radius: 8px;
    padding: 12px;
    display: flex;
    justify-content: space-around;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
    
    .info-item {
      display: flex;
      flex-direction: column;
      align-items: center;
      font-size: 12px;
      color: #646566;
      
      .van-icon {
        margin-bottom: 4px;
        font-size: 16px;
        color: #323233;
      }
    }
  }
}

.computers-list {
  padding: 12px;
}

.computer-item {
  background: white;
  border-radius: 8px;
  padding: 12px;
  margin-bottom: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  &.available {
    border-left: 4px solid #07c160;
  }
  
  &.in-use {
    border-left: 4px solid #ee0a24;
    opacity: 0.7;
  }
  
  &.maintenance {
    border-left: 4px solid #ff976a;
    opacity: 0.7;
  }
  
  .computer-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    
    .computer-name {
      margin: 0;
      font-size: 16px;
      font-weight: bold;
      color: #323233;
    }
  }
  
  .computer-details {
    margin-bottom: 12px;
    
    .computer-config {
      margin: 0 0 4px 0;
      font-size: 13px;
      color: #646566;
    }
    
    .computer-ip {
      margin: 0;
      font-size: 12px;
      color: #969799;
    }
  }
  
  .computer-footer {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .computer-price {
      font-size: 14px;
      font-weight: bold;
      color: #ee0a24;
    }
  }
}

.confirm-dialog {
  padding: 16px 0;
  
  .computer-info {
    margin-top: 12px;
    background: #f7f8fa;
    border-radius: 8px;
    padding: 12px;
    
    .info-row {
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
        }
      }
    }
  }
}
</style>