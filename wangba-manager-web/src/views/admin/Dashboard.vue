<template>
  <div class="dashboard-page">
    <el-row :gutter="20">
      <!-- 统计卡片 -->
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon user-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.totalUsers }}</div>
              <div class="stats-label">总用户数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon computer-icon">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.totalComputers }}</div>
              <div class="stats-label">电脑总数</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon revenue-icon">
              <el-icon><Money /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">¥{{ stats.totalRecharge }}</div>
              <div class="stats-label">总充值收入</div>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="6">
        <el-card class="stats-card">
          <div class="stats-content">
            <div class="stats-icon order-icon">
              <el-icon><List /></el-icon>
            </div>
            <div class="stats-info">
              <div class="stats-value">{{ stats.activeSessions }}</div>
              <div class="stats-label">进行中订单</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="12">
        <el-card header="上机收入统计">
          <div class="revenue-stats">
            <div class="revenue-item">
              <span class="label">总充值收入</span>
              <span class="value recharge">¥{{ stats.totalRecharge }}</span>
            </div>
            <div class="revenue-item">
              <span class="label">总上机收入</span>
              <span class="value session">¥{{ stats.totalSessionRevenue }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card header="电脑使用状态">
          <div class="computer-status">
            <div class="status-item">
              <span class="label">空闲电脑</span>
              <span class="value free">{{ computerStatus.free }}</span>
            </div>
            <div class="status-item">
              <span class="label">使用中</span>
              <span class="value using">{{ computerStatus.using }}</span>
            </div>
            <div class="status-item">
              <span class="label">维护中</span>
              <span class="value maintenance">{{ computerStatus.maintenance }}</span>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { User, Monitor, Money, List } from '@element-plus/icons-vue'
import { getDashboardStats, getComputerStatus } from '@/api/dashboard'

const stats = reactive({
  totalUsers: 0,
  totalComputers: 0,
  totalRecharge: 0,
  totalSessionRevenue: 0,
  activeSessions: 0
})

const computerStatus = reactive({
  free: 0,
  using: 0,
  maintenance: 0
})

// 加载统计数据
const loadDashboardData = async () => {
  try {
    // 加载主要统计数据
    const statsResponse = await getDashboardStats()
    if (statsResponse.code === 200) {
      Object.assign(stats, statsResponse.data)
    }
    
    // 加载电脑状态
    const statusResponse = await getComputerStatus()
    if (statusResponse.code === 200) {
      Object.assign(computerStatus, statusResponse.data)
    }
  } catch (error) {
    console.error('加载仪表盘数据失败:', error)
  }
}

onMounted(() => {
  loadDashboardData()
})
</script>

<style scoped lang="scss">
.dashboard-page {
  padding: 0;
}

.stats-card {
  margin-bottom: 0;
  
  .stats-content {
    display: flex;
    align-items: center;
  }
  
  .stats-icon {
    width: 60px;
    height: 60px;
    border-radius: 50%;
    display: flex;
    align-items: center;
    justify-content: center;
    margin-right: 15px;
    font-size: 24px;
    color: white;
    
    &.user-icon {
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    }
    
    &.computer-icon {
      background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    }
    
    &.revenue-icon {
      background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
    }
    
    &.order-icon {
      background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
    }
  }
  
  .stats-info {
    flex: 1;
    
    .stats-value {
      font-size: 24px;
      font-weight: bold;
      margin-bottom: 4px;
    }
    
    .stats-label {
      color: #909399;
      font-size: 14px;
    }
  }
}

.computer-status,
.revenue-stats {
  .status-item,
  .revenue-item {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 0;
    border-bottom: 1px solid #f0f0f0;
    
    &:last-child {
      border-bottom: none;
    }
    
    .label {
      color: #606266;
    }
    
    .value {
      font-weight: bold;
      font-size: 18px;
      
      &.free {
        color: #67c23a;
      }
      
      &.using {
        color: #e6a23c;
      }
      
      &.maintenance {
        color: #f56c6c;
      }
      
      &.recharge {
        color: #409eff;
      }
      
      &.session {
        color: #67c23a;
      }
    }
  }
}
</style>