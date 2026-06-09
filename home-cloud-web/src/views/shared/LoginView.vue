<template>
  <div class="login-container">
    <n-card class="login-card" :bordered="false">
      <h1 class="title">家庭云盘</h1>
      <n-tabs v-model:value="tab" type="line" animated>
        <n-tab-pane name="login" tab="登录">
          <n-form ref="loginFormRef" :model="loginForm" :rules="loginRules">
            <n-form-item path="username" label="用户名">
              <n-input v-model:value="loginForm.username" placeholder="请输入用户名" />
            </n-form-item>
            <n-form-item path="password" label="密码">
              <n-input v-model:value="loginForm.password" type="password" placeholder="请输入密码" />
            </n-form-item>
            <n-button type="primary" block :loading="loading" @click="handleLogin">登 录</n-button>
          </n-form>
        </n-tab-pane>
        <n-tab-pane name="register" tab="注册">
          <n-form ref="registerFormRef" :model="registerForm" :rules="registerRules">
            <n-form-item path="username" label="用户名">
              <n-input v-model:value="registerForm.username" placeholder="3-50位字母数字" />
            </n-form-item>
            <n-form-item path="password" label="密码">
              <n-input v-model:value="registerForm.password" type="password" placeholder="至少8位，含字母和数字" />
            </n-form-item>
            <n-form-item path="nickname" label="昵称">
              <n-input v-model:value="registerForm.nickname" placeholder="可选" />
            </n-form-item>
            <n-button type="primary" block :loading="loading" @click="handleRegister">注 册</n-button>
          </n-form>
        </n-tab-pane>
      </n-tabs>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { authApi } from "@/api/auth";
import { useMessage } from "naive-ui";

const router = useRouter();
const userStore = useUserStore();
const message = useMessage();

const tab = ref("login");
const loading = ref(false);

const loginForm = reactive({ username: "", password: "" });
const loginRules = {
  username: [{ required: true, message: "请输入用户名" }],
  password: [{ required: true, message: "请输入密码" }],
};

const registerForm = reactive({ username: "", password: "", nickname: "" });
const registerRules = {
  username: [
    { required: true, message: "请输入用户名" },
    { min: 3, max: 50, message: "用户名长度3-50位" },
  ],
  password: [
    { required: true, message: "请输入密码" },
    { min: 8, message: "密码至少8位" },
  ],
};

async function handleLogin() {
  loading.value = true;
  try {
    const res = await authApi.login(loginForm);
    const { accessToken, refreshToken, userInfo } = res.data.data;
    userStore.setToken(accessToken, refreshToken);
    userStore.setUserInfo(userInfo);
    message.success("登录成功");
    router.push("/");
  } catch (e: any) {
    message.error(e.response?.data?.message || "登录失败");
  } finally {
    loading.value = false;
  }
}

async function handleRegister() {
  loading.value = true;
  try {
    await authApi.register(registerForm);
    message.success("注册成功，请登录");
    tab.value = "login";
  } catch (e: any) {
    message.error(e.response?.data?.message || "注册失败");
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}
.login-card {
  width: 400px;
  border-radius: 12px;
}
.title {
  text-align: center;
  font-size: 24px;
  color: #333;
  margin-bottom: 16px;
}
</style>
