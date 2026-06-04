<template>
  <div class="permission-list-page">
    <!-- 操作栏 -->
    <el-card class="search-card">
      <div class="action-bar">
        <el-button v-permission="'permission:add'" type="primary" @click="handleAdd">
          新增
        </el-button>
      </div>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table :data="tableData" v-loading="loading" stripe row-key="id" default-expand-all>
        <el-table-column label="权限名称" min-width="180">
          <template #default="{ row }">
            <div class="permission-name">
              <el-icon class="permission-icon">
                <Folder v-if="row.permissionType === 1" />
                <Pointer v-else />
              </el-icon>
              <span>{{ row.permissionName }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="权限编码" min-width="140">
          <template #default="{ row }">
            <span class="permission-code">{{ row.permissionCode }}</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" min-width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.permissionType === 1 ? 'primary' : 'info'" size="small">
              {{ row.permissionType === 1 ? '模块' : '按钮' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column label="创建时间" min-width="160">
          <template #default="{ row }">
            <span class="font-mono">{{ row.createTime }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="{ row }">
            <el-button
              v-permission="'permission:edit'"
              link
              type="primary"
              title="编辑"
              @click="handleEdit(row)"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              v-if="row.permissionType === 1"
              v-permission="'permission:add'"
              link
              type="primary"
              title="新增子权限"
              @click="handleAddChild(row)"
            >
              <el-icon><Plus /></el-icon>
            </el-button>
            <el-button
              v-permission="'permission:delete'"
              link
              type="danger"
              title="删除"
              @click="handleDelete(row)"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑权限弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="isEdit ? '编辑权限' : '新增权限'"
      width="500px"
      destroy-on-close
    >
      <el-form
        ref="formRef"
        :model="permissionForm"
        :rules="formRules"
        label-width="80px"
      >
        <el-form-item label="权限名称" prop="permissionName">
          <el-input v-model="permissionForm.permissionName" placeholder="请输入权限名称" />
        </el-form-item>
        <el-form-item label="权限编码" prop="permissionCode">
          <el-input
            v-model="permissionForm.permissionCode"
            placeholder="如 user 或 user:add"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="父权限" prop="parentId">
          <el-select
            v-model="permissionForm.parentId"
            placeholder="选择父权限（模块级）"
            clearable
            :disabled="isEdit"
            style="width: 100%"
          >
            <el-option
              v-for="item in moduleOptions"
              :key="item.id"
              :label="item.permissionName"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="permissionForm.description"
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Folder, Pointer, Edit, Plus, Delete } from '@element-plus/icons-vue'
import {
  getPermissionTree,
  createPermission,
  updatePermission,
  deletePermission,
  type PermissionResponse,
  type PermissionTreeResponse,
  type PermissionCreateRequest,
  type PermissionUpdateRequest
} from '@/api/permission'

const loading = ref(false)
const treeData = ref<PermissionTreeResponse[]>([])

// 将树形数据扁平化为表格数据
const tableData = computed(() => {
  const result: PermissionResponse[] = []
  const flatten = (items: PermissionTreeResponse[]) => {
    for (const item of items) {
      result.push({
        id: item.id,
        permissionName: item.permissionName,
        permissionCode: item.permissionCode,
        permissionType: 1,
        parentId: null,
        description: null,
        createTime: ''
      })
      if (item.children && item.children.length > 0) {
        result.push(...item.children)
      }
    }
  }
  flatten(treeData.value)
  return result
})

// 模块选项（用于父权限选择）
const moduleOptions = computed(() => {
  return treeData.value.map(item => ({
    id: item.id,
    permissionName: item.permissionName
  }))
})

// 弹窗状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingPermissionId = ref<number | null>(null)
const formRef = ref()

const permissionForm = reactive({
  permissionName: '',
  permissionCode: '',
  parentId: undefined as number | undefined,
  description: ''
})

const formRules = {
  permissionName: [{ required: true, message: '请输入权限名称', trigger: 'blur' }],
  permissionCode: [{ required: true, message: '请输入权限编码', trigger: 'blur' }]
}

async function fetchPermissions() {
  loading.value = true
  try {
    const result = await getPermissionTree()
    treeData.value = result
  } catch {
    // 错误已由request拦截器处理
  } finally {
    loading.value = false
  }
}

function handleAdd() {
  isEdit.value = false
  editingPermissionId.value = null
  permissionForm.permissionName = ''
  permissionForm.permissionCode = ''
  permissionForm.parentId = undefined
  permissionForm.description = ''
  dialogVisible.value = true
}

function handleAddChild(row: PermissionResponse) {
  isEdit.value = false
  editingPermissionId.value = null
  permissionForm.permissionName = ''
  permissionForm.permissionCode = ''
  permissionForm.parentId = row.id
  permissionForm.description = ''
  dialogVisible.value = true
}

async function handleEdit(row: PermissionResponse) {
  isEdit.value = true
  editingPermissionId.value = row.id
  permissionForm.permissionName = row.permissionName
  permissionForm.permissionCode = row.permissionCode
  permissionForm.parentId = row.parentId ?? undefined
  permissionForm.description = row.description || ''
  dialogVisible.value = true
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }

  try {
    if (isEdit.value && editingPermissionId.value) {
      const data: PermissionUpdateRequest = {
        permissionName: permissionForm.permissionName,
        description: permissionForm.description || undefined
      }
      await updatePermission(editingPermissionId.value, data)
      ElMessage.success('编辑成功')
    } else {
      const data: PermissionCreateRequest = {
        permissionName: permissionForm.permissionName,
        permissionCode: permissionForm.permissionCode,
        parentId: permissionForm.parentId,
        description: permissionForm.description || undefined
      }
      await createPermission(data)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    fetchPermissions()
  } catch {
    // 错误已由request拦截器处理
  }
}

async function handleDelete(row: PermissionResponse) {
  const isModule = row.permissionType === 1
  const message = isModule
    ? `确定要删除权限"${row.permissionName}"吗？将同时删除其下所有按钮级权限。`
    : `确定要删除权限"${row.permissionName}"吗？`

  try {
    await ElMessageBox.confirm(message, '删除权限', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deletePermission(row.id)
    ElMessage.success('删除成功')
    fetchPermissions()
  } catch {
    // 用户取消或请求失败
  }
}

onMounted(() => {
  fetchPermissions()
})
</script>

<style scoped>
.permission-list-page {
  padding: 16px;
}

.search-card {
  margin-bottom: 16px;
}

.action-bar {
  display: flex;
  justify-content: flex-end;
}

.permission-name {
  display: flex;
  align-items: center;
  gap: 8px;
}

.permission-icon {
  color: var(--primary-color);
}

.permission-code {
  font-family: var(--font-data);
  color: #22D3EE;
}

.font-mono {
  font-family: var(--font-data);
}
</style>