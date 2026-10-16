<template>
  <el-card>
    <div class="toolbar">
      <el-input v-model="query.username" placeholder="账号" clearable style="width: 160px" @keyup.enter="load" />
      <el-input v-model="query.nickname" placeholder="姓名" clearable style="width: 160px" @keyup.enter="load" />
      <el-tree-select
        v-model="query.orgId"
        :data="orgTree"
        :props="{ label: 'name', children: 'children' }"
        node-key="id"
        check-strictly
        clearable
        placeholder="所属组织"
        style="width: 200px"
      />
      <el-button type="primary" @click="load">查询</el-button>
      <el-button v-permission="'system:user:add'" type="success" @click="openCreate">新增用户</el-button>
      <span class="tip">当前数据权限：{{ scopeTip }}</span>
    </div>

    <el-table v-loading="loading" :data="rows" border stripe>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="username" label="账号" width="140" />
      <el-table-column prop="nickname" label="姓名" width="160" />
      <el-table-column label="所属组织" width="140">
        <template #default="{ row }">{{ orgName(row.orgId) }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'info'">{{ row.status === 1 ? '正常' : '停用' }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="创建时间" width="180" />
      <el-table-column label="操作" width="320" fixed="right">
        <template #default="{ row }">
          <el-button v-permission="'system:user:edit'" link type="primary" @click="openEdit(row)">修改</el-button>
          <el-button v-permission="'system:user:assign'" link type="primary" @click="openAssign(row)">分配角色</el-button>
          <el-button v-permission="'system:user:edit'" link type="warning" @click="resetPassword(row)">重置密码</el-button>
          <el-button v-permission="'system:user:delete'" link type="danger" @click="remove(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-pagination
      class="pager"
      layout="total, prev, pager, next"
      :total="total"
      :page-size="query.pageSize"
      :current-page="query.pageNo"
      @current-change="onPageChange"
    />
  </el-card>

  <el-dialog v-model="dialogVisible" :title="form.id ? '修改用户' : '新增用户'" width="480px">
    <el-form :model="form" label-width="90px">
      <el-form-item label="账号"><el-input v-model="form.username" :disabled="!!form.id" /></el-form-item>
      <el-form-item label="姓名"><el-input v-model="form.nickname" /></el-form-item>
      <el-form-item label="密码"><el-input v-model="form.password" placeholder="留空则默认 123456" /></el-form-item>
      <el-form-item label="所属组织">
        <el-tree-select
          v-model="form.orgId"
          :data="orgTree"
          :props="{ label: 'name', children: 'children' }"
          node-key="id"
          check-strictly
          style="width: 100%"
        />
      </el-form-item>
      <el-form-item label="角色">
        <el-select v-model="form.roleIds" multiple style="width: 100%">
          <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
        </el-select>
      </el-form-item>
      <el-form-item label="状态">
        <el-radio-group v-model="form.status">
          <el-radio :value="1">正常</el-radio>
          <el-radio :value="0">停用</el-radio>
        </el-radio-group>
      </el-form-item>
    </el-form>
    <template #footer>
      <el-button @click="dialogVisible = false">取消</el-button>
      <el-button type="primary" @click="save">保存</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../api/request'
import { userStore } from '../store/user'

const rows = ref([])
const total = ref(0)
const loading = ref(false)
const orgTree = ref([])
const roles = ref([])
const dialogVisible = ref(false)
const query = reactive({ pageNo: 1, pageSize: 10, username: '', nickname: '', orgId: null })
const form = reactive({ id: null, username: '', nickname: '', password: '', orgId: null, status: 1, roleIds: [] })

// 用一个简单映射把组织ID显示成名称
const orgMap = ref({})
const orgName = (id) => orgMap.value[id] || '-'

const scopeTip = ref('')

function flatten(nodes, map) {
  nodes.forEach((node) => {
    map[node.id] = node.name
    if (node.children?.length) flatten(node.children, map)
  })
}

async function load() {
  loading.value = true
  try {
    const data = await request.get('/system/user/page', { params: query })
    rows.value = data.records || []
    total.value = data.total || 0
  } finally {
    loading.value = false
  }
}

function onPageChange(page) {
  query.pageNo = page
  load()
}

function openCreate() {
  Object.assign(form, { id: null, username: '', nickname: '', password: '', orgId: null, status: 1, roleIds: [] })
  dialogVisible.value = true
}

async function openEdit(row) {
  const roleIds = await request.get(`/system/user/${row.id}/roles`)
  Object.assign(form, {
    id: row.id,
    username: row.username,
    nickname: row.nickname,
    password: '',
    orgId: row.orgId,
    status: row.status,
    roleIds
  })
  dialogVisible.value = true
}

async function openAssign(row) {
  await openEdit(row)
}

async function save() {
  if (form.id) {
    await request.put('/system/user', form)
  } else {
    await request.post('/system/user', form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  await load()
}

async function remove(row) {
  await ElMessageBox.confirm(`确认删除用户 ${row.nickname}？`, '提示', { type: 'warning' })
  await request.delete(`/system/user/${row.id}`)
  ElMessage.success('删除成功')
  await load()
}

async function resetPassword(row) {
  const { value } = await ElMessageBox.prompt('请输入新密码', '重置密码', { inputValue: '123456' })
  await request.put(`/system/user/${row.id}/password`, { password: value })
  ElMessage.success('密码已重置')
}

onMounted(async () => {
  const tree = await request.get('/system/org/tree')
  orgTree.value = tree
  const map = {}
  flatten(tree, map)
  orgMap.value = map
  roles.value = await request.get('/system/role/list')
  scopeTip.value = userStore.roles.includes('admin') ? '全部数据' : '按角色数据范围过滤'
  await load()
})
</script>

<style scoped>
.toolbar {
  display: flex;
  gap: 8px;
  align-items: center;
  margin-bottom: 12px;
  flex-wrap: wrap;
}
.tip {
  margin-left: auto;
  font-size: 12px;
  color: #909399;
}
.pager {
  margin-top: 12px;
  justify-content: flex-end;
}
</style>
