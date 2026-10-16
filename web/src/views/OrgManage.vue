<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="load">刷新</el-button>
      <el-button v-permission="'system:org:add'" type="success" @click="openCreate(null)">新增组织</el-button>
    </div>
    <el-table v-loading="loading" :data="rows" row-key="id" border default-expand-all>
      <el-table-column prop="name" label="组织名称" width="240" />
      <el-table-column prop="id" label="ID" width="100" />
      <el-table-column prop="ancestors" label="祖级路径" width="200" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="220">
        <template #default="{ row }">
          <el-button v-permission="'system:org:add'" link type="primary" @click="openCreate(row)">新增下级</el-button>
          <el-button v-permission="'system:org:edit'" link type="primary" @click="openEdit(row)">修改</el-button>
          <el-button v-permission="'system:org:delete'" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '修改组织' : '新增组织'" width="460px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="组织名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="上级组织">
        <el-tree-select
          v-model="form.parentId"
          :data="parentOptions"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          check-strictly
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="排序"><el-input-number v-model="form.sort" /></el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const rows = ref([])
const loading = ref(false)
const dialogVisible = ref(false)
const form = reactive({ id: null, name: '', parentId: 0, sort: 0, status: 1 })
const parentOptions = computed(() => [{ id: 0, name: '顶级', children: rows.value }])

async function load() {
  loading.value = true
  try {
    rows.value = await request.get('/system/org/tree')
  } finally {
    loading.value = false
  }
}

function openCreate(parent) {
  Object.assign(form, { id: null, name: '', parentId: parent?.id || 0, sort: 0, status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { id: row.id, name: row.name, parentId: row.parentId, sort: row.sort, status: row.status })
  dialogVisible.value = true
}

async function save() {
  if (form.id) {
    await request.put('/system/org', form)
  } else {
    await request.post('/system/org', form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除组织 ${row.name}？`, '提示', { type: 'warning' })
  await request.delete(`/system/org/${row.id}`)
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  margin-bottom: 12px;
}
</style>
