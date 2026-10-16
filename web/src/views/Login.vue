<template>
  <div class="login-page">
    <el-card class="login-card">
      <template #header>
        <div class="title">RBAC 权限管理系统</div>
      </template>
      <el-form :model="form" @submit.prevent>
        <el-form-item>
          <el-input v-model="form.username" placeholder="账号，如 admin" size="large" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="form.password" type="password" placeholder="密码 123456" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" style="width: 100%" :loading="loading" @click="doLogin">登录</el-button>
      </el-form>
      <div class="tips">
        <p>演示账号（密码均为 123456）：</p>
        <p>admin 超级管理员｜zhangsan 研发主管｜lisi 后端工程师｜wangwu 财务专员</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '../api/request'
import { userStore } from '../store/user'

const router = useRouter()
const loading = ref(false)
const form = reactive({ username: 'admin', password: '123456' })

async function doLogin() {
  loading.value = true
  try {
    const data = await request.post('/auth/login', form)
    // 先清内存状态，再写入令牌，顺序反了会让后面的 /auth/me 变成裸请求
    userStore.reset()
    userStore.setTokens(data.accessToken, data.refreshToken)
    await userStore.loadProfile()
    ElMessage.success(`欢迎，${data.nickname}`)
    router.push('/system/user')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}
.login-card {
  width: 420px;
}
.title {
  font-size: 18px;
  font-weight: 600;
  text-align: center;
}
.tips {
  margin-top: 12px;
  font-size: 12px;
  color: #909399;
  line-height: 1.6;
}
</style>
