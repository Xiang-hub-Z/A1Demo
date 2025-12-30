<template>
  <div class="computer-mgr-page">
    <div class="page-header">
      <h2>电脑管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddComputer">
          <el-icon><Plus /></el-icon>
          添加电脑
        </el-button>
        <el-button @click="loadComputers" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-card>
      <!-- 搜索筛选区域 -->
      <div class="filter-container">
        <el-form :model="filterForm" :inline="true" class="demo-form-inline">
          <el-form-item label="电脑编号">
            <el-input
              v-model="filterForm.computerNo"
              placeholder="请输入电脑编号"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
              style="width: 150px"
            />
          </el-form-item>
          
          <el-form-item label="电脑名称">
            <el-input
              v-model="filterForm.name"
              placeholder="请输入电脑名称"
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
              <el-option label="空闲" value="1" />
              <el-option label="使用中" value="2" />
              <el-option label="维护中" value="3" />
            </el-select>
          </el-form-item>
          
          <el-form-item label="区域">
            <el-select 
              v-model="filterForm.areaId" 
              placeholder="全部区域" 
              clearable
              @change="handleSearch"
              style="width: 150px"
            >
              <el-option 
                v-for="area in areas" 
                :key="area.id" 
                :label="area.name" 
                :value="area.id" 
              />
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
            v-if="filterForm.computerNo"
            closable
            @close="removeFilter('computerNo')"
          >
            电脑编号: {{ filterForm.computerNo }}
          </el-tag>
          <el-tag
            v-if="filterForm.name"
            closable
            @close="removeFilter('name')"
          >
            电脑名称: {{ filterForm.name }}
          </el-tag>
          <el-tag
            v-if="filterForm.status"
            closable
            @close="removeFilter('status')"
            :type="getStatusType(filterForm.status)"
          >
            状态: {{ getStatusText(filterForm.status) }}
          </el-tag>
          <el-tag
            v-if="filterForm.areaId"
            closable
            @close="removeFilter('areaId')"
          >
            区域: {{ getAreaName(filterForm.areaId) }}
          </el-tag>
        </div>
      </div>

      <!-- 数据表格 -->
      <el-table 
        :data="filteredComputers" 
        v-loading="loading" 
        style="width: 100%"
        :default-sort="{ prop: 'computerNo', order: 'ascending' }"
        empty-text="暂无数据"
      >
        <el-table-column prop="id" label="电脑ID" width="100" align="center" />
        <el-table-column prop="computerNo" label="电脑编号" width="120" align="center" />
        <el-table-column prop="name" label="电脑名称" min-width="120" />
        <el-table-column prop="areaId" label="区域" width="100" align="center">
          <template #default="{ row }">
            {{ getAreaName(row.areaId) }}
          </template>
        </el-table-column>
        <el-table-column prop="ipAddress" label="IP地址" width="130" align="center" />
        <el-table-column prop="configuration" label="配置信息" min-width="200" show-overflow-tooltip />
        <el-table-column prop="pricePerHour" label="价格/小时" width="120" align="right">
          <template #default="{ row }">
            <span class="price-text">¥{{ row.pricePerHour }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              <el-icon><Edit /></el-icon>
              编辑
            </el-button>
            <el-button 
              type="warning" 
              link size="small" 
              @click="handleToggleStatus(row)"
              :loading="updatingId === row.id"
            >
              <el-icon><Switch /></el-icon>
              {{ row.status === 3 ? '恢复' : '维护' }}
            </el-button>
            <el-button 
              type="danger" 
              link size="small" 
              @click="handleDelete(row)"
              :loading="deletingId === row.id"
            >
              <el-icon><Delete /></el-icon>
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑电脑弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="computerFormRef"
        :model="computerForm"
        :rules="computerRules"
        label-width="100px"
      >
        <el-form-item label="电脑编号" prop="computerNo">
          <el-input v-model="computerForm.computerNo" placeholder="请输入电脑编号" />
        </el-form-item>
        <el-form-item label="电脑名称" prop="name">
          <el-input v-model="computerForm.name" placeholder="请输入电脑名称" />
        </el-form-item>
        <el-form-item label="所属区域" prop="areaId">
          <el-select v-model="computerForm.areaId" placeholder="请选择区域" style="width: 100%">
            <el-option 
              v-for="area in areas" 
              :key="area.id" 
              :label="area.name" 
              :value="area.id" 
            />
          </el-select>
        </el-form-item>
        <el-form-item label="IP地址" prop="ipAddress">
          <el-input v-model="computerForm.ipAddress" placeholder="请输入IP地址" />
        </el-form-item>
        <el-form-item label="配置信息" prop="configuration">
          <el-input
            v-model="computerForm.configuration"
            type="textarea"
            :rows="3"
            placeholder="请输入电脑配置信息"
          />
        </el-form-item>
        <el-form-item label="价格/小时" prop="pricePerHour">
          <el-input-number
            v-model="computerForm.pricePerHour"
            :min="0"
            :max="100"
            :precision="2"
            :step="0.5"
            controls-position="right"
            style="width: 200px"
          />
          <span style="margin-left: 8px; color: #666;">元</span>
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="isEdit">
          <el-radio-group v-model="computerForm.status">
            <el-radio :label="1">空闲</el-radio>
            <el-radio :label="2">使用中</el-radio>
            <el-radio :label="3">维护中</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, 
  Refresh, 
  Search, 
  Edit, 
  Delete, 
  Switch 
} from '@element-plus/icons-vue'
import { getComputerList, addComputer, updateComputer, deleteComputer } from '@/api/computer'
import { getAreaList } from '@/api/area'

const loading = ref(false)
const submitting = ref(false)
const updatingId = ref(null)
const deletingId = ref(null)
const dialogVisible = ref(false)
const computerFormRef = ref()
const isEdit = ref(false)

const computers = ref([])
const areas = ref([])

// 筛选表单
const filterForm = reactive({
  computerNo: '',
  name: '',
  status: '',
  areaId: ''
})

// 电脑表单
const computerForm = reactive({
  id: null,
  computerNo: '',
  name: '',
  areaId: null,
  ipAddress: '',
  configuration: '',
  pricePerHour: 5.00,
  status: 1
})

// 表单验证规则
const computerRules = {
  computerNo: [
    { required: true, message: '请输入电脑编号', trigger: 'blur' },
    { min: 2, max: 20, message: '电脑编号长度为2-20个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入电脑名称', trigger: 'blur' },
    { min: 2, max: 50, message: '电脑名称长度为2-50个字符', trigger: 'blur' }
  ],
  areaId: [
    { required: true, message: '请选择所属区域', trigger: 'change' }
  ],
  ipAddress: [
    { required: true, message: '请输入IP地址', trigger: 'blur' }
  ],
  configuration: [
    { required: true, message: '请输入配置信息', trigger: 'blur' },
    { min: 5, max: 200, message: '配置信息长度为5-200个字符', trigger: 'blur' }
  ],
  pricePerHour: [
    { required: true, message: '请输入价格', trigger: 'blur' },
    { type: 'number', min: 0, message: '价格不能为负数', trigger: 'blur' }
  ]
}

// 计算属性
const hasActiveFilters = computed(() => {
  return filterForm.computerNo || filterForm.name || filterForm.status || filterForm.areaId
})

const filteredComputers = computed(() => {
  return computers.value.filter(computer => {
    const computerNoMatch = !filterForm.computerNo || 
      computer.computerNo.toLowerCase().includes(filterForm.computerNo.toLowerCase())
    const nameMatch = !filterForm.name || 
      computer.name.toLowerCase().includes(filterForm.name.toLowerCase())
    const statusMatch = !filterForm.status || 
      computer.status.toString() === filterForm.status
    const areaMatch = !filterForm.areaId || 
      computer.areaId.toString() === filterForm.areaId
    return computerNoMatch && nameMatch && statusMatch && areaMatch
  })
})

const dialogTitle = computed(() => {
  return isEdit.value ? '编辑电脑' : '添加电脑'
})

// 状态映射
const statusMap = {
  1: { text: '空闲', type: 'success' },
  2: { text: '使用中', type: 'warning' },
  3: { text: '维护中', type: 'danger' }
}

// 获取电脑列表
const loadComputers = async () => {
  try {
    loading.value = true
    const response = await getComputerList()
    
    if (response.code === 200) {
      computers.value = response.data
      ElMessage.success(`加载成功，共 ${computers.value.length} 台电脑`)
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取电脑列表失败:', error)
    ElMessage.error('获取数据失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 获取区域列表
const loadAreas = async () => {
  try {
    const response = await getAreaList()
    if (response.code === 200) {
      areas.value = response.data
    }
  } catch (error) {
    console.error('获取区域列表失败:', error)
  }
}

// 搜索
const handleSearch = () => {
  // 前端筛选，不需要重新请求
}

// 重置
const handleReset = () => {
  filterForm.computerNo = ''
  filterForm.name = ''
  filterForm.status = ''
  filterForm.areaId = ''
}

// 移除单个筛选条件
const removeFilter = (filterKey) => {
  filterForm[filterKey] = ''
}

// 添加电脑
const handleAddComputer = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑电脑
const handleEdit = (computer) => {
  isEdit.value = true
  resetForm()
  Object.assign(computerForm, { 
    ...computer,
    pricePerHour: parseFloat(computer.pricePerHour)
  })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await computerFormRef.value.validate()
    submitting.value = true

    let response
    if (isEdit.value) {
      response = await updateComputer(computerForm)
    } else {
      response = await addComputer(computerForm)
    }

    console.log('后端响应:', response)

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      await loadComputers()
    } else {
      // 处理后端返回的错误信息
      let errorMessage = '操作失败'
      
      if (response.description) {
        errorMessage = Array.isArray(response.description) 
          ? response.description[0]?.message || response.description[0] || '数据验证失败'
          : response.description
      } else if (response.data && response.data.description) {
        errorMessage = Array.isArray(response.data.description) 
          ? response.data.description[0]?.message || response.data.description[0] || '数据验证失败'
          : response.data.description
      } else if (response.message) {
        errorMessage = response.message
      }
      
      ElMessage.error(errorMessage)
    }
  } catch (error) {
    console.error('提交错误详情:', error)
    if (error.errors) {
      ElMessage.warning('请完善表单信息')
    } else {
      ElMessage.error('操作失败: ' + (error.message || '网络错误'))
    }
  } finally {
    submitting.value = false
  }
}

// 切换维护状态
const handleToggleStatus = async (computer) => {
  try {
    updatingId.value = computer.id
    const action = computer.status === 3 ? '恢复' : '维护'
    const newStatus = computer.status === 3 ? 1 : 3
    
    await ElMessageBox.confirm(
      `确定要${action}电脑 ${computer.computerNo} 吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    const updateData = {
      ...computer,
      status: newStatus
    }
    
    const response = await updateComputer(updateData)
    if (response.code === 200) {
      ElMessage.success(`${action}成功`)
      await loadComputers()
    } else {
      ElMessage.error(response.message || `${action}失败`)
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('切换状态失败:', error)
      ElMessage.error('操作失败: ' + (error.message || '网络错误'))
    }
  } finally {
    updatingId.value = null
  }
}

// 删除电脑
const handleDelete = async (computer) => {
  try {
    deletingId.value = computer.id
    
    await ElMessageBox.confirm(
      `确定要删除电脑 ${computer.computerNo} 吗？此操作不可恢复！`,
      '警告',
      { 
        confirmButtonText: '确定删除', 
        cancelButtonText: '取消', 
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    const response = await deleteComputer(computer.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      await loadComputers()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败: ' + (error.message || '网络错误'))
    }
  } finally {
    deletingId.value = null
  }
}

// 重置表单
const resetForm = () => {
  if (computerFormRef.value) {
    computerFormRef.value.resetFields()
  }
  Object.assign(computerForm, {
    id: null,
    computerNo: '',
    name: '',
    areaId: null,
    ipAddress: '',
    configuration: '',
    pricePerHour: 5.00,
    status: 1
  })
}

// 状态相关方法
const getStatusType = (status) => {
  return statusMap[status]?.type || 'info'
}

const getStatusText = (status) => {
  return statusMap[status]?.text || '未知'
}

// 获取区域名称
const getAreaName = (areaId) => {
  const area = areas.value.find(a => a.id === areaId)
  return area ? area.name : '-'
}

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

onMounted(() => {
  loadComputers()
  loadAreas()
})
</script>

<style scoped lang="scss">
.computer-mgr-page {
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

  .price-text {
    color: #f56c6c;
    font-weight: 600;
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
  .computer-mgr-page {
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