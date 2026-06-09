<template>
  <n-layout class="desktop-layout" has-sider>
    <n-layout-sider bordered collapse-mode="width" :collapsed-width="64" :width="200">
      <SideMenu />
    </n-layout-sider>
    <n-layout-content>
      <div class="content-area">
        <router-view />
      </div>
    </n-layout-content>
  </n-layout>
</template>

<script setup lang="ts">
import SideMenu from "@/components/desktop/SideMenu.vue";
</script>

<style scoped>
.desktop-layout { height: 100vh; }
.content-area { padding: 16px; height: 100%; overflow-y: auto; }
</style>
