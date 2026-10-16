<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.username" placeholder="操作人" clearable style="width: 160px" @keyup.enter="load" />
      <el-input v-model="query.module" placeholder="模块" clearable style="width: 160px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="操作人" width="120" />
      <el-table-column prop="module" label="模块" width="120" />
      <el-table-column prop="operation" label="操作" width="160" />
      <el-table-column prop="requestMethod" label="方法" width="90" />
      <el-table-column prop="requestUri" label="接口" width="240" />
      <el-table-column label="结果" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">{{ row.status === 1 ? '成功' : '失败' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="costTime" label="耗时(ms)" width="110" />
      <el-table-column prop="ip" label="IP" width="140" />
      <el-table-column prop="createTime" label="时间" width="180" />
      <el-table-column prop="params" label="参数" show-overflow-tooltip />
    </el-table>

    <el-pagination
      class="pager"
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.pageNo"
      @current-change="(p) => { query.pageNo = p; load() }"
    />
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import request from '../api/request'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const query = reactive({ pageNo: 1, pageSize: 10, username: '', module: '' })

async function load() {
  loading.value = true
  try {
    const data = await request.get('/system/audit/page', { params: query })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
