<template>
  <div style="display:none" />
</template>

<script setup lang="ts">
import { useMessage } from "naive-ui";

const message = useMessage();
(window as any).$message = message;
</script>
