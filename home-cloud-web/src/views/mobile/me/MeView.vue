<template>
  <div class="mobile-me">
    <div class="user-card">
      <van-image round width="60" height="60" src="" />
      <h3>{{ userStore.userInfo?.nickname }}</h3>
    </div>
    <van-cell-group>
      <van-cell title="存储空间" :value="storageText" />
      <van-cell title="修改密码" is-link @click="showPassword=true" />
      <van-cell title="退出登录" @click="logout" />
    </van-cell-group>
  </div>
</template>

<script setup lang="ts">
import { computed, ref } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";

const router = useRouter();
const userStore = useUserStore();
const showPassword = ref(false);
const storageText = computed(() => "0 / 50 GB");

function logout() { userStore.logout(); router.push("/login"); }
</script>

<style scoped>
.user-card { text-align: center; padding: 24px; }
.user-card h3 { margin-top: 8px; }
</style>
