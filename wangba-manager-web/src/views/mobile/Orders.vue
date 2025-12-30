<template>
  <div class="orders-page">
    <!-- 状态筛选 -->
    <van-tabs v-model:active="activeStatus" @change="onStatusChange">
      <van-tab title="全部" name=""></van-tab>
      <van-tab title="进行中" name="1"></van-tab>
      <van-tab title="已结束" name="2"></van-tab>
    </van-tabs>

    <!-- 订单列表 -->
    <div class="orders-list">
      <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
        <van-list
          v-model:loading="loading"
          :finished="finished"
          finished-text="没有更多记录了"
          @load="onLoad"
        >
          <div 
            v-for="session in sessions" 
            :key="session.id"
            class="session-item"
          >
            <van-card>
              <template #header>
                <div class="session-header">
                  <div class="session-id">记录 #{{ session.id }}</div>
                  <van-tag :type="getSessionStatusType(session.status)" size="medium">
                    {{ getSessionStatusText(session.status) }}
                  </van-tag>
                </div>
              </template>
              
              <template #desc>
                <div class="session-info">
                  <div class="info-row">
                    <span class="label">电脑:</span>
                    <span class="value">{{ session.computerNo || `电脑 ${session.computerId}` }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">开始时间:</span>
                    <span class="value">{{ formatTime(session.startTime) }}</span>
                  </div>
                  <div class="info-row" v-if="session.endTime">
                    <span class="label">结束时间:</span>
                    <span class="value">{{ formatTime(session.endTime) }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">使用时长:</span>
                    <span class="value">{{ calculateDuration(session.startTime, session.endTime) }}</span>
                  </div>
                  <div class="info-row">
                    <span class="label">费用:</span>
                    <span class="value price">¥{{ session.totalAmount || '0.00' }}</span>
                  </div>
                </div>
              </template>

              <template #footer>
                <div class="session-actions">
                  <van-button 
                    v-if="session.status === 1" 
                    type="danger" 
                    size="small"
                    @click="$router.push('/user/session')"
                  >
                    结束上机
                  </van-button>
                  <van-button 
                    v-else 
                    type="default" 
                    size="small"
                    plain
                    @click="handleViewDetail(session)"
                  >
                    查看详情
                  </van-button>
                </div>
              </template>
            </van-card>
          </div>
        </van-list>
      </van-pull-refresh>
    </div>

    <!-- 加载状态 -->
    <van-loading v-if="initialLoading" size="24px" vertical>加载中...</van-loading>

    <!-- 空状态 -->
    <van-empty 
      v-if="!initialLoading && sessions.length === 0" 
      :description="emptyText" 
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

// API - 使用 computerSession 接口
import { getSessionList } from '@/api/computerSession'

const router = useRouter()
const userStore = useUserStore()

// 响应式数据
const activeStatus = ref('')
const sessions = ref([])
const loading = ref(false)
const finished = ref(false)
const refreshing = ref(false)
const initialLoading = ref(false)

// 分页参数
const pagination = reactive({
  page: 1,
  pageSize: 10,
  total: 0
})

// 计算属性
const emptyText = computed(() => {
  if (activeStatus.value === '') return '暂无上机记录'
  if (activeStatus.value === '1') return '暂无进行中的会话'
  if (activeStatus.value === '2') return '暂无已结束的会话'
  return '暂无记录'
})

// 获取会话状态文本
const getSessionStatusText = (status) => {
  const statusMap = {
    1: '进行中',
    2: '已结束',
    3: '异常'
  }
  return statusMap[status] || '未知'
}

// 获取会话状态标签类型
const getSessionStatusType = (status) => {
  const typeMap = {
    1: 'primary',
    2: 'success',
    3: 'warning'
  }
  return typeMap[status] || 'default'
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 计算使用时长
const calculateDuration = (startTime, endTime) => {
  if (!startTime) return '-'
  
  const start = new Date(startTime)
  const end = endTime ? new Date(endTime) : new Date()
  const diff = end - start
  const hours = Math.floor(diff / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))
  
  if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  }
  return `${minutes}分钟`
}

// 状态变化处理
const onStatusChange = (name) => {
  activeStatus.value = name
  // 重置分页和列表
  pagination.page = 1
  sessions.value = []
  finished.value = false
  // 重新加载
  onLoad()
}

// 下拉刷新
const onRefresh = () => {
  // 清空列表
  sessions.value = []
  finished.value = false
  pagination.page = 1
  
  // 重新加载数据
  loading.value = true
  onLoad()
}

// 加载更多
const onLoad = async () => {
  if (refreshing.value) {
    sessions.value = []
    refreshing.value = false
  }

  try {
    // 构建请求参数
    const params = {
      userId: userStore.userInfo.id,
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    
    // 添加状态筛选
    if (activeStatus.value) {
      params.status = parseInt(activeStatus.value)
    }
    
    // 获取会话列表
    const response = await getSessionList(params)
    
    if (response.code === 200 && response.data) {
      const data = response.data
      const paginationInfo = response.pagination
      
      // 如果是第一页，直接赋值
      if (pagination.page === 1) {
        sessions.value = data
      } else {
        // 否则追加数据
        sessions.value = [...sessions.value, ...data]
      }
      
      // 更新分页信息
      if (paginationInfo) {
        pagination.total = paginationInfo.total
        
        // 判断是否已加载全部
        if (sessions.value.length >= paginationInfo.total || data.length < pagination.pageSize) {
          finished.value = true
        } else {
          pagination.page += 1
        }
      } else {
        // 如果没有分页信息，根据返回数据长度判断
        if (data.length < pagination.pageSize) {
          finished.value = true
        } else {
          pagination.page += 1
        }
      }
    } else {
      finished.value = true
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
    showToast('获取会话列表失败')
  } finally {
    loading.value = false
    initialLoading.value = false
    refreshing.value = false
  }
}

// 查看会话详情
const handleViewDetail = (session) => {
  // 这里可以跳转到会话详情页面或显示详情弹窗
  console.log('查看会话详情:', session)
  showToast('查看会话详情功能开发中')
}

// 页面加载时获取数据
onMounted(() => {
  initialLoading.value = true
  onLoad()
})
</script>

<style scoped lang="scss">
.orders-page {
  background-color: #f7f8fa;
  min-height: 100vh;
}

.orders-list {
  padding: 12px;
}

.session-item {
  margin-bottom: 12px;
  
  .session-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;
    
    .session-id {
      font-size: 14px;
      color: #646566;
      font-weight: 500;
    }
  }
  
  .session-info {
    .info-row {
      display: flex;
      justify-content: space-between;
      margin-bottom: 6px;
      font-size: 13px;
      
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
  
  .session-actions {
    display: flex;
    gap: 8px;
    justify-content: flex-end;
  }
}
</style>