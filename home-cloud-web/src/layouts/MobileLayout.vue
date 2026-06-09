<template>
  <div class="mobile-layout">
    <div class="mobile-content">
      <router-view />
    </div>
    <van-tabbar v-model:active="active" @change="onChange">
      <van-tabbar-item icon="photo-o" name="photos">照片</van-tabbar-item>
      <van-tabbar-item icon="folder-o" name="files">文件</van-tabbar-item>
      <van-tabbar-item icon="user-o" name="me">我的</van-tabbar-item>
    </van-tabbar>
  </div>
</template>

<script setup lang="ts">
import { ref } from "vue";
import { useRouter } from "vue-router";

const router = useRouter();
const active = ref("photos");

function onChange(name: string) {
  router.push({ name: `mobile-${name}` as string });
}
</script>

<style scoped>
.mobile-layout { height: 100vh; display: flex; flex-direction: column; }
.mobile-content { flex: 1; overflow-y: auto; }
</style>
