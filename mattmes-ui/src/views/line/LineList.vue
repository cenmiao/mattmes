<template>
  <div class="line-container">
    <!-- 查询表单 -->
    <el-card class="query-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="线体编码">
          <el-input
            v-model="queryParams.code"
            placeholder="请输入线体编码"
            clearable
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="线体名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入线体名称"
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
          <el-button type="success" @click="handleAdd" v-permission="'line:add'">
            <el-icon><Plus /></el-icon>
            新增
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
        <el-table-column prop="code" label="线体编码" width="150" />
        <el-table-column prop="name" label="线体名称" width="180" />
        <el-table-column prop="description" label="描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="enable" label="启用状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enable === 1 ? 'success' : 'danger'">
              {{ row.enable === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdBy" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <el-button
              type="primary"
              link
              @click="handleEdit(row)"
              v-permission="'line:edit'"
            >
              编辑
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
      title="新增线体"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addFormRules"
        label-width="100px"
      >
        <el-form-item label="线体编码" prop="code">
          <el-input
            v-model="addForm.code"
            placeholder="请输入线体编码"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="线体名称" prop="name">
          <el-input
            v-model="addForm.name"
            placeholder="请输入线体名称"
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
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="addForm.description"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入描述"
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
      title="编辑线体"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="100px"
      >
        <el-form-item label="线体编码">
          <!-- 编码是业务唯一标识，编辑时只读展示且不随请求提交 -->
          <el-input v-model="editForm.code" disabled placeholder="请输入线体编码" />
        </el-form-item>
        <el-form-item label="线体名称" prop="name">
          <el-input
            v-model="editForm.name"
            placeholder="请输入线体名称"
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
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            maxlength="500"
            show-word-limit
            placeholder="请输入描述"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitEdit" :loading="editSubmitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { queryLineList, addLine, editLine, type LineQueryRequest, type LineResponse, type LineAddRequest } from '@/api/line'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

// 查询参数
const queryParams = reactive<LineQueryRequest>({
  code: '',
  name: '',
  enable: undefined,
  pageNum: 1,
  pageSize: 10
})

// 表格数据
const tableData = ref<LineResponse[]>([])
const total = ref(0)
const loading = ref(false)

// 多选相关
const selectedRows = ref<LineResponse[]>([])

// 新增弹窗
const addDialogVisible = ref(false)
const addFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const addForm = reactive<LineAddRequest>({
  code: '',
  name: '',
  description: '',
  enable: 1
})

// 编码格式校验（仅允许字母、数字、下划线、中划线；纯空白视为为空），与后端规则一致
const validateCode = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (value === null || value === undefined || value.trim() === '') {
    callback(new Error('请输入线体编码'))
    return
  }
  if (!/^[a-zA-Z0-9_-]+$/.test(value)) {
    callback(new Error('线体编码只能包含字母、数字、下划线和中划线'))
  } else {
    callback()
  }
}

// 名称校验（纯空白视为为空），与后端规则一致
const validateName = (_rule: any, value: string, callback: (error?: Error) => void) => {
  if (value === null || value === undefined || value.trim() === '') {
    callback(new Error('请输入线体名称'))
  } else {
    callback()
  }
}

// 新增表单校验规则，与后端规则一致
const addFormRules: FormRules = {
  code: [
    { required: true, message: '请输入线体编码', trigger: 'blur' },
    { max: 50, message: '线体编码长度不能超过50个字符', trigger: 'blur' },
    { validator: validateCode, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入线体名称', trigger: 'blur' },
    { max: 100, message: '线体名称长度不能超过100个字符', trigger: 'blur' },
    { validator: validateName, trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述长度不能超过500个字符', trigger: 'blur' }
  ]
}

// 编辑弹窗
const editDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()
const editSubmitLoading = ref(false)
const editForm = reactive({
  id: 0,
  // 编码仅用于只读展示，不会随编辑请求提交
  code: '',
  name: '',
  description: '',
  enable: 1
})

// 编辑表单校验规则：编码不可修改，故不校验也不提交编码
const editFormRules: FormRules = {
  name: [
    { required: true, message: '请输入线体名称', trigger: 'blur' },
    { max: 100, message: '线体名称长度不能超过100个字符', trigger: 'blur' },
    { validator: validateName, trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述长度不能超过500个字符', trigger: 'blur' }
  ]
}

// 查询线体列表
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await queryLineList(queryParams)
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('查询线体失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置查询（清空三个查询条件并回到第一页）
const handleReset = () => {
  queryParams.code = ''
  queryParams.name = ''
  queryParams.enable = undefined
  queryParams.pageNum = 1
  queryParams.pageSize = 10
  handleQuery()
}

// 多选变化
const handleSelectionChange = (rows: LineResponse[]) => {
  selectedRows.value = rows
}

// 打开新增弹窗
const handleAdd = () => {
  addForm.code = ''
  addForm.name = ''
  addForm.description = ''
  addForm.enable = 1
  addFormRef.value?.clearValidate()
  addDialogVisible.value = true
}

// 提交新增
const handleSubmitAdd = async () => {
  if (!addFormRef.value) return
  // 校验不通过（前端规则：必填、长度、编码格式）时不发起请求
  let valid = false
  try {
    valid = await addFormRef.value.validate()
  } catch {
    valid = false
  }
  if (!valid) return

  submitLoading.value = true
  try {
    await addLine({
      code: addForm.code.trim(),
      name: addForm.name.trim(),
      description: addForm.description,
      enable: addForm.enable
    })
    ElMessage.success('新增线体成功')
    addDialogVisible.value = false
    // 刷新列表，确保新记录立即可见
    handleQuery()
  } catch (error: any) {
    ElMessage.error(error.message || '新增线体失败')
  } finally {
    submitLoading.value = false
  }
}

// 打开编辑弹窗：预填当前行的名称、描述与状态（编码只读展示）
const handleEdit = (row: LineResponse) => {
  editForm.id = row.id
  editForm.code = row.code
  editForm.name = row.name
  editForm.description = row.description ?? ''
  editForm.enable = row.enable
  editFormRef.value?.clearValidate()
  editDialogVisible.value = true
}

// 提交编辑
const handleSubmitEdit = async () => {
  if (!editFormRef.value) return
  // 校验不通过（前端规则：名称必填与长度）时不发起请求
  let valid = false
  try {
    valid = await editFormRef.value.validate()
  } catch {
    valid = false
  }
  if (!valid) return

  editSubmitLoading.value = true
  try {
    // 编码不在请求体中：编码是业务唯一标识，编辑时不可修改
    await editLine({
      id: editForm.id,
      name: editForm.name.trim(),
      description: editForm.description,
      enable: editForm.enable
    })
    ElMessage.success('编辑线体成功')
    editDialogVisible.value = false
    // 刷新列表，确保更新后的名称、描述与状态立即可见
    handleQuery()
  } catch (error: any) {
    ElMessage.error(error.message || '编辑线体失败')
  } finally {
    editSubmitLoading.value = false
  }
}

// 初始化
onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.line-container {
  padding: 20px;
}

.query-card {
  margin-bottom: 20px;
}

.action-placeholder {
  color: var(--content-c4);
}
</style>
