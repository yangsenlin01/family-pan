<template>
  <n-icon :size="20">
    <FolderOutline v-if="isDir" />
    <ImageOutline v-else-if="fileType === 'IMAGE'" />
    <VideocamOutline v-else-if="fileType === 'VIDEO'" />
    <DocumentOutline v-else />
  </n-icon>
</template>

<script setup lang="ts">
import { FolderOutline, ImageOutline, VideocamOutline, DocumentOutline } from "@vicons/ionicons5";
defineProps<{ fileType?: string; isDir?: number }>();
</script>
