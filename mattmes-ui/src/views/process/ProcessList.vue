<template>
  <div class="process-container">
    <!-- 查询表单 -->
    <el-card class="query-card">
      <el-form :model="queryParams" inline>
        <el-form-item label="工序编码">
          <el-input
            v-model="queryParams.code"
            placeholder="请输入工序编码"
            clearable
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="工序名称">
          <el-input
            v-model="queryParams.name"
            placeholder="请输入工序名称"
            clearable
            style="width: 200px"
          />
        </el-form-item>

        <el-form-item label="工序类型">
          <el-select
            v-model="queryParams.processType"
            placeholder="请选择工序类型"
            clearable
            style="width: 150px"
          >
            <el-option label="检测" value="INSPECTION" />
            <el-option label="组装" value="ASSEMBLY" />
            <el-option label="包装" value="PACKAGING" />
            <el-option label="其他" value="OTHER" />
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
          <el-button type="success" @click="handleAdd" v-permission="'process:add'">
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
        <el-table-column prop="code" label="工序编码" width="150" />
        <el-table-column prop="name" label="工序名称" width="180" />
        <el-table-column prop="processType" label="工序类型" width="120">
          <template #default="{ row }">
            <el-tag :type="getProcessTypeTagType(row.processType)">
              {{ getProcessTypeLabel(row.processType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="工序描述" min-width="200" show-overflow-tooltip />
        <el-table-column prop="enable" label="启用状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.enable === 1 ? 'success' : 'danger'">
              {{ row.enable === 1 ? '启用' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column prop="remark" label="备注" min-width="150" show-overflow-tooltip />
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
      title="新增工序"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="addFormRef"
        :model="addForm"
        :rules="addFormRules"
        label-width="100px"
      >
        <el-form-item label="工序编码" prop="code">
          <el-input
            v-model="addForm.code"
            placeholder="请输入工序编码"
            maxlength="50"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="工序名称" prop="name">
          <el-input
            v-model="addForm.name"
            placeholder="请输入工序名称"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="工序类型" prop="processType">
          <el-select
            v-model="addForm.processType"
            placeholder="请选择工序类型"
            style="width: 100%"
          >
            <el-option label="检测" value="INSPECTION" />
            <el-option label="组装" value="ASSEMBLY" />
            <el-option label="包装" value="PACKAGING" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="启用状态" prop="enable">
          <el-radio-group v-model="addForm.enable">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="工序描述" prop="description">
          <el-input
            v-model="addForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入工序描述"
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { queryProcessList, addProcess, type ProcessQueryRequest, type ProcessResponse, type ProcessAddRequest } from '@/api/process'
import { Search, Refresh, Plus } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import type { FormInstance, FormRules } from 'element-plus'

// 查询参数
const queryParams = reactive<ProcessQueryRequest>({
  code: '',
  name: '',
  processType: '',
  enable: undefined,
  pageNum: 1,
  pageSize: 10
})

// 表格数据
const tableData = ref<ProcessResponse[]>([])
const total = ref(0)
const loading = ref(false)

// 新增弹窗
const addDialogVisible = ref(false)
const addFormRef = ref<FormInstance>()
const submitLoading = ref(false)
const addForm = reactive<ProcessAddRequest>({
  code: '',
  name: '',
  processType: '',
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
    { required: true, message: '请输入工序编码', trigger: 'blur' },
    { max: 50, message: '编码长度不能超过50个字符', trigger: 'blur' },
    { validator: validateCode, trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入工序名称', trigger: 'blur' },
    { max: 100, message: '名称长度不能超过100个字符', trigger: 'blur' }
  ],
  processType: [
    { required: true, message: '请选择工序类型', trigger: 'change' }
  ]
}

// 查询工序列表
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await queryProcessList(queryParams)
    tableData.value = res.list
    total.value = res.total
  } catch (error) {
    console.error('查询工序失败:', error)
  } finally {
    loading.value = false
  }
}

// 重置查询
const handleReset = () => {
  queryParams.code = ''
  queryParams.name = ''
  queryParams.processType = ''
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
  addForm.processType = ''
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
    await addProcess(addForm)
    ElMessage.success('新增工序成功')
    addDialogVisible.value = false
    handleQuery() // 刷新列表
  } catch (error: any) {
    ElMessage.error(error.message || '新增工序失败')
  } finally {
    submitLoading.value = false
  }
}

// 工序类型标签颜色
const getProcessTypeTagType = (type: string) => {
  switch (type) {
    case 'INSPECTION':
      return 'primary'
    case 'ASSEMBLY':
      return 'warning'
    case 'PACKAGING':
      return 'success'
    default:
      return 'info'
  }
}

// 工序类型显示文本
const getProcessTypeLabel = (type: string) => {
  switch (type) {
    case 'INSPECTION':
      return '检测'
    case 'ASSEMBLY':
      return '组装'
    case 'PACKAGING':
      return '包装'
    default:
      return '其他'
  }
}

// 初始化
onMounted(() => {
  handleQuery()
})
</script>

<style scoped>
.process-container {
  padding: 20px;
}

.query-card {
  margin-bottom: 20px;
}

.table-card {
  /* 工业风格表格卡片 */
}
</style>