<template>
  <div class="admin-mgr-page">
    <div class="page-header">
      <h2>管理员管理</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索管理员账号、姓名或手机号"
          style="width: 200px; margin-right: 10px;"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button type="primary" @click="handleAddAdmin">
          <el-icon><Plus /></el-icon>
          添加管理员
        </el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="filteredAdmins" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="管理员ID" width="100" />
        <el-table-column prop="admin" label="管理员账号" />
        <el-table-column prop="password" label="密码" width="150" />
        <el-table-column prop="name" label="姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="createtime" label="创建时间" width="180">
          <template #default="{ row }">
            {{ formatTime(row.createtime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 添加/编辑管理员对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加管理员' : '编辑管理员'"
      width="500px"
    >
      <el-form
        :model="adminForm"
        :rules="adminRules"
        ref="adminFormRef"
        label-width="100px"
      >
        <el-form-item label="管理员账号" prop="admin">
          <el-input v-model="adminForm.admin" placeholder="请输入管理员账号" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="adminForm.password"
            :type="showPassword ? 'text' : 'password'"
            :placeholder="dialogType === 'add' ? '请输入密码' : '请输入新密码'"
          >
            <template #append>
              <el-button :icon="showPassword ? View : Hide" @click="showPassword = !showPassword" />
            </template>
          </el-input>
          <div v-if="dialogType === 'edit'" class="password-tip">
            请输入新密码，不能为空
          </div>
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="adminForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="adminForm.phone" placeholder="请输入手机号" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            确定
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getAdminList, addAdmin, updateAdminInfo, deleteAdmin } from '@/api/admin'
import { Search, Plus, View, Hide } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const dialogType = ref('add') // 'add' | 'edit'
const searchKeyword = ref('')
const adminFormRef = ref()
const showPassword = ref(false)

const admins = ref([])

const adminForm = reactive({
  id: null,
  admin: '',
  password: '',
  name: '',
  phone: ''
})

// 验证规则
const adminRules = {
  admin: [
    { required: true, message: '请输入管理员账号', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  phone: [
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
  ]
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return ''
  return new Date(time).toLocaleString('zh-CN')
}

// 获取管理员列表
const fetchAdminList = async () => {
  loading.value = true
  try {
    const response = await getAdminList()
    admins.value = response
  } catch (error) {
    ElMessage.error('获取管理员列表失败')
    console.error('获取管理员列表错误:', error)
  } finally {
    loading.value = false
  }
}

// 搜索管理员
const filteredAdmins = computed(() => {
  if (!searchKeyword.value) return admins.value
  const keyword = searchKeyword.value.toLowerCase()
  return admins.value.filter(admin => 
    admin.admin.toLowerCase().includes(keyword) || 
    admin.name.toLowerCase().includes(keyword) ||
    admin.phone.includes(keyword)
  )
})

const handleSearch = () => {
  // 搜索逻辑已经在 computed 中处理
  ElMessage.success(`搜索完成，找到 ${filteredAdmins.value.length} 条结果`)
}

// 添加管理员
const handleAddAdmin = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  showPassword.value = false
  Object.assign(adminForm, {
    id: null,
    admin: '',
    password: '',
    name: '',
    phone: ''
  })
}

// 编辑管理员
const handleEdit = (admin) => {
  dialogType.value = 'edit'
  dialogVisible.value = true
  showPassword.value = false
  Object.assign(adminForm, { ...admin })
}

// 提交表单
const handleSubmit = async () => {
  if (!adminFormRef.value) return
  
  await adminFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (dialogType.value === 'add') {
        const response = await addAdmin(adminForm)
        if (response === '添加成功') {
          ElMessage.success('添加管理员成功')
          dialogVisible.value = false
          fetchAdminList()
        } else {
          ElMessage.error('添加管理员失败')
        }
      } else {
        const response = await updateAdminInfo(adminForm)
        if (response === '修改成功') {
          ElMessage.success('修改管理员成功')
          dialogVisible.value = false
          fetchAdminList()
        } else {
          ElMessage.error('修改管理员失败')
        }
      }
    } catch (error) {
      ElMessage.error('操作失败，请重试')
      console.error('操作错误:', error)
    } finally {
      submitting.value = false
    }
  })
}

// 删除管理员
const handleDelete = async (admin) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除管理员 "${admin.admin}" 吗？`,
      '警告',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    const response = await deleteAdmin(admin.id)
    if (response === '删除成功') {
      ElMessage.success('删除成功')
      fetchAdminList()
    } else {
      ElMessage.error('删除失败')
    }
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  fetchAdminList()
})
</script>

<style scoped lang="scss">
.admin-mgr-page {
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
}

.password-tip {
  font-size: 12px;
  color: #909399;
  margin-top: 5px;
}
</style>