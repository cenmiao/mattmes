<template>
  <div class="material-container">
    <!-- 查询表单 -->
    <el-card class="query-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="料号编码">
          <el-input
            v-model="queryParams.code"
            placeholder="请输入料号编码"
            clearable
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="料号名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入料号名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="所属项目">
          <el-select
            v-model="queryParams.projectId"
            placeholder="请选择项目"
            clearable
            style="width: 200px"
          >
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="`${project.code} - ${project.name}`"
              :value="project.id"
            />
          </el-select>
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
          <el-button type="success" @click="handleAdd" v-permission="'material:add'">
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
      >
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="code" label="料号编码" width="150" />
        <el-table-column prop="name" label="料号名称" width="180" />
        <el-table-column prop="projectName" label="所属项目" width="180" show-overflow-tooltip />
        <el-table-column prop="color" label="颜色" width="120" />
        <el-table-column prop="size" label="尺码" width="120" />
        <el-table-column prop="enable" label="启用状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.enable === 1" type="success">启用</el-tag>
            <el-tag v-else type="danger">禁用</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdBy" label="创建人" width="120" />
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" link size="small" @click="handleEdit(row)" v-permission="'material:edit'">
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
      title="新增料号"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addFormRules"
        label-width="120px"
      >
        <el-form-item label="料号编码" prop="code">
          <el-input
            v-model="addForm.code"
            placeholder="请输入料号编码"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="料号名称" prop="name">
          <el-input
            v-model="addForm.name"
            placeholder="请输入料号名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="所属项目" prop="projectId">
          <el-select
            v-model="addForm.projectId"
            placeholder="请选择项目"
            style="width: 100%"
          >
            <el-option
              v-for="project in projectList"
              :key="project.id"
              :label="`${project.code} - ${project.name}`"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-input
            v-model="addForm.color"
            placeholder="请输入颜色"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="尺码" prop="size">
          <el-input
            v-model="addForm.size"
            placeholder="请输入尺码"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="通用规格1" prop="spec1">
          <el-input
            v-model="addForm.spec1"
            placeholder="请输入通用规格1"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="通用规格2" prop="spec2">
          <el-input
            v-model="addForm.spec2"
            placeholder="请输入通用规格2"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="通用规格3" prop="spec3">
          <el-input
            v-model="addForm.spec3"
            placeholder="请输入通用规格3"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="addForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="addForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
            maxlength="500"
            show-word-limit
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
      title="编辑料号"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="editFormRef"
        :model="editForm"
        :rules="editFormRules"
        label-width="120px"
      >
        <el-form-item label="料号编码">
          <el-input
            v-model="editForm.code"
            disabled
            placeholder="料号编码不可修改"
          />
        </el-form-item>
        <el-form-item label="所属项目">
          <el-input
            v-model="editForm.projectName"
            disabled
            placeholder="所属项目不可修改"
          />
        </el-form-item>
        <el-form-item label="料号名称" prop="name">
          <el-input
            v-model="editForm.name"
            placeholder="请输入料号名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="颜色" prop="color">
          <el-input
            v-model="editForm.color"
            placeholder="请输入颜色"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="尺码" prop="size">
          <el-input
            v-model="editForm.size"
            placeholder="请输入尺码"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="通用规格1" prop="spec1">
          <el-input
            v-model="editForm.spec1"
            placeholder="请输入通用规格1"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="通用规格2" prop="spec2">
          <el-input
            v-model="editForm.spec2"
            placeholder="请输入通用规格2"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="通用规格3" prop="spec3">
          <el-input
            v-model="editForm.spec3"
            placeholder="请输入通用规格3"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="editForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="备注" prop="remark">
          <el-input
            v-model="editForm.remark"
            type="textarea"
            :rows="2"
            placeholder="请输入备注"
            maxlength="500"
            show-word-limit
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
import {
  queryMaterialList,
  addMaterial,
  editMaterial,
  type MaterialQueryRequest,
  type MaterialResponse,
  type MaterialAddRequest
} from '@/api/material'
import { listEnabledProjects, type ProjectSimpleResponse } from '@/api/project'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

// 查询参数
const queryParams = reactive<MaterialQueryRequest>({
  code: '',
  name: '',
  projectId: undefined,
  enable: undefined,
  pageNum: 1,
  pageSize: 10
})

// 表格数据
const tableData = ref<MaterialResponse[]>([])
const total = ref(0)
const loading = ref(false)

// 项目列表(用于下拉框)
const projectList = ref<ProjectSimpleResponse[]>([])

// 新增弹窗
const addDialogVisible = ref(false)
const addFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const addForm = reactive<MaterialAddRequest>({
  code: '',
  name: '',
  projectId: 0,
  color: '',
  size: '',
  spec1: '',
  spec2: '',
  spec3: '',
  description: '',
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
    { required: true, message: '请输入料号编码', trigger: 'blur' },
    { max: 50, message: '编码长度不能超过50个字符', trigger: 'blur' },
    { validator: validateCode, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入料号名称', trigger: 'blur' },
    { max: 100, message: '名称长度不能超过100个字符', trigger: 'blur' }
  ],
  projectId: [
    { required: true, message: '请选择所属项目', trigger: 'change' }
  ],
  color: [
    { max: 50, message: '颜色长度不能超过50个字符', trigger: 'blur' }
  ],
  size: [
    { max: 50, message: '尺码长度不能超过50个字符', trigger: 'blur' }
  ],
  spec1: [
    { max: 100, message: '通用规格长度不能超过100个字符', trigger: 'blur' }
  ],
  spec2: [
    { max: 100, message: '通用规格长度不能超过100个字符', trigger: 'blur' }
  ],
  spec3: [
    { max: 100, message: '通用规格长度不能超过100个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述长度不能超过500个字符', trigger: 'blur' }
  ],
  remark: [
    { max: 500, message: '备注长度不能超过500个字符', trigger: 'blur' }
  ]
}

// 编辑弹窗
const editDialogVisible = ref(false)
const editFormRef = ref<FormInstance>()
const editForm = reactive<{
  id: number
  code: string
  projectName: string
  name: string
  color: string
  size: string
  spec1: string
  spec2: string
  spec3: string
  description: string
  remark: string
}>({
  id: 0,
  code: '',
  projectName: '',
  name: '',
  color: '',
  size: '',
  spec1: '',
  spec2: '',
  spec3: '',
  description: '',
  remark: ''
})

// 编辑表单校验规则
const editFormRules: FormRules = {
  name: [
    { required: true, message: '请输入料号名称', trigger: 'blur' },
    { max: 100, message: '名称长度不能超过100个字符', trigger: 'blur' }
  ],
  color: [
    { max: 50, message: '颜色长度不能超过50个字符', trigger: 'blur' }
  ],
  size: [
    { max: 50, message: '尺码长度不能超过50个字符', trigger: 'blur' }
  ],
  spec1: [
    { max: 100, message: '通用规格长度不能超过100个字符', trigger: 'blur' }
  ],
  spec2: [
    { max: 100, message: '通用规格长度不能超过100个字符', trigger: 'blur' }
  ],
  spec3: [
    { max: 100, message: '通用规格长度不能超过100个字符', trigger: 'blur' }
  ],
  description: [
    { max: 500, message: '描述长度不能超过500个字符', trigger: 'blur' }
  ],
  remark: [
    { max: 500, message: '备注长度不能超过500个字符', trigger: 'blur' }
  ]
}

// 加载项目列表
const loadProjectList = async () => {
  try {
    const res = await listEnabledProjects()
    projectList.value = res
  } catch (error) {
    console.error('加载项目列表失败:', error)
  }
}

// 查询料号列表
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await queryMaterialList(queryParams)
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('查询料号失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  queryParams.code = ''
  queryParams.name = ''
  queryParams.projectId = undefined
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
  addForm.projectId = 0
  addForm.color = ''
  addForm.size = ''
  addForm.spec1 = ''
  addForm.spec2 = ''
  addForm.spec3 = ''
  addForm.description = ''
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
    await addMaterial(addForm)
    ElMessage.success('新增料号成功')
    addDialogVisible.value = false
    handleQuery() // 刷新列表
  } catch (error: any) {
    ElMessage.error(error.message || '新增料号失败')
  } finally {
    submitLoading.value = false
  }
}

// 打开编辑弹窗
const handleEdit = (row: MaterialResponse) => {
  // 填充表单数据
  editForm.id = row.id
  editForm.code = row.code
  editForm.projectName = row.projectName || ''
  editForm.name = row.name
  editForm.color = row.color || ''
  editForm.size = row.size || ''
  editForm.spec1 = row.spec1 || ''
  editForm.spec2 = row.spec2 || ''
  editForm.spec3 = row.spec3 || ''
  editForm.description = row.description || ''
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
    await editMaterial(editForm.id, {
      name: editForm.name,
      color: editForm.color,
      size: editForm.size,
      spec1: editForm.spec1,
      spec2: editForm.spec2,
      spec3: editForm.spec3,
      description: editForm.description,
      remark: editForm.remark
    })
    ElMessage.success('编辑料号成功')
    editDialogVisible.value = false
    handleQuery() // 刷新列表
  } catch (error: any) {
    ElMessage.error(error.message || '编辑料号失败')
  } finally {
    submitLoading.value = false
  }
}

// 初始化
onMounted(() => {
  loadProjectList()
  handleQuery()
})
</script>

<style scoped>
.material-container {
  padding: 20px;
}

.query-card {
  margin-bottom: 20px;
}

.table-card {
  /* 工业风格表格卡片 */
}
</style>