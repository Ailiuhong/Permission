<template>
  <el-container style="height: 100vh">
    <el-aside width="220px" class="aside">
      <div class="logo">权限管理系统</div>
      <el-menu :default-active="$route.path" router background-color="#001529" text-color="#c9d1d9" active-text-color="#fff">
        <template v-for="menu in menus" :key="menu.id">
          <el-sub-menu v-if="menu.children && menu.children.length" :index="String(menu.id)">
            <template #title>{{ menu.name }}</template>
            <el-menu-item v-for="child in menu.children" :key="child.id" :index="child.path">{{ child.name }}</el-menu-item>
          </el-sub-menu>
          <el-menu-item v-else :index="menu.path">{{ menu.name }}</el-menu-item>
        </template>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="header">
        <span class="role">角色：{{ userStore.roles.join('、') || '无' }}</span>
        <span class="perm">权限点数：{{ userStore.permissions.length }}</span>
        <el-button link type="primary" @click="logout">退出登录</el-button>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import request from '../api/request'
import { userStore } from '../store/user'

const router = useRouter()
// 菜单由后端按当前用户权限返回，没权限的菜单不会出现
const menus = computed(() => userStore.menus)

async function logout() {
  try {
    await request.post('/auth/logout', { refreshToken: localStorage.getItem('refreshToken') })
  } finally {
    userStore.reset()
    userStore.clearTokens()
    router.push('/login')
  }
}
</script>

<style scoped>
.aside {
  background: #001529;
}
.logo {
  color: #fff;
  font-weight: 600;
  text-align: center;
  line-height: 60px;
}
.header {
  display: flex;
  align-items: center;
  gap: 16px;
  background: #fff;
  border-bottom: 1px solid #ebeef5;
}
.role,
.perm {
  font-size: 13px;
  color: #606266;
}
</style>
