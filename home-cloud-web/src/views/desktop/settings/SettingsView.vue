<template>
  <div class="settings">
    <h2>个人设置</h2>
    <n-card title="基本信息" style="max-width:600px;margin-top:16px">
      <p>用户名: {{ userStore.userInfo?.username }}</p>
      <p>昵称: {{ userStore.userInfo?.nickname }}</p>
    </n-card>
  </div>
</template>

<script setup lang="ts">
import { useUserStore } from "@/stores/user";
const userStore = useUserStore();
</script>

<style scoped>
.settings { padding: 16px; }
</style>
