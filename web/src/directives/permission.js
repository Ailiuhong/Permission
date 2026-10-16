import { userStore } from '../store/user'

/**
 * 按钮级权限：v-permission="'system:user:delete'"
 * 注意：前端隐藏按钮只是体验优化，后端 @PreAuthorize 才是真正的防线
 */
export default {
  mounted(el, binding) {
    if (!userStore.has(binding.value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
