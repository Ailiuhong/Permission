import { reactive } from 'vue'
import request from '../api/request'

/**
 * 极简用户状态：token 放 localStorage，权限点与菜单登录后从后端拉取
 */
export const userStore = reactive({
  loaded: false,
  userId: null,
  nickname: '',
  roles: [],
  permissions: [],
  menus: [],

  get accessToken() {
    return localStorage.getItem('accessToken') || ''
  },

  setTokens(accessToken, refreshToken) {
    localStorage.setItem('accessToken', accessToken)
    localStorage.setItem('refreshToken', refreshToken)
  },

  clearTokens() {
    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
  },

  has(permissionCode) {
    if (!permissionCode) return true
    return this.permissions.includes(permissionCode)
  },

  async loadProfile() {
    const data = await request.get('/auth/me')
    this.userId = data.userId
    this.nickname = data.nickname
    this.roles = data.roles || []
    this.permissions = data.permissions || []
    this.menus = data.menus || []
    this.loaded = true
    return data
  },

  /** 只清理内存中的登录态，不动 localStorage 里的令牌 */
  reset() {
    this.loaded = false
    this.userId = null
    this.nickname = ''
    this.permissions = []
    this.menus = []
    this.roles = []
  }
})
