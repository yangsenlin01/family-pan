<template>
  <div class="mobile-files">
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <van-cell-group>
        <van-cell v-for="file in files" :key="file.id" :title="file.fileName" :label="formatSize(file.fileSize)" is-link @click="onFileClick(file)" />
      </van-cell-group>
    </van-pull-refresh>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { fileApi } from "@/api/file";

const files = ref<any[]>([]);
const refreshing = ref(false);

onMounted(() => fetchFiles());

async function fetchFiles(parentId = 0) {
  const res = await fileApi.list({ parentId, size: 50 });
  files.value = res.data.data.list;
}
function formatSize(bytes: number): string {
  if (!bytes) return "-";
  if (bytes < 1048576) return (bytes/1024).toFixed(1)+" KB";
  return (bytes/1048576).toFixed(1)+" MB";
}
function onFileClick(file: any) {
  if (file.isDir) fetchFiles(file.id);
}
async function onRefresh() {
  await fetchFiles();
  refreshing.value = false;
}
</script>
