import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  { path: '/login', component: () => import('../views/Login.vue') },
  {
    path: '/',
    component: () => import('../layout/Layout.vue'),
    redirect: '/system/user',
    children: [
      { path: 'system/user', name: 'UserManage', component: () => import('../views/UserManage.vue'), meta: { permission: 'system:user:list' } },
      { path: 'system/role', name: 'RoleManage', component: () => import('../views/RoleManage.vue'), meta: { permission: 'system:role:list' } },
      { path: 'system/permission', name: 'PermissionManage', component: () => import('../views/PermissionManage.vue'), meta: { permission: 'system:permission:list' } },
      { path: 'system/org', name: 'OrgManage', component: () => import('../views/OrgManage.vue'), meta: { permission: 'system:org:list' } },
      { path: 'system/audit', name: 'AuditLog', component: () => import('../views/AuditLog.vue'), meta: { permission: 'system:audit:list' } }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach(async (to) => {
  if (to.path === '/login') return true
  const token = localStorage.getItem('accessToken')
  if (!token) return '/login'

  const { userStore } = await import('../store/user')
  if (!userStore.loaded) {
    try {
      await userStore.loadProfile()
    } catch (e) {
      return '/login'
    }
  }
  // 路由级权限：没有该菜单权限就不允许进入
  if (to.meta?.permission && !userStore.has(to.meta.permission)) {
    return { path: '/system/user', replace: true }
  }
  return true
})

export default router
