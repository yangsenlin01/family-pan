<template>
  <div class="side-menu">
    <div class="logo">家庭云盘</div>
    <div class="menu-items">
      <div
        v-for="item in menuItems"
        :key="item.key"
        class="menu-item"
        :class="{ active: currentRoute === item.key }"
        @click="navigate(item.key)"
      >
        <n-icon size="20"><component :is="item.icon" /></n-icon>
        <span class="item-label">{{ item.label }}</span>
      </div>
    </div>
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
import { computed } from "vue";
import { useRouter } from "vue-router";
import { useUserStore } from "@/stores/user";
import { ImageOutline, FolderOutline, SettingsOutline } from "@vicons/ionicons5";

const router = useRouter();
const userStore = useUserStore();

const menuItems = [
  { label: "照片", key: "desktop-photos", icon: ImageOutline },
  { label: "文件", key: "desktop-files", icon: FolderOutline },
  { label: "设置", key: "desktop-settings", icon: SettingsOutline },
];

const currentRoute = computed(() => router.currentRoute.value.name);

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
  border-bottom: 1px solid var(--n-border-color);
}
.menu-items {
  flex: 0;
  padding: 8px;
}
.menu-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  color: var(--n-text-color-2);
  transition: all 0.2s;
  margin-bottom: 2px;
}
.menu-item:hover {
  background: var(--n-color-hover);
  color: var(--n-text-color);
}
.menu-item.active {
  background: var(--n-color-target);
  color: var(--n-color-targeted);
  font-weight: 500;
}
.item-label {
  font-size: 14px;
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
