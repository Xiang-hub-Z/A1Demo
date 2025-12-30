<template>
  <div class="user-mgr-page">
    <div class="page-header">
      <h2>用户管理</h2>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索用户名、真实姓名、手机号或密码"
          style="width: 200px; margin-right: 10px;"
          clearable
          @keyup.enter="handleSearch"
        />
        <el-button type="primary" @click="handleSearch">
          <el-icon><Search /></el-icon>
          搜索
        </el-button>
        <el-button type="primary" @click="handleAddUser">
          <el-icon><Plus /></el-icon>
          添加用户
        </el-button>
      </div>
    </div>

    <el-card>
      <el-table :data="filteredUsers" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="用户ID" width="100" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="password" label="密码" width="150" />
        <el-table-column prop="realname" label="真实姓名" />
        <el-table-column prop="phone" label="手机号" />
        <el-table-column prop="balance" label="余额" width="120">
          <template #default="{ row }">
            ¥{{ row.balance }}
          </template>
        </el-table-column>
        <el-table-column prop="createtime" label="注册时间" width="180">
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

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
      width="500px"
    >
      <el-form
        :model="userForm"
        :rules="userRules"
        ref="userFormRef"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input
            v-model="userForm.password"
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
        <el-form-item label="真实姓名" prop="realname">
          <el-input v-model="userForm.realname" placeholder="请输入真实姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="余额" prop="balance">
          <el-input-number
            v-model="userForm.balance"
            :min="0"
            :precision="2"
            style="width: 100%"
          />
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
import { getUserList, addUser, updateUserInfo, deleteUser } from '@/api/user'
import { Search, Plus, View, Hide } from '@element-plus/icons-vue'

const loading = ref(false)
const dialogVisible = ref(false)
const submitting = ref(false)
const dialogType = ref('add') // 'add' | 'edit'
const searchKeyword = ref('')
const userFormRef = ref()
const showPassword = ref(false)

const users = ref([])

const userForm = reactive({
  id: null,
  username: '',
  password: '',
  realname: '',
  phone: '',
  balance: 0
})

// 密码验证规则 - 始终必填
const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度至少6位', trigger: 'blur' }
  ],
  realname: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
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

// 获取用户列表
const fetchUserList = async () => {
  loading.value = true
  try {
    const response = await getUserList()
    users.value = response
  } catch (error) {
    ElMessage.error('获取用户列表失败')
    console.error('获取用户列表错误:', error)
  } finally {
    loading.value = false
  }
}

// 搜索用户
const filteredUsers = computed(() => {
  if (!searchKeyword.value) return users.value
  const keyword = searchKeyword.value.toLowerCase()
  return users.value.filter(user => 
    user.username.toLowerCase().includes(keyword) || 
    user.realname.toLowerCase().includes(keyword) ||
    user.phone.includes(keyword) ||
    (user.password && user.password.toLowerCase().includes(keyword))
  )
})

const handleSearch = () => {
  // 搜索逻辑已经在 computed 中处理
  ElMessage.success(`搜索完成，找到 ${filteredUsers.value.length} 条结果`)
}

// 添加用户
const handleAddUser = () => {
  dialogType.value = 'add'
  dialogVisible.value = true
  showPassword.value = false
  Object.assign(userForm, {
    id: null,
    username: '',
    password: '',
    realname: '',
    phone: '',
    balance: 0
  })
}

// 编辑用户
const handleEdit = (user) => {
  dialogType.value = 'edit'
  dialogVisible.value = true
  showPassword.value = false
  Object.assign(userForm, { ...user })
}

// 提交表单
const handleSubmit = async () => {
  if (!userFormRef.value) return
  
  await userFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (dialogType.value === 'add') {
        const response = await addUser(userForm)
        if (response === '添加成功') {
          ElMessage.success('添加用户成功')
          dialogVisible.value = false
          fetchUserList()
        } else {
          ElMessage.error('添加用户失败')
        }
      } else {
        const response = await updateUserInfo(userForm)
        if (response === '修改成功') {
          ElMessage.success('修改用户成功')
          dialogVisible.value = false
          fetchUserList()
        } else {
          ElMessage.error('修改用户失败')
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

// 删除用户
const handleDelete = async (user) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 "${user.username}" 吗？`,
      '警告',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    
    const response = await deleteUser(user.id)
    if (response === '删除成功') {
      ElMessage.success('删除成功')
      fetchUserList()
    } else {
      ElMessage.error('删除失败')
    }
  } catch {
    // 用户取消
  }
}

onMounted(() => {
  fetchUserList()
})
</script>

<style scoped lang="scss">
.user-mgr-page {
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