<template>
  <n-config-provider :locale="zhCN" :date-locale="dateZhCN">
    <n-loading-bar-provider>
      <n-message-provider>
        <router-view />
      </n-message-provider>
    </n-loading-bar-provider>
  </n-config-provider>
</template>

<script setup lang="ts">
import { onMounted } from "vue";
import { zhCN, dateZhCN } from "naive-ui";
import { useRouter } from "vue-router";

const router = useRouter();

onMounted(() => {
  const isLoggedIn = !!localStorage.getItem("accessToken");
  if (!isLoggedIn) {
    router.push("/login");
  }
});
</script>
