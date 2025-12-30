<template>
  <div class="area-mgr-page">
    <div class="page-header">
      <h2>区域管理</h2>
      <div class="header-actions">
        <el-button type="primary" @click="handleAddArea">
          <el-icon><Plus /></el-icon>
          添加区域
        </el-button>
        <el-button @click="loadAreas" :loading="loading">
          <el-icon><Refresh /></el-icon>
          刷新
        </el-button>
      </div>
    </div>

    <el-card>
      <!-- 搜索筛选区域 -->
      <div class="filter-container">
        <el-form :model="filterForm" :inline="true" class="demo-form-inline">
          <el-form-item label="区域名称">
            <el-input
              v-model="filterForm.name"
              placeholder="请输入区域名称"
              clearable
              @keyup.enter="handleSearch"
              @clear="handleSearch"
              style="width: 200px"
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
              <el-option label="启用" value="1" />
              <el-option label="禁用" value="2" />
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
            v-if="filterForm.name"
            closable
            @close="removeFilter('name')"
          >
            区域名称: {{ filterForm.name }}
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
        :data="filteredAreas" 
        v-loading="loading" 
        style="width: 100%"
        :default-sort="{ prop: 'sortOrder', order: 'ascending' }"
        empty-text="暂无数据"
      >
        <el-table-column prop="id" label="区域ID" width="100" align="center" />
        <el-table-column prop="name" label="区域名称" min-width="150" />
        <el-table-column prop="description" label="区域描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="sortOrder" label="排序" width="100" align="center" sortable />
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
        <el-table-column prop="updateTime" label="更新时间" width="180">
          <template #default="{ row }">
            {{ formatDateTime(row.updateTime) }}
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
              {{ row.status === 1 ? '禁用' : '启用' }}
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

    <!-- 添加/编辑区域弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="areaFormRef"
        :model="areaForm"
        :rules="areaRules"
        label-width="80px"
      >
        <el-form-item label="区域名称" prop="name">
          <el-input v-model="areaForm.name" placeholder="请输入区域名称" />
        </el-form-item>
        <el-form-item label="区域描述" prop="description">
          <el-input
            v-model="areaForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入区域描述"
          />
        </el-form-item>
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number
            v-model="areaForm.sortOrder"
            :min="1"
            :max="999"
            controls-position="right"
          />
        </el-form-item>
        <el-form-item label="状态" prop="status" v-if="isEdit">
          <el-radio-group v-model="areaForm.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="2">禁用</el-radio>
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
import { getAreaList, addArea, updateArea, deleteArea } from '@/api/area'

const loading = ref(false)
const submitting = ref(false)
const updatingId = ref(null)
const deletingId = ref(null)
const dialogVisible = ref(false)
const areaFormRef = ref()
const isEdit = ref(false)

const areas = ref([])

// 筛选表单
const filterForm = reactive({
  name: '',
  status: ''
})

// 区域表单
const areaForm = reactive({
  id: null,
  name: '',
  description: '',
  sortOrder: 1,
  status: 1
})

// 表单验证规则
const areaRules = {
  name: [
    { required: true, message: '请输入区域名称', trigger: 'blur' },
    { min: 2, max: 20, message: '区域名称长度为2-20个字符', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入区域描述', trigger: 'blur' },
    { min: 5, max: 100, message: '区域描述长度为5-100个字符', trigger: 'blur' }
  ],
  sortOrder: [
    { required: true, message: '请输入排序值', trigger: 'blur' }
  ]
}

// 计算属性
const hasActiveFilters = computed(() => {
  return filterForm.name || filterForm.status
})

const filteredAreas = computed(() => {
  return areas.value.filter(area => {
    const nameMatch = !filterForm.name || 
      area.name.toLowerCase().includes(filterForm.name.toLowerCase())
    const statusMatch = !filterForm.status || 
      area.status.toString() === filterForm.status
    return nameMatch && statusMatch
  })
})

const dialogTitle = computed(() => {
  return isEdit.value ? '编辑区域' : '添加区域'
})

// 状态映射
const statusMap = {
  1: { text: '启用', type: 'success' },
  2: { text: '禁用', type: 'danger' }
}

// 获取区域列表
const loadAreas = async () => {
  try {
    loading.value = true
    const response = await getAreaList()
    
    if (response.code === 200) {
      areas.value = response.data
      ElMessage.success(`加载成功，共 ${areas.value.length} 个区域`)
    } else {
      ElMessage.error(response.message || '获取数据失败')
    }
  } catch (error) {
    console.error('获取区域列表失败:', error)
    ElMessage.error('获取数据失败: ' + (error.message || '网络错误'))
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = () => {
  // 前端筛选，不需要重新请求
}

// 重置
const handleReset = () => {
  filterForm.name = ''
  filterForm.status = ''
}

// 移除单个筛选条件
const removeFilter = (filterKey) => {
  filterForm[filterKey] = ''
}

// 添加区域
const handleAddArea = () => {
  isEdit.value = false
  resetForm()
  dialogVisible.value = true
}

// 编辑区域
const handleEdit = (area) => {
  isEdit.value = true
  resetForm()
  Object.assign(areaForm, { ...area })
  dialogVisible.value = true
}

// 提交表单
const handleSubmit = async () => {
  try {
    await areaFormRef.value.validate()
    submitting.value = true

    let response
    if (isEdit.value) {
      response = await updateArea(areaForm)
    } else {
      response = await addArea(areaForm)
    }

    console.log('后端响应:', response) // 调试信息

    if (response.code === 200) {
      ElMessage.success(isEdit.value ? '修改成功' : '添加成功')
      dialogVisible.value = false
      await loadAreas()
    } else {
      // 处理后端返回的错误信息
      console.log('错误响应结构:', response)
      
      // 尝试不同的错误信息提取方式
      let errorMessage = '操作失败'
      
      if (response.description) {
        // 如果直接有 description 字段
        errorMessage = Array.isArray(response.description) 
          ? response.description[0]?.message || response.description[0] || '数据验证失败'
          : response.description
      } else if (response.data && response.data.description) {
        // 如果错误信息在 data.description 中
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
    console.error('完整错误对象:', JSON.stringify(error, null, 2))
    
    if (error.errors) {
      // 前端表单验证失败
      ElMessage.warning('请完善表单信息')
    } else {
      // 处理后端错误
      let errorMessage = '操作失败'
      
      // 尝试从不同位置提取错误信息
      if (error.description) {
        errorMessage = Array.isArray(error.description) 
          ? error.description[0]?.message || error.description[0] || '数据验证失败'
          : error.description
      } else if (error.response?.data) {
        const errorData = error.response.data
        if (errorData.description) {
          errorMessage = Array.isArray(errorData.description) 
            ? errorData.description[0]?.message || errorData.description[0] || '数据验证失败'
            : errorData.description
        } else if (errorData.message) {
          errorMessage = errorData.message
        }
      } else if (error.message) {
        errorMessage = error.message
      }
      
      ElMessage.error(errorMessage)
    }
  } finally {
    submitting.value = false
  }
}

// 切换状态
const handleToggleStatus = async (area) => {
  try {
    updatingId.value = area.id
    const action = area.status === 1 ? '禁用' : '启用'
    
    await ElMessageBox.confirm(
      `确定要${action}区域 "${area.name}" 吗？`,
      '提示',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    const updateData = {
      ...area,
      status: area.status === 1 ? 2 : 1
    }
    
    const response = await updateArea(updateData)
    if (response.code === 200) {
      ElMessage.success(`${action}成功`)
      await loadAreas()
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

// 删除区域
const handleDelete = async (area) => {
  try {
    deletingId.value = area.id
    
    await ElMessageBox.confirm(
      `确定要删除区域 "${area.name}" 吗？此操作不可恢复！`,
      '警告',
      { 
        confirmButtonText: '确定删除', 
        cancelButtonText: '取消', 
        type: 'error',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    const response = await deleteArea(area.id)
    if (response.code === 200) {
      ElMessage.success('删除成功')
      await loadAreas()
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
  if (areaFormRef.value) {
    areaFormRef.value.resetFields()
  }
  Object.assign(areaForm, {
    id: null,
    name: '',
    description: '',
    sortOrder: 1,
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

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '-'
  return new Date(dateTime).toLocaleString('zh-CN')
}

onMounted(() => {
  loadAreas()
})
</script>

<style scoped lang="scss">
.area-mgr-page {
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
  .area-mgr-page {
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