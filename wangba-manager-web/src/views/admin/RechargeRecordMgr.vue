<template>
  <div class="recharge-record-page">
    <div class="page-header">
      <h2>充值记录管理</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索订单号、用户ID或支付方式"
          style="width: 200px; margin-right: 10px;"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-select
          v-model="searchStatus"
          placeholder="选择状态"
          style="width: 120px; margin-right: 10px;"
          clearable
        >
          <el-option label="全部" value="" />
          <el-option label="成功" value="1" />
          <el-option label="失败" value="2" />
          <el-option label="处理中" value="3" />
        </el-select>
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button @click="handleReset">
          <el-icon><Refresh /></el-icon>
          重置
        </el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="filteredRecords" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="记录ID" width="80" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="orderId" label="订单号" width="200">
          <template #default="{ row }">
            <el-tooltip :content="row.orderId" placement="top">
              <span class="order-id">{{ formatOrderId(row.orderId) }}</span>
            </el-tooltip>
          </template>
        </el-table-column>
        <el-table-column prop="amount" label="充值金额" width="120">
          <template #default="{ row }">
            <span class="amount">¥{{ row.amount }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="paymentMethod" label="支付方式" width="120">
          <template #default="{ row }">
            <el-tag :type="getPaymentMethodType(row.paymentMethod)" size="small">
              {{ getPaymentMethodLabel(row.paymentMethod) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small">
              {{ getStatusLabel(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.updateTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleViewDetail(row)">
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="totalRecords"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 查看详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="充值记录详情"
      width="500px"
    >
      <el-descriptions :column="1" border>
        <el-descriptions-item label="记录ID">
          {{ currentRecord.id }}
        </el-descriptions-item>
        <el-descriptions-item label="用户ID">
          {{ currentRecord.userId }}
        </el-descriptions-item>
        <el-descriptions-item label="订单号">
          {{ currentRecord.orderId }}
        </el-descriptions-item>
        <el-descriptions-item label="充值金额">
          <span class="amount">¥{{ currentRecord.amount }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="支付方式">
          <el-tag :type="getPaymentMethodType(currentRecord.paymentMethod)">
            {{ getPaymentMethodLabel(currentRecord.paymentMethod) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(currentRecord.status)">
            {{ getStatusLabel(currentRecord.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatTime(currentRecord.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatTime(currentRecord.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button type="primary" @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getRechargeRecords } from '../../api/recharge.js'
import { Search, Refresh } from '@element-plus/icons-vue'

const loading = ref(false)
const detailDialogVisible = ref(false)
const searchKeyword = ref('')
const searchStatus = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const totalRecords = ref(0)

const rechargeRecords = ref([])
const currentRecord = ref({})

// 支付方式映射
const paymentMethods = {
  alipay: { label: '支付宝', type: 'primary' },
  wechat: { label: '微信支付', type: 'success' },
  bank: { label: '银行卡', type: 'warning' }
}

// 状态映射
const statusMap = {
  1: { label: '成功', type: 'success' },
  2: { label: '失败', type: 'danger' },
  3: { label: '处理中', type: 'warning' }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// 格式化订单号显示
const formatOrderId = (orderId) => {
  if (!orderId) return ''
  if (orderId.length > 15) {
    return orderId.substring(0, 8) + '...' + orderId.substring(orderId.length - 8)
  }
  return orderId
}

// 获取支付方式标签
const getPaymentMethodLabel = (method) => {
  return paymentMethods[method]?.label || method
}

// 获取支付方式类型
const getPaymentMethodType = (method) => {
  return paymentMethods[method]?.type || 'info'
}

// 获取状态标签
const getStatusLabel = (status) => {
  return statusMap[status]?.label || '未知'
}

// 获取状态类型
const getStatusType = (status) => {
  return statusMap[status]?.type || 'info'
}

// 获取充值记录列表
const fetchRechargeRecords = async () => {
  loading.value = true
  try {
    const response = await getRechargeRecords()
    if (response.code === 200) {
      rechargeRecords.value = response.data
      totalRecords.value = response.data.length
    } else {
      ElMessage.error('获取充值记录失败')
    }
  } catch (error) {
    ElMessage.error('获取充值记录失败')
    console.error('获取充值记录错误:', error)
  } finally {
    loading.value = false
  }
}

// 搜索充值记录
const filteredRecords = computed(() => {
  let records = rechargeRecords.value
  
  // 关键词搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    records = records.filter(record => 
      record.orderId.toLowerCase().includes(keyword) || 
      record.userId.toString().includes(keyword) ||
      (record.paymentMethod && record.paymentMethod.toLowerCase().includes(keyword))
    )
  }
  
  // 状态筛选
  if (searchStatus.value) {
    records = records.filter(record => record.status.toString() === searchStatus.value)
  }
  
  totalRecords.value = records.length
  
  // 分页
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return records.slice(start, end)
})

const handleSearch = () => {
  currentPage.value = 1
  ElMessage.success(`搜索完成，找到 ${filteredRecords.value.length} 条结果`)
}

const handleReset = () => {
  searchKeyword.value = ''
  searchStatus.value = ''
  currentPage.value = 1
  fetchRechargeRecords()
}

// 查看详情
const handleViewDetail = (record) => {
  currentRecord.value = record
  detailDialogVisible.value = true
}

// 分页处理
const handleSizeChange = (newSize) => {
  pageSize.value = newSize
  currentPage.value = 1
}

const handleCurrentChange = (newPage) => {
  currentPage.value = newPage
}

onMounted(() => {
  fetchRechargeRecords()
})
</script>

<style scoped lang="scss">
.recharge-record-page {
  .page-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 20px;
    
    h2 {
      margin: 0;
      color: #303133;
    }
    
    .header-actions {
      display: flex;
      align-items: center;
    }
  }

  .order-id {
    font-family: 'Courier New', monospace;
    font-size: 12px;
    cursor: pointer;
  }

  .amount {
    color: #e6a23c;
    font-weight: bold;
    font-size: 14px;
  }

  .pagination-container {
    display: flex;
    justify-content: flex-end;
    margin-top: 20px;
  }
}

:deep(.el-descriptions__label) {
  width: 100px;
  font-weight: bold;
}
</style>