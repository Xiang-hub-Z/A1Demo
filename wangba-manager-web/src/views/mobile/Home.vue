<template>
  <div class="mobile-home">
    <!-- 用户信息卡片 -->
    <van-cell-group class="user-card">
      <van-cell center>
        <template #title>
          <div class="user-info">
            <van-image
              round
              width="50"
              height="50"
              :src="userStore.userInfo.avatar || 'https://fastly.jsdelivr.net/npm/@vant/assets/cat.jpeg'"
            />
            <div class="user-detail">
              <div class="username">{{ userStore.userInfo.realname || userStore.userInfo.username }}</div>
              <div class="balance">余额: ¥{{ userStore.userInfo.balance || '0.00' }}</div>
            </div>
          </div>
        </template>
        <van-button size="small" type="primary" @click="$router.push('recharge')">
          充值
        </van-button>
      </van-cell>
    </van-cell-group>

    <!-- 功能菜单 -->
    <van-grid :column-num="3" :border="false" clickable>
      <van-grid-item
        icon="desktop-o"
        text="上机"
        @click="$router.push('/areas')"
      />
      <van-grid-item
        icon="orders-o"
        text="我的上机"
        @click="$router.push('/user/session')"
      />
      <van-grid-item
        icon="cash-back-record-o"
        text="充值"
        @click="$router.push('recharge')"
      />
    </van-grid>

    <!-- 区域选择 -->
    <div class="areas-section">
      <div class="section-header">
        <h3>选择区域</h3>
        <van-button 
          size="small" 
          type="primary" 
          plain 
          @click="$router.push('/areas')"
        >
          查看全部
        </van-button>
      </div>
      
      <!-- 加载状态 -->
      <van-loading v-if="areasLoading" size="24px" vertical>加载中...</van-loading>
      
      <!-- 区域网格 -->
      <van-grid :column-num="2" :gutter="10" v-if="!areasLoading && areas.length > 0">
        <van-grid-item 
          v-for="area in areas" 
          :key="area.id"
          @click="handleAreaClick(area)"
        >
          <div class="area-card" :class="{ 'disabled': area.status !== 1 }">
            <van-image
              width="100%"
              height="80"
              fit="cover"
              :src="getAreaImage(area)"
              radius="4"
            />
            <div class="area-info">
              <div class="area-header">
                <div class="area-name">{{ area.name }}</div>
                <van-tag 
                  v-if="area.status === 2" 
                  type="danger" 
                  size="mini"
                >
                  禁用
                </van-tag>
              </div>
              <div class="area-desc">{{ area.description || '暂无描述' }}</div>
              <div class="area-stats">
                <span class="stat-item">
                  <van-icon name="desktop-o" size="12" />
                  {{ area.totalComputers || 0 }}台
                </span>
                <span class="stat-item available">
                  <van-icon name="success" size="12" />
                  {{ area.availableComputers || 0 }}台可用
                </span>
              </div>
            </div>
          </div>
        </van-grid-item>
      </van-grid>
      
      <!-- 空状态 -->
      <van-empty 
        v-if="!areasLoading && areas.length === 0" 
        description="暂无区域数据" 
      />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { showToast } from 'vant'

// 导入本地图片
import areaImage101 from '@/assets/images/101.jpg'
import areaImage102 from '@/assets/images/102.jpg'
import areaImage103 from '@/assets/images/103.jpg'

// API
import { getAreaList } from '@/api/area'
import { getComputersByArea } from '@/api/computer'

const router = useRouter()
const userStore = useUserStore()

const areas = ref([])
const areasLoading = ref(false)

// 图片数组
const areaImages = [areaImage101, areaImage102, areaImage103]

// 获取区域图片 - 根据区域ID选择对应的图片
const getAreaImage = (area) => {
  // 使用区域ID对图片数量取模，确保每个区域都有对应的图片
  const imageIndex = (area.id - 1) % areaImages.length
  return areaImages[imageIndex]
}

// 区域点击事件
const handleAreaClick = (area) => {
  // 检查区域是否启用
  if (area.status !== 1) {
    showToast('该区域暂不可用')
    return
  }

  // 检查是否有可用电脑
  if (area.availableComputers === 0) {
    showToast('该区域暂无可用电脑')
    return
  }

  router.push(`/area/${area.id}/computers`)
}

// 获取每个区域的电脑统计信息
const fetchAreaStatistics = async () => {
  const statisticsPromises = areas.value.map(async (area) => {
    try {
      const response = await getComputersByArea(area.id)
      if (response.code === 200 && response.data) {
        const computers = response.data
        
        // 计算统计信息
        const totalComputers = computers.length
        const availableComputers = computers.filter(computer => computer.status === 1).length
        
        return {
          ...area,
          totalComputers,
          availableComputers
        }
      }
      return area
    } catch (error) {
      console.error(`获取区域 ${area.id} 的电脑统计失败:`, error)
      return area
    }
  })
  
  const areasWithStats = await Promise.all(statisticsPromises)
  areas.value = areasWithStats
}

// 获取区域数据
const fetchAreas = async () => {
  areasLoading.value = true
  try {
    const response = await getAreaList()
    if (response.code === 200 && response.data) {
      // 只显示启用的区域，并且限制显示数量（比如最多显示4个）
      areas.value = response.data
        .filter(area => area.status === 1)
        .slice(0, 4) // 首页只显示前4个区域
        .map(area => ({
          id: area.id,
          name: area.name,
          description: area.description,
          status: area.status,
          sortOrder: area.sortOrder,
          totalComputers: 0, // 初始值，稍后更新
          availableComputers: 0 // 初始值，稍后更新
        }))
      
      // 按排序字段排序
      areas.value.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
      
      // 获取每个区域的统计信息
      await fetchAreaStatistics()
    }
  } catch (error) {
    console.error('获取区域列表失败:', error)
    showToast('获取区域列表失败')
  } finally {
    areasLoading.value = false
  }
}

// 页面加载时获取数据
onMounted(() => {
  fetchAreas()
})
</script>

<style scoped lang="scss">
.mobile-home {
  padding-bottom: 20px;
}

.user-card {
  margin-bottom: 12px;
}

.user-info {
  display: flex;
  align-items: center;
}

.user-detail {
  margin-left: 12px;
}

.username {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 4px;
}

.balance {
  font-size: 14px;
  color: #666;
}

.areas-section {
  margin-top: 20px;
  
  .section-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 12px;
    padding: 0 4px;
    
    h3 {
      margin: 0;
      font-size: 16px;
    }
  }
}

.area-card {
  text-align: left;
  
  &.disabled {
    opacity: 0.6;
  }
  
  .area-info {
    padding: 8px 0;
    
    .area-header {
      display: flex;
      justify-content: space-between;
      align-items: flex-start;
      margin-bottom: 4px;
    }
    
    .area-name {
      font-size: 14px;
      font-weight: bold;
      margin-bottom: 2px;
    }
    
    .area-desc {
      font-size: 12px;
      color: #666;
      margin-bottom: 6px;
      line-height: 1.3;
      display: -webkit-box;
      -webkit-box-orient: vertical;
      overflow: hidden;
    }
    
    .area-stats {
      display: flex;
      justify-content: space-between;
      
      .stat-item {
        display: flex;
        align-items: center;
        font-size: 11px;
        color: #969799;
        
        .van-icon {
          margin-right: 2px;
        }
        
        &.available {
          color: #07c160;
        }
      }
    }
  }
}
</style>