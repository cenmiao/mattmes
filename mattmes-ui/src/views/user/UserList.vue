<template>
  <div class="user-list-page">
    <!-- 搜索栏 -->
    <el-card class="search-card">
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="工号/姓名">
          <el-input
            v-model="searchForm.userNo"
            placeholder="工号/姓名"
            clearable
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable>
            <el-option label="全部" :value="undefined" />
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="角色">
          <el-select v-model="searchForm.roleId" placeholder="全部" clearable>
            <el-option label="全部" :value="undefined" />
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">搜索</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
      <div class="action-bar">
        <el-button v-permission="'user:add'" type="primary" @click="handleAdd">新增</el-button>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column prop="userNo" label="工号" min-width="120">
          <template #default="{ row }">
            <span class="user-no" @click="handleViewDetail(row)">{{ row.userNo }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column label="角色" min-width="150">
          <template #default="{ row }">
            <el-tag
              v-for="role in row.roles"
              :key="role.id"
              size="small"
              class="role-tag"
            >
              {{ role.roleName }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="120">
          <template #default="{ row }">
            <el-tooltip
              v-if="row.status === 0 && row.disableReason"
              :content="'禁用原因: ' + row.disableReason"
              placement="top"
            >
              <el-switch
                v-permission="'user:disable'"
                :model-value="row.status === 1"
                :disabled="row.userNo === 'admin'"
                @change="(val: boolean) => handleStatusChange(row, val)"
              />
            </el-tooltip>
            <el-switch
              v-else
              v-permission="'user:disable'"
              :model-value="row.status === 1"
              :disabled="row.userNo === 'admin'"
              @change="(val: boolean) => handleStatusChange(row, val)"
            />
          </template>
        </el-table-column>
        <el-table-column label="最后登录" min-width="160">
          <template #default="{ row }">
            <span v-if="row.lastLoginTime" class="font-mono">{{ row.lastLoginTime }}</span>
            <span v-else class="text-muted">从未登录</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="160" fixed="right">
          <template #default="{ row }">
            <el-button
              v-permission="'user:edit'"
              link
              type="primary"
              title="编辑"
              @click="handleEdit(row)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              v-permission="'user:reset-password'"
              link
              type="warning"
              title="重置密码"
              @click="handleResetPassword(row)"
            >
              <el-icon><Key /></el-icon>
            </el-button>
            <el-button
              v-permission="'user:delete'"
              link
              type="danger"
              title="删除"
              :disabled="row.userNo === 'admin'"
              @click="handleDelete(row)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :total="pagination.total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="fetchUsers"
          @current-change="fetchUsers"
        />
      </div>
    </el-card>

    <!-- 新增/编辑用户弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑用户' : '新增用户'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="userForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="工号" prop="userNo">
          <el-input
            v-model="userForm.userNo"
            placeholder="请输入工号"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item v-if="!isEdit" label="密码" prop="password">
          <el-input
            v-model="userForm.password"
            type="password"
            placeholder="请输入初始密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select
            v-model="userForm.roleIds"
            multiple
            placeholder="请选择角色"
          >
            <el-option
              v-for="role in roleOptions"
              :key="role.id"
              :label="role.roleName"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 重置密码结果弹窗 -->
    <el-dialog
      v-model="resetPwdDialogVisible"
      title="重置密码"
      width="400px"
    >
      <div class="reset-pwd-result">
        <p>新密码已生成，请告知用户：</p>
        <div class="new-password">{{ newPassword }}</div>
      </div>
      <template #footer>
        <el-button type="primary" @click="resetPwdDialogVisible = false">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Key, Delete } from '@element-plus/icons-vue'
import {
  getUsers,
  getUser,
  createUser,
  updateUser,
  resetPassword,
  disableUser,
  enableUser,
  deleteUser,
  type UserQuery,
  type UserCreateRequest,
  type UserUpdateRequest,
  type UserResponse
} from '@/api/user'
import {
  getAllEnabledRoles,
  type RoleSimpleResponse
} from '@/api/role'

const loading = ref(false)
const tableData = ref<UserResponse[]>([])
const roleOptions = ref<RoleSimpleResponse[]>([])

const searchForm = reactive<UserQuery>({
  userNo: undefined,
  name: undefined,
  status: undefined,
  roleId: undefined
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 弹窗状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingUserId = ref<number | null>(null)
const formRef = ref()

const userForm = reactive({
  userNo: '',
  name: '',
  phone: '',
  email: '',
  password: '',
  roleIds: [] as number[]
})

const formRules = {
  userNo: [{ required: true, message: '请输入工号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入姓名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入初始密码', trigger: 'blur' }]
}

// 重置密码结果弹窗
const resetPwdDialogVisible = ref(false)
const newPassword = ref('')

async function fetchUsers() {
  loading.value = true
  try {
    const result = await getUsers({
      ...searchForm,
      page: pagination.page,
      size: pagination.size
    })
    tableData.value = result.list
    pagination.total = result.total
  } catch {
    // 错误已由request拦截器处理
  } finally {
    loading.value = false
  }
}

async function fetchRoles() {
  try {
    roleOptions.value = await getAllEnabledRoles()
  } catch {
    // 角色加载失败不阻塞页面
  }
}

function handleSearch() {
  pagination.page = 1
  fetchUsers()
}

function handleReset() {
  searchForm.userNo = undefined
  searchForm.name = undefined
  searchForm.status = undefined
  searchForm.roleId = undefined
  pagination.page = 1
  fetchUsers()
}

function handleAdd() {
  isEdit.value = false
  editingUserId.value = null
  userForm.userNo = ''
  userForm.name = ''
  userForm.phone = ''
  userForm.email = ''
  userForm.password = ''
  userForm.roleIds = []
  dialogVisible.value = true
}

async function handleEdit(row: UserResponse) {
  isEdit.value = true
  editingUserId.value = row.id
  try {
    const user = await getUser(row.id)
    userForm.userNo = user.userNo
    userForm.name = user.name
    userForm.phone = user.phone || ''
    userForm.email = user.email || ''
    userForm.roleIds = user.roles.map(r => r.id)
    dialogVisible.value = true
  } catch {
    // 错误已由request拦截器处理
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  try {
    if (isEdit.value && editingUserId.value) {
      const data: UserUpdateRequest = {
        name: userForm.name,
        phone: userForm.phone || undefined,
        email: userForm.email || undefined,
        roleIds: userForm.roleIds
      }
      await updateUser(editingUserId.value, data)
      ElMessage.success('编辑成功')
    } else {
      const data: UserCreateRequest = {
        userNo: userForm.userNo,
        name: userForm.name,
        password: userForm.password,
        phone: userForm.phone || undefined,
        email: userForm.email || undefined,
        roleIds: userForm.roleIds.length > 0 ? userForm.roleIds : undefined
      }
      await createUser(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchUsers()
  } catch {
    // 错误已由request拦截器处理
  }
}

async function handleStatusChange(row: UserResponse, val: boolean) {
  if (row.userNo === 'admin') return

  try {
    if (!val) {
      // 禁用需要输入原因
      const { value: reason } = await ElMessageBox.prompt(
        '请输入禁用原因',
        '禁用用户',
        { confirmButtonText: '确定', cancelButtonText: '取消', inputValidator: (v: string) => v ? true : '请输入禁用原因' }
      )
      await disableUser(row.id, reason)
      ElMessage.success('已禁用')
    } else {
      await enableUser(row.id)
      ElMessage.success('已启用')
    }
    fetchUsers()
  } catch {
    // 用户取消或请求失败
  }
}

async function handleResetPassword(row: UserResponse) {
  try {
    await ElMessageBox.confirm(
      '确定要重置该用户的密码吗？',
      '重置密码',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    const result = await resetPassword(row.id)
    newPassword.value = result.password
    resetPwdDialogVisible.value = true
  } catch {
    // 用户取消或请求失败
  }
}

async function handleDelete(row: UserResponse) {
  if (row.userNo === 'admin') return

  try {
    await ElMessageBox.confirm(
      `确定要删除用户"${row.name}"吗？`,
      '删除用户',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch {
    // 用户取消或请求失败
  }
}

function handleViewDetail(_row: UserResponse) {
  // 可扩展为查看详情
}

onMounted(() => {
  fetchUsers()
  fetchRoles()
})
</script>

<style scoped>
.user-list-page {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.search-form {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
}

.action-bar {
  margin-top: 12px;
}

.user-no {
  font-family: var(--font-data);
  color: var(--primary-color);
  cursor: pointer;
}

.user-no:hover {
  text-decoration: underline;
}

.role-tag {
  margin-right: 4px;
  margin-bottom: 2px;
}

.font-mono {
  font-family: var(--font-data);
}

.text-muted {
  color: var(--content-c4);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.reset-pwd-result {
  text-align: center;
}

.new-password {
  font-family: var(--font-data);
  font-size: 20px;
  color: var(--primary-color);
  padding: 12px;
  margin-top: 8px;
  background: var(--surface-l2);
  border-radius: var(--radius-md);
  user-select: all;
}
</style>
