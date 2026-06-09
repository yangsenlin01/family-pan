<template>
  <div class="side-menu">
    <div class="logo">家庭云盘</div>
    <n-menu :value="currentRoute" :options="menuOptions" @update:value="navigate" />
    <div class="user-section">
      <n-dropdown :options="userOptions" @select="handleUserAction">
        <div class="user-info">
          <n-avatar size="small">{{ userStore.userInfo?.nickname?.charAt(0) || "U" }}</n-avatar>
          <span class="username">{{ userStore.userInfo?.nickname }}</span>
        </div>
      </n-dropdown>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, h } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import type { Component } from "vue";
import { NIcon } from "naive-ui";
import { ImageOutline, FolderOutline, SettingsOutline } from "@vicons/ionicons5";

const router = useRouter();
const userStore = useUserStore();

function renderIcon(icon: Component) {
  return () => h(NIcon, null, { default: () => h(icon) });
}

const menuOptions = [
  { label: "照片", key: "desktop-photos", icon: renderIcon(ImageOutline) },
  { label: "文件", key: "desktop-files", icon: renderIcon(FolderOutline) },
  { label: "设置", key: "desktop-settings", icon: renderIcon(SettingsOutline) },
];

const currentRoute = computed(() => router.currentRoute.value.name as string);

const userOptions = [
  { label: "修改密码", key: "password" },
  { label: "退出登录", key: "logout" },
];

function navigate(key: string) {
  router.push({ name: key });
}

function handleUserAction(key: string) {
  if (key === "logout") {
    userStore.logout();
    router.push("/login");
  } else if (key === "password") {
    router.push({ name: "desktop-settings" });
  }
}
</script>

<style scoped>
.side-menu {
  height: 100vh;
  display: flex;
  flex-direction: column;
}
.logo {
  padding: 20px 16px;
  font-size: 18px;
  font-weight: bold;
  color: var(--n-text-color);
  text-align: center;
}
.user-section {
  margin-top: auto;
  padding: 12px;
  border-top: 1px solid var(--n-border-color);
}
.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}
.username { font-size: 14px; }
</style>
