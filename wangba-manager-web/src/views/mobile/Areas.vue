<template>
  <div class="areas-page">
    <!-- 页面标题 -->
    <van-nav-bar
      title="选择区域"
      left-text="返回"
      left-arrow
      @click-left="$router.back()"
    />

    <!-- 搜索框 -->
    <div class="search-box">
      <van-search
        v-model="searchKeyword"
        placeholder="搜索区域名称"
        @search="onSearch"
        @clear="onSearch"
      />
    </div>

    <!-- 区域列表 -->
    <div class="areas-list">
      <div 
        v-for="area in filteredAreas" 
        :key="area.id"
        class="area-item"
        @click="handleAreaSelect(area)"
      >
        <van-image
          width="100%"
          height="120"
          fit="cover"
          :src="getAreaImage(area)"
          radius="4"
        />
        <div class="area-content">
          <div class="area-header">
            <h3 class="area-name">{{ area.name }}</h3>
            <van-tag :type="getAreaStatusType(area)" size="medium">
              {{ getAreaStatusText(area) }}
            </van-tag>
          </div>
          <p class="area-desc">{{ area.description || '暂无描述' }}</p>
          <div class="area-stats">
            <span class="stat-item">
              <van-icon name="desktop-o" />
              {{ area.totalComputers || 0 }}台电脑
            </span>
            <span class="stat-item">
              <van-icon name="success" />
              {{ area.availableComputers || 0 }}台可用
            </span>
            <span class="stat-item price">
              <van-icon name="gold-coin-o" />
              {{ getPriceRange(area) }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 加载状态 -->
    <van-loading v-if="loading" size="24px" vertical>加载中...</van-loading>

    <!-- 空状态 -->
    <van-empty 
      v-if="!loading && filteredAreas.length === 0" 
      :description="searchKeyword ? '未找到相关区域' : '暂无区域数据'" 
    />

    <!-- 错误状态 -->
    <div v-if="error" class="error-message">
      <van-empty description="加载失败，请重试">
        <van-button type="primary" @click="fetchAreas">重新加载</van-button>
      </van-empty>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'

// 导入本地图片
import areaImage101 from '@/assets/images/101.jpg'
import areaImage102 from '@/assets/images/102.jpg'
import areaImage103 from '@/assets/images/103.jpg'

// API
import { getAreaList } from '@/api/area'
import { getComputersByArea } from '@/api/computer'

const router = useRouter()
const loading = ref(false)
const error = ref(false)
const searchKeyword = ref('')

// 区域数据 - 从后端获取
const areas = ref([])

// 图片数组
const areaImages = [areaImage101, areaImage102, areaImage103]

// 过滤后的区域列表
const filteredAreas = computed(() => {
  if (!searchKeyword.value) {
    return areas.value
  }
  return areas.value.filter(area => 
    area.name && area.name.toLowerCase().includes(searchKeyword.value.toLowerCase())
  )
})

// 获取区域图片 - 根据区域ID选择对应的图片
const getAreaImage = (area) => {
  // 使用区域ID对图片数量取模，确保每个区域都有对应的图片
  const imageIndex = (area.id - 1) % areaImages.length
  return areaImages[imageIndex]
}

// 获取区域状态标签类型
const getAreaStatusType = (area) => {
  // 根据你的数据库 status: 1-启用 2-禁用
  if (area.status === 1) {
    return 'success'
  } else if (area.status === 2) {
    return 'danger'
  }
  return 'default'
}

// 获取区域状态文本
const getAreaStatusText = (area) => {
  if (area.status === 1) {
    return '启用'
  } else if (area.status === 2) {
    return '禁用'
  }
  return '未知'
}

// 获取价格范围
const getPriceRange = (area) => {
  if (area.minPrice && area.maxPrice) {
    if (area.minPrice === area.maxPrice) {
      return `¥${area.minPrice}/小时`
    }
    return `¥${area.minPrice}-${area.maxPrice}/小时`
  }
  return '价格待定'
}

// 搜索处理
const onSearch = () => {
  // 搜索逻辑已在 computed 中处理
}

// 区域选择处理
const handleAreaSelect = (area) => {
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
        
        // 计算价格范围
        const prices = computers.map(computer => parseFloat(computer.pricePerHour) || 0)
        const minPrice = prices.length > 0 ? Math.min(...prices) : 0
        const maxPrice = prices.length > 0 ? Math.max(...prices) : 0
        
        return {
          ...area,
          totalComputers,
          availableComputers,
          minPrice,
          maxPrice
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

// 获取区域列表
const fetchAreas = async () => {
  loading.value = true
  error.value = false
  try {
    const response = await getAreaList()
    
    if (response.code === 200 && response.data) {
      // 过滤掉禁用的区域，只显示启用的区域
      areas.value = response.data
        .filter(area => area.status === 1) // 只显示启用的区域
        .map(area => ({
          id: area.id,
          name: area.name,
          description: area.description,
          status: area.status,
          sortOrder: area.sortOrder,
          createTime: area.createTime,
          updateTime: area.updateTime,
          totalComputers: 0, // 初始值，稍后更新
          availableComputers: 0, // 初始值，稍后更新
          minPrice: 0, // 初始值，稍后更新
          maxPrice: 0 // 初始值，稍后更新
        }))
      
      // 按排序字段排序
      areas.value.sort((a, b) => (a.sortOrder || 0) - (b.sortOrder || 0))
      
      // 获取每个区域的统计信息
      await fetchAreaStatistics()
    } else {
      throw new Error(response.message || '获取数据失败')
    }
  } catch (err) {
    console.error('获取区域列表失败:', err)
    error.value = true
    showToast('获取区域列表失败')
    
    // 开发环境下可以使用模拟数据
    if (process.env.NODE_ENV === 'development') {
      areas.value = getMockAreas()
      showToast('使用模拟数据')
    }
  } finally {
    loading.value = false
  }
}

// 模拟数据（仅开发环境使用）
const getMockAreas = () => {
  return [
    {
      id: 1,
      name: '大厅区域',
      description: '普通上网区域，环境舒适，价格实惠',
      status: 1,
      sortOrder: 1,
      createTime: '2023-01-01 00:00:00',
      updateTime: '2023-01-01 00:00:00',
      totalComputers: 20,
      availableComputers: 8,
      minPrice: 5,
      maxPrice: 8
    },
    {
      id: 2,
      name: 'VIP包厢',
      description: '独立包厢环境，私密性好，配置高端',
      status: 1,
      sortOrder: 2,
      createTime: '2023-01-01 00:00:00',
      updateTime: '2023-01-01 00:00:00',
      totalComputers: 10,
      availableComputers: 4,
      minPrice: 15,
      maxPrice: 20
    },
    {
      id: 3,
      name: '竞技区',
      description: '专业电竞设备，高速网络，游戏体验极佳',
      status: 1,
      sortOrder: 3,
      createTime: '2023-01-01 00:00:00',
      updateTime: '2023-01-01 00:00:00',
      totalComputers: 15,
      availableComputers: 6,
      minPrice: 12,
      maxPrice: 18
    }
  ]
}

// 页面加载时获取数据
onMounted(() => {
  fetchAreas()
})
</script>

<style scoped lang="scss">
.areas-page {
  background-color: #f7f8fa;
  min-height: 100vh;
}

.search-box {
  background: white;
  padding: 8px 12px;
}

.areas-list {
  padding: 12px;
}

.area-item {
  background: white;
  border-radius: 8px;
  margin-bottom: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.05);
  
  &:active {
    background: #f5f5f5;
  }
}

.area-content {
  padding: 12px;
  
  .area-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 8px;
    
    .area-name {
      margin: 0;
      font-size: 16px;
      font-weight: bold;
      color: #323233;
    }
  }
  
  .area-desc {
    margin: 0 0 12px 0;
    font-size: 13px;
    color: #646566;
    line-height: 1.4;
  }
  
  .area-stats {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    
    .stat-item {
      display: flex;
      align-items: center;
      font-size: 12px;
      color: #969799;
      
      .van-icon {
        margin-right: 4px;
        font-size: 14px;
      }
      
      &.price {
        color: #ee0a24;
        font-weight: 500;
        
        .van-icon {
          color: #ee0a24;
        }
      }
    }
  }
}

.error-message {
  text-align: center;
  padding: 20px;
}
</style>