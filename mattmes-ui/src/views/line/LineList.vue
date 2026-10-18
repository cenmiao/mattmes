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
          <template #default>
            <span class="action-placeholder">—</span>
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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { queryLineList, type LineQueryRequest, type LineResponse } from '@/api/line'
import { Search, Refresh } from '@element-plus/icons-vue'

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
