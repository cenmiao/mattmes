<template>
  <div class="role-list-page">
    <!-- 操作栏 -->
    <el-card class="search-card">
      <div class="action-bar">
        <el-button v-permission="'role:add'" type="primary" @click="handleAdd">新增</el-button>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe>
        <el-table-column label="角色名称" min-width="120">
          <template #default="{ row }">
            <div class="role-name">
              <el-icon class="role-icon"><Avatar /></el-icon>
              <span>{{ row.roleName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="roleCode" label="角色编码" min-width="140">
          <template #default="{ row }">
            <span class="role-code">{{ row.roleCode }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="用户数" min-width="100" align="center">
          <template #default="{ row }">
            <el-badge :value="row.userCount" :max="99" class="user-count-badge" />
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" size="small">
              {{ row.status === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" min-width="160">
          <template #default="{ row }">
            <span class="font-mono">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              v-permission="'role:edit'"
              link
              type="primary"
              title="编辑"
              @click="handleEdit(row)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              v-permission="'role:assign-permission'"
              link
              type="primary"
              title="分配权限"
              @click="handleAssignPermissions(row)"
            >
              <el-icon><Setting /></el-icon>
            </el-button>
            <el-button
              v-permission="'role:delete'"
              link
              type="danger"
              title="删除"
              :disabled="row.roleCode === 'SUPER_ADMIN'"
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
          @size-change="fetchRoles"
          @current-change="fetchRoles"
        />
      </div>
    </el-card>

    <!-- 新增/编辑角色弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑角色' : '新增角色'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="roleForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码" prop="roleCode">
          <el-input
            v-model="roleForm.roleCode"
            placeholder="请输入角色编码，如 ROLE_NAME"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="roleForm.description"
            type="textarea"
            placeholder="请输入描述"
            :rows="3"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 分配权限弹窗 -->
    <el-dialog
      v-model="permissionDialogVisible"
      title="分配权限"
      width="500px"
      destroy-on-close
    >
      <el-tree
        ref="permissionTreeRef"
        :data="permissionTree"
        :props="{ label: 'permissionName', children: 'children' }"
        show-checkbox
        node-key="id"
        default-expand-all
        class="permission-tree"
      />
      <template #footer>
        <el-button @click="permissionDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePermissionSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Avatar, Edit, Setting, Delete } from '@element-plus/icons-vue'
import {
  getRoles,
  getRole,
  createRole,
  updateRole,
  assignPermissions,
  deleteRole,
  type RoleResponse,
  type RoleCreateRequest,
  type RoleUpdateRequest
} from '@/api/role'
import { getPermissions, type PermissionResponse } from '@/api/permission'

const loading = ref(false)
const tableData = ref<RoleResponse[]>([])

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// 弹窗状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingRoleId = ref<number | null>(null)
const formRef = ref()

const roleForm = reactive({
  roleName: '',
  roleCode: '',
  description: ''
})

const formRules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleCode: [{ required: true, message: '请输入角色编码', trigger: 'blur' }]
}

// 权限弹窗状态
const permissionDialogVisible = ref(false)
const permissionTreeRef = ref()
const permissionTree = ref<PermissionResponse[]>([])
const assigningRoleId = ref<number | null>(null)

async function fetchRoles() {
  loading.value = true
  try {
    const result = await getRoles({
      pageNum: pagination.page,
      pageSize: pagination.size
    })
    tableData.value = result.list
    pagination.total = result.total
  } catch {
    // 错误已由request拦截器处理
  } finally {
    loading.value = false
  }
}

async function fetchPermissions() {
  try {
    const result = await getPermissions()
    permissionTree.value = result
  } catch {
    // 权限加载失败不阻塞
  }
}

function handleAdd() {
  isEdit.value = false
  editingRoleId.value = null
  roleForm.roleName = ''
  roleForm.roleCode = ''
  roleForm.description = ''
  dialogVisible.value = true
}

async function handleEdit(row: RoleResponse) {
  isEdit.value = true
  editingRoleId.value = row.id
  try {
    const role = await getRole(row.id)
    roleForm.roleName = role.roleName
    roleForm.roleCode = role.roleCode
    roleForm.description = role.description || ''
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
    if (isEdit.value && editingRoleId.value) {
      const data: RoleUpdateRequest = {
        roleName: roleForm.roleName,
        description: roleForm.description || undefined
      }
      await updateRole(editingRoleId.value, data)
      ElMessage.success('编辑成功')
    } else {
      const data: RoleCreateRequest = {
        roleName: roleForm.roleName,
        roleCode: roleForm.roleCode,
        description: roleForm.description || undefined
      }
      await createRole(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchRoles()
  } catch {
    // 错误已由request拦截器处理
  }
}

async function handleAssignPermissions(row: RoleResponse) {
  assigningRoleId.value = row.id
  try {
    // 获取权限树
    await fetchPermissions()

    // 获取角色详情（包含已分配的权限）
    const role = await getRole(row.id)
    const assignedPermissionIds = role.permissions?.map((p: any) => p.id) || []

    // 设置已选中的权限
    permissionTreeRef.value?.setCheckedKeys(assignedPermissionIds)
    permissionDialogVisible.value = true
  } catch {
    // 错误已由request拦截器处理
  }
}

async function handlePermissionSubmit() {
  if (!assigningRoleId.value) return

  try {
    const checkedKeys = permissionTreeRef.value?.getCheckedKeys(false) || []
    await assignPermissions(assigningRoleId.value, checkedKeys)
    ElMessage.success('权限分配成功')
    permissionDialogVisible.value = false
  } catch {
    // 错误已由request拦截器处理
  }
}

async function handleDelete(row: RoleResponse) {
  if (row.roleCode === 'SUPER_ADMIN') return

  try {
    await ElMessageBox.confirm(
      `确定要删除角色"${row.roleName}"吗？`,
      '删除角色',
      { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' }
    )
    await deleteRole(row.id)
    ElMessage.success('删除成功')
    fetchRoles()
  } catch {
    // 用户取消或请求失败
  }
}

onMounted(() => {
  fetchRoles()
})
</script>

<style scoped>
.role-list-page {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
}

.role-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.role-icon {
  color: var(--primary-color);
}

.role-code {
  font-family: var(--font-data);
  color: var(--primary-color);
}

.font-mono {
  font-family: var(--font-data);
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: 16px;
}

.permission-tree {
  max-height: 400px;
  overflow-y: auto;
}

.user-count-badge :deep(.el-badge__content) {
  background-color: var(--primary-color);
}
</style>
