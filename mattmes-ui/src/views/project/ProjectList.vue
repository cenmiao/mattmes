<template>
  <div class="project-container">
    <!-- 查询表单 -->
    <el-card class="query-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="项目编码">
          <el-input
            v-model="queryParams.code"
            placeholder="请输入项目编码"
            clearable
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="项目名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入项目名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="启用状态">
          <el-select
            v-model="queryParams.enable"
            placeholder="请选择状态"
            clearable
            style="width: 120px"
          >
            <el-option label="启用" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>

        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
          <el-button type="success" @click="handleAdd" v-permission="'project:add'">
            <el-icon><Plus /></el-icon>
            新增
          </el-button>
          <el-button
            type="danger"
            @click="handleBatchDelete"
            :disabled="selectedRows.length === 0"
            v-permission="'project:delete'"
          >
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
          <el-button type="warning" @click="handleExport" v-permission="'project:export'">
            <el-icon><Download /></el-icon>
            导出
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 数据表格 -->
    <el-card class="table-card">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="项目编码" width="150" />
        <el-table-column prop="name" label="项目名称" width="180" />
        <el-table-column prop="description" label="项目描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="enable" label="启用状态" width="100">
          <template #default="{ row }">
            <el-switch
              v-model="row.enable"
              :active-value="1"
              :inactive-value="0"
              @change="handleStatusChange(row)"
              v-permission="'project:edit'"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)" v-permission="'project:edit'">
              编辑
            </el-button>
            <el-button type="danger" link size="small" @click="handleDelete(row)" v-permission="'project:delete'">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleQuery"
        @current-change="handleQuery"
        style="margin-top: 20px; justify-content: flex-end"
      />
    </el-card>

    <!-- 新增弹窗 -->
    <el-dialog
      v-model="addDialogVisible"
      title="新增项目"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addFormRules"
        label-width="100px"
      >
        <el-form-item label="项目编码" prop="code">
          <el-input
            v-model="addForm.code"
            placeholder="请输入项目编码"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="项目名称" prop="name">
          <el-input
            v-model="addForm.name"
            placeholder="请输入项目名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="启用状态" prop="enable">
          <el-radio-group v-model="addForm.enable">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="addForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="addForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAdd" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑弹窗 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑项目"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="100px"
      >
        <el-form-item label="项目编码">
          <el-input
            v-model="editForm.code"
            disabled
            placeholder="项目编码不可修改"
          />
        </el-form-item>
        <el-form-item label="项目名称" prop="name">
          <el-input
            v-model="editForm.name"
            placeholder="请输入项目名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="启用状态" prop="enable">
          <el-radio-group v-model="editForm.enable">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="项目描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入项目描述"
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitEdit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { queryProjectList, addProject, editProject, updateProjectStatus, deleteProject, batchDeleteProjects, exportProject, type ProjectQueryRequest, type ProjectResponse, type ProjectAddRequest, type ProjectEditRequest } from '@/api/project'
import { Search, Refresh, Plus, Delete, Download } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

// 查询参数
const queryParams = reactive<ProjectQueryRequest>({
  code: '',
  name: '',
  enable: undefined,
  pageNum: 1,
  pageSize: 10
})

// 表格数据
const tableData = ref<ProjectResponse[]>([])
const total = ref(0)
const loading = ref(false)

// 多选相关
const selectedRows = ref<ProjectResponse[]>([])

// 新增弹窗
const addDialogVisible = ref(false)
const addFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const addForm = reactive<ProjectAddRequest>({
  code: '',
  name: '',
  description: '',
  enable: 1,
  remark: ''
})

// 编码格式校验（仅允许字母、数字、下划线、中划线）
const validateCode = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (!/^[a-zA-Z0-9_-]+$/.test(value)) {
    callback(new Error('编码只能包含字母、数字、下划线和中划线'))
  } else {
    callback()
  }
}

// 新增表单校验规则
const addFormRules: FormRules = {
  code: [
    { required: true, message: '请输入项目编码', trigger: 'blur' },
    { max: 50, message: '编码长度不能超过50个字符', trigger: 'blur' },
    { validator: validateCode, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { max: 100, message: '名称长度不能超过100个字符', trigger: 'blur' }
  ]
}

// 编辑弹窗
const editDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive<ProjectEditRequest & { code: string }>({
  id: 0,
  code: '',
  name: '',
  description: '',
  enable: 1,
  remark: ''
})

// 编辑表单校验规则
const editFormRules: FormRules = {
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { max: 100, message: '名称长度不能超过100个字符', trigger: 'blur' }
  ]
}

// 查询项目列表
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await queryProjectList(queryParams)
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('查询项目失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  queryParams.code = ''
  queryParams.name = ''
  queryParams.enable = undefined
  queryParams.pageNum = 1
  queryParams.pageSize = 10
  handleQuery()
}

// 打开新增弹窗
const handleAdd = () => {
  // 重置表单
  addForm.code = ''
  addForm.name = ''
  addForm.description = ''
  addForm.enable = 1
  addForm.remark = ''
  addFormRef.value?.resetFields()
  addDialogVisible.value = true
}

// 提交新增
const handleSubmitAdd = async () => {
  const valid = await addFormRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    await addProject(addForm)
    ElMessage.success('新增项目成功')
    addDialogVisible.value = false
    handleQuery() // 刷新列表
  } catch (error: any) {
    ElMessage.error(error.message || '新增项目失败')
  } finally {
    submitLoading.value = false
  }
}

// 打开编辑弹窗
const handleEdit = (row: ProjectResponse) => {
  // 填充表单数据
  editForm.id = row.id
  editForm.code = row.code
  editForm.name = row.name
  editForm.description = row.description || ''
  editForm.enable = row.enable
  editForm.remark = row.remark || ''
  editFormRef.value?.resetFields()
  editDialogVisible.value = true
}

// 提交编辑
const handleSubmitEdit = async () => {
  const valid = await editFormRef.value?.validate()
  if (!valid) return

  submitLoading.value = true
  try {
    await editProject({
      id: editForm.id,
      name: editForm.name,
      description: editForm.description,
      enable: editForm.enable,
      remark: editForm.remark
    })
    ElMessage.success('编辑项目成功')
    editDialogVisible.value = false
    handleQuery() // 刷新列表
  } catch (error: any) {
    ElMessage.error(error.message || '编辑项目失败')
  } finally {
    submitLoading.value = false
  }
}

// 状态切换
const handleStatusChange = async (row: ProjectResponse) => {
  try {
    await updateProjectStatus(row.id, row.enable)
    ElMessage.success('状态更新成功')
  } catch (error: any) {
    // 更新失败时恢复原状态
    row.enable = row.enable === 1 ? 0 : 1
    ElMessage.error(error.message || '状态更新失败')
  }
}

// 多选变化
const handleSelectionChange = (rows: ProjectResponse[]) => {
  selectedRows.value = rows
}

// 删除单个项目
const handleDelete = (row: ProjectResponse) => {
  ElMessageBox.confirm(
    `确定要删除项目"${row.name}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      await deleteProject(row.id)
      ElMessage.success('删除项目成功')
      handleQuery() // 刷新列表
    } catch (error: any) {
      ElMessage.error(error.message || '删除项目失败')
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 批量删除项目
const handleBatchDelete = () => {
  const count = selectedRows.value.length
  ElMessageBox.confirm(
    `确定要删除选中的 ${count} 条项目吗？`,
    '批量删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      const ids = selectedRows.value.map(row => row.id)
      await batchDeleteProjects(ids)
      ElMessage.success(`成功删除 ${count} 条项目`)
      handleQuery() // 刷新列表
      selectedRows.value = [] // 清空选择
    } catch (error: any) {
      ElMessage.error(error.message || '批量删除项目失败')
    }
  }).catch(() => {
    // 用户取消删除
  })
}

// 导出项目数据
const handleExport = async () => {
  try {
    const response = await exportProject(queryParams)
    // 创建 Blob 对象
    const blob = new Blob([response as any], { type: 'text/csv;charset=UTF-8' })
    // 创建下载链接
    const url = window.URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    // 生成文件名
    const now = new Date()
    const timestamp = `${now.getFullYear()}${String(now.getMonth() + 1).padStart(2, '0')}${String(now.getDate()).padStart(2, '0')}_${String(now.getHours()).padStart(2, '0')}${String(now.getMinutes()).padStart(2, '0')}${String(now.getSeconds()).padStart(2, '0')}`
    link.download = `项目数据_${timestamp}.csv`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)
    window.URL.revokeObjectURL(url)
    ElMessage.success('导出成功')
  } catch (error: any) {
    ElMessage.error(error.message || '导出失败')
  }
}

// 初始化
onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.project-container {
  padding: 20px;
}

.query-card {
  margin-bottom: 20px;
}

.table-card {
  /* 工业风格表格卡片 */
}
</style>
