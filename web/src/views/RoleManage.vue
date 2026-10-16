<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.name" placeholder="角色名称" clearable style="width: 180px" @keyup.enter="load" />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button v-permission="'system:role:add'" type="success" @click="openCreate">新增角色</el-button>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="角色名称" width="160" />
      <el-table-column prop="code" label="角色标识" width="160" />
      <el-table-column label="数据范围" width="160">
        <template #default="{ row }">
          <el-tag>{{ scopeName(row.dataScope) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="remark" label="备注" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:role:edit'" link type="primary" @click="openEdit(row)">修改</el-button>
          <el-button v-permission="'system:role:assign'" link type="primary" @click="openAssign(row)">分配权限</el-button>
          <el-button v-permission="'system:role:delete'" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
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

  <el-dialog v-model="dialogVisible" :title="form.id ? '修改角色' : '新增角色'" width="520px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="角色名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="角色标识"><el-input v-model="form.code" :disabled="!!form.id" /></el-form-item>
      <el-form-item label="数据范围">
        <el-select v-model="form.dataScope" style="width: 100%">
          <el-option label="全部数据" :value="1" />
          <el-option label="本部门数据" :value="2" />
          <el-option label="本部门及以下数据" :value="3" />
          <el-option label="仅本人数据" :value="4" />
        </el-select>
      </el-form-item>
      <el-form-item label="备注"><el-input v-model="form.remark" /></el-form-item>
      <el-form-item v-if="dialogVisible" label="权限点">
        <el-tree
          ref="treeRef"
          :data="permissionTree"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          show-checkbox
          :default-checked-keys="checkedKeys"
          style="max-height: 260px; overflow: auto; width: 100%"
        />
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const dialogVisible = ref(false)
const permissionTree = ref([])
const checkedKeys = ref([])
const treeRef = ref()
const query = reactive({ pageNo: 1, pageSize: 10, name: '' })
const form = reactive({ id: null, name: '', code: '', dataScope: 4, remark: '', permissionIds: [] })

const SCOPE = { 1: '全部数据', 2: '本部门数据', 3: '本部门及以下', 4: '仅本人数据' }
const scopeName = (code) => SCOPE[code] || '-'

async function load() {
  loading.value = true
  try {
    const data = await request.get('/system/role/page', { params: query })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

async function ensurePermissionTree() {
  if (!permissionTree.value.length) {
    permissionTree.value = await request.get('/system/permission/tree')
  }
}

async function openCreate() {
  await ensurePermissionTree()
  Object.assign(form, { id: null, name: '', code: '', dataScope: 4, remark: '' })
  checkedKeys.value = []
  dialogVisible.value = true
  await nextTick()
  treeRef.value?.setCheckedKeys([])
}

async function openEdit(row) {
  await ensurePermissionTree()
  Object.assign(form, { id: row.id, name: row.name, code: row.code, dataScope: row.dataScope, remark: row.remark })
  checkedKeys.value = await request.get(`/system/role/${row.id}/permissions`)
  dialogVisible.value = true
  await nextTick()
  treeRef.value?.setCheckedKeys(checkedKeys.value)
}

async function openAssign(row) {
  await openEdit(row)
}

async function save() {
  const permissionIds = [
    ...(treeRef.value?.getCheckedKeys() || []),
    ...(treeRef.value?.getHalfCheckedKeys() || [])
  ]
  const payload = { ...form, permissionIds }
  if (form.id) {
    await request.put('/system/role', payload)
  } else {
    await request.post('/system/role', payload)
  }
  ElMessage.success('保存成功（相关用户权限缓存已失效）')
  dialogVisible.value = false
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除角色 ${row.name}？`, '提示', { type: 'warning' })
  await request.delete(`/system/role/${row.id}`)
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
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
