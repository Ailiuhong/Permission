<template>
  <el-card>
    <div class="toolbar">
      <el-button type="primary" @click="load">刷新</el-button>
      <el-button v-permission="'system:permission:add'" type="success" @click="openCreate(null)">新增权限</el-button>
      <span class="tip">权限编码规范：模块:资源:操作，菜单与按钮共用同一套编码</span>
    </div>
    <el-table v-loading="loading" :data="rows" row-key="id" border default-expand-all>
      <el-table-column prop="name" label="权限名称" width="220" />
      <el-table-column prop="code" label="权限编码" width="240" />
      <el-table-column label="类型" width="100">
        <template #default="{ row }">
          <el-tag :type="row.type === 1 ? 'primary' : row.type === 2 ? 'warning' : 'info'">
            {{ typeName(row.type) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="path" label="路由地址" width="200" />
      <el-table-column prop="sort" label="排序" width="80" />
      <el-table-column label="操作" width="200">
        <template #default="{ row }">
          <el-button v-permission="'system:permission:edit'" link type="primary" @click="openEdit(row)">修改</el-button>
          <el-button v-permission="'system:permission:delete'" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '修改权限' : '新增权限'" width="520px">
    <el-form :model="form" label-width="100px">
      <el-form-item label="权限名称"><el-input v-model="form.name" /></el-form-item>
      <el-form-item label="权限编码"><el-input v-model="form.code" placeholder="如 system:user:export" /></el-form-item>
      <el-form-item label="类型">
        <el-radio-group v-model="form.type">
          <el-radio :value="1">菜单</el-radio>
          <el-radio :value="2">按钮</el-radio>
          <el-radio :value="3">接口</el-radio>
        </el-radio-group>
      </el-form-item>
      <el-form-item label="上级权限">
        <el-tree-select
          v-model="form.parentId"
          :data="parentOptions"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          check-strictly
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="路由地址"><el-input v-model="form.path" /></el-form-item>
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
const form = reactive({ id: null, name: '', code: '', type: 2, parentId: 0, path: '', sort: 0, status: 1 })

const TYPE = { 1: '菜单', 2: '按钮', 3: '接口' }
const typeName = (type) => TYPE[type] || '-'
const parentOptions = computed(() => [{ id: 0, name: '顶级', children: rows.value }])

async function load() {
  loading.value = true
  try {
    rows.value = await request.get('/system/permission/tree')
  } finally {
    loading.value = false
  }
}

function openCreate(parent) {
  Object.assign(form, { id: null, name: '', code: '', type: 2, parentId: parent?.id || 0, path: '', sort: 0, status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  Object.assign(form, { ...row, parentId: row.parentId ?? 0 })
  dialogVisible.value = true
}

async function save() {
  if (form.id) {
    await request.put('/system/permission', form)
  } else {
    await request.post('/system/permission', form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除权限 ${row.name}？`, '提示', { type: 'warning' })
  await request.delete(`/system/permission/${row.id}`)
  ElMessage.success('删除成功')
  await load()
}

onMounted(load)
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
}
.tip {
  font-size: 12px;
  color: #909399;
}
</style>
