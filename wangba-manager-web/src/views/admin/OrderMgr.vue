<template>
  <div class="order-mgr-page">
    <div class="page-header">
      <h2>上机记录管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="loadSessions" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-card>
      <!-- 搜索筛选区域 -->
      <div class="filter-container">
        <el-form :model="filterForm" :inline="true" class="demo-form-inline">
          <el-form-item label="用户ID">
            <el-input
              v-model="filterForm.userId"
              placeholder="请输入用户ID"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
              style="width: 150px"
            />
          </el-form-item>
          
          <el-form-item label="状态">
            <el-select 
              v-model="filterForm.status" 
              placeholder="全部状态" 
              clearable
              @change="handleSearch"
              style="width: 120px"
            >
              <el-option label="进行中" value="1" />
              <el-option label="已结束" value="2" />
              <el-option label="异常" value="3" />
            </el-select>
          </el-form-item>
          
          <el-form-item>
            <el-button type="primary" @click="handleSearch" :loading="loading">
              <el-icon><Search /></el-icon>
              搜索
            </el-button>
            <el-button @click="handleReset">
              <el-icon><Refresh /></el-icon>
              重置
            </el-button>
          </el-form-item>
        </el-form>
        
        <!-- 筛选条件显示 -->
        <div v-if="hasActiveFilters" class="active-filters">
          <span class="filters-label">当前筛选：</span>
          <el-tag
            v-if="filterForm.userId"
            closable
            @close="removeFilter('userId')"
          >
            用户ID: {{ filterForm.userId }}
          </el-tag>
          <el-tag
            v-if="filterForm.status"
            closable
            @close="removeFilter('status')"
            :type="getStatusType(filterForm.status)"
          >
            状态: {{ getStatusText(filterForm.status) }}
          </el-tag>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table 
        :data="sessionList" 
        v-loading="loading" 
        style="width: 100%"
        :default-sort="{ prop: 'startTime', order: 'descending' }"
        empty-text="暂无数据"
      >
        <el-table-column prop="id" label="会话ID" width="100" align="center" />
        <el-table-column prop="userId" label="用户ID" width="100" align="center" />
        <el-table-column prop="computerId" label="电脑ID" width="100" align="center" />
        <el-table-column prop="startTime" label="开始时间" width="180" sortable>
          <template #default="{ row }">
            {{ formatDateTime(row.startTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="endTime" label="结束时间" width="180">
          <template #default="{ row }">
            {{ row.endTime ? formatDateTime(row.endTime) : '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="duration" label="时长" width="120" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'warning' : 'info'">
              {{ calculateDuration(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="totalAmount" label="金额" width="120" align="right">
          <template #default="{ row }">
            <span v-if="row.totalAmount && row.totalAmount > 0" class="amount-text">
              ¥{{ row.totalAmount }}
            </span>
            <span v-else class="amount-placeholder">-</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleView(row)">
              <el-icon><View /></el-icon>
              详情
            </el-button>
            <el-button 
              v-if="row.status === 1" 
              type="success" 
              link size="small" 
              @click="handleComplete(row)"
              :loading="completingId === row.id"
            >
              <el-icon><CircleCheck /></el-icon>
              结束
            </el-button>
            <el-tooltip v-else content="只有进行中的会话可以结束" placement="top">
              <el-button type="success" link size="small" disabled>
                <el-icon><CircleCheck /></el-icon>
                结束
              </el-button>
            </el-tooltip>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.pageSize"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, 
  Refresh, 
  View, 
  CircleCheck 
} from '@element-plus/icons-vue'
import { getSessionList, endSession } from '@/api/computerSession'

const loading = ref(false)
const completingId = ref(null)
const sessionList = ref([])

// 筛选表单
const filterForm = reactive({
  userId: '',
  status: ''
})

// 分页参数
const pagination = ref({
  page: 1,
  pageSize: 10,
  total: 0
})

// 状态映射
const statusMap = {
  1: { text: '进行中', type: 'warning' },
  2: { text: '已结束', type: 'success' },
  3: { text: '异常', type: 'danger' }
}

// 计算是否有激活的筛选条件
const hasActiveFilters = computed(() => {
  return filterForm.userId || filterForm.status
})

// 获取会话列表
const loadSessions = async () => {
  try {
    loading.value = true
    const params = {
      page: pagination.value.page,
      pageSize: pagination.value.pageSize
    }
    
    // 添加筛选条件
    if (filterForm.userId) {
      params.userId = parseInt(filterForm.userId) || filterForm.userId
    }
    if (filterForm.status) {
      params.status = parseInt(filterForm.status)
    }

    console.log('请求参数:', params) // 调试用

    const response = await getSessionList(params)
    
    if (response.code === 200) {
      sessionList.value = response.data
      pagination.value.total = response.pagination?.total || 0
      ElMessage.success(`加载成功，共 ${sessionList.value.length} 条记录`)
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取会话列表失败:', error)
    ElMessage.error('获取数据失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  pagination.value.page = 1
  loadSessions()
}

// 重置
const handleReset = () => {
  filterForm.userId = ''
  filterForm.status = ''
  pagination.value.page = 1
  loadSessions()
}

// 移除单个筛选条件
const removeFilter = (filterKey) => {
  filterForm[filterKey] = ''
  handleSearch()
}

// 结束上机 - 真正调用后端接口
const handleComplete = async (session) => {
  try {
    completingId.value = session.id
    
    const result = await ElMessageBox.confirm(
      `确定要结束会话 ${session.id} 吗？系统将自动计算费用并从用户余额扣除。`,
      '结束上机确认',
      { 
        confirmButtonText: '确定结束', 
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    if (result) {
      // 调用后端结束上机接口
      const response = await endSession({
        sessionId: session.id
      })
      
      console.log('结束上机响应:', response) // 调试用
      
      if (response.code === 200) {
        ElMessage.success('上机已结束，费用已从用户余额扣除')
        // 重新加载数据以更新状态
        await loadSessions()
      } else {
        ElMessage.error(response.message || '结束上机失败')
      }
    }
  } catch (error) {
    if (error === 'cancel') {
      // 用户取消操作
      ElMessage.info('已取消操作')
    } else {
      console.error('结束上机失败:', error)
      ElMessage.error('结束上机失败: ' + (error.message || '网络错误'))
    }
  } finally {
    completingId.value = null
  }
}

// 查看详情
const handleView = async (session) => {
  ElMessage.info(`查看会话详情: ${session.id}`)
  // 这里可以打开详情弹窗或跳转到详情页面
  console.log('会话详情:', session)
}

// 计算时长
const calculateDuration = (session) => {
  if (!session.startTime) return '-'
  
  const start = new Date(session.startTime)
  const end = session.endTime ? new Date(session.endTime) : new Date()
  
  const duration = Math.floor((end - start) / (1000 * 60)) // 分钟数
  const hours = Math.floor(duration / 60)
  const minutes = duration % 60
  
  if (hours > 0) {
    return `${hours}时${minutes}分`
  } else {
    return `${minutes}分钟`
  }
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

// 状态相关方法
const getStatusType = (status) => {
  return statusMap[status]?.type || 'info'
}

const getStatusText = (status) => {
  return statusMap[status]?.text || '未知'
}

// 分页处理
const handleSizeChange = (newSize) => {
  pagination.value.pageSize = newSize
  pagination.value.page = 1
  loadSessions()
}

const handleCurrentChange = (newPage) => {
  pagination.value.page = newPage
  loadSessions()
}

onMounted(() => {
  loadSessions()
})
</script>

<style scoped lang="scss">
.order-mgr-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      color: #303133;
      font-weight: 600;
    }
    
    .header-actions {
      display: flex;
      gap: 10px;
    }
  }

  .filter-container {
    margin-bottom: 20px;
    padding: 20px;
    background: #f8f9fa;
    border-radius: 8px;
    
    .demo-form-inline {
      display: flex;
      flex-wrap: wrap;
      gap: 16px;
      align-items: flex-end;
      
      :deep(.el-form-item) {
        margin-bottom: 0;
      }
      
      :deep(.el-form-item__label) {
        font-weight: 500;
      }
    }
    
    .active-filters {
      margin-top: 12px;
      display: flex;
      align-items: center;
      flex-wrap: wrap;
      gap: 8px;
      
      .filters-label {
        font-size: 14px;
        color: #606266;
        font-weight: 500;
      }
      
      .el-tag {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
  }

  .amount-text {
    color: #f56c6c;
    font-weight: 600;
    font-size: 14px;
  }

  .amount-placeholder {
    color: #c0c4cc;
    font-size: 14px;
  }

  .pagination-container {
    margin-top: 20px;
    display: flex;
    justify-content: flex-end;
    padding: 16px 0;
  }

  // 表格样式优化
  :deep(.el-table) {
    .el-table__header {
      th {
        background-color: #f5f7fa;
        color: #606266;
        font-weight: 600;
      }
    }
    
    .el-table__row:hover {
      background-color: #f5f7fa;
    }
  }
}

// 响应式设计
@media (max-width: 768px) {
  .order-mgr-page {
    .filter-container {
      .demo-form-inline {
        flex-direction: column;
        align-items: stretch;
        
        .el-form-item {
          width: 100%;
        }
      }
    }
  }
}
</style>