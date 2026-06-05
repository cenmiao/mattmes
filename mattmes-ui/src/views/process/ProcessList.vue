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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { queryProcessList, type ProcessQueryRequest, type ProcessResponse } from '@/api/process'
import { Search, Refresh } from '@element-plus/icons-vue'

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

// 查询工序列表
const handleQuery = async () => {
  loading.value = true
  try {
    const res = await queryProcessList(queryParams)
    tableData.value = res.data.list
    total.value = res.data.total
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