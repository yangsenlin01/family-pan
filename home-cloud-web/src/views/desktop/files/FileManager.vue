<template>
  <div class="file-manager">
    <div class="toolbar">
      <n-button @click="showUpload = true">上传文件</n-button>
      <n-button @click="showCreateFolder = true">新建文件夹</n-button>
      <n-select v-model:value="filterType" :options="typeOptions" placeholder="类型筛选" clearable style="width:140px" @update:value="onFilter" />
    </div>
    <FileTable :files="fileStore.files" :loading="fileStore.loading" @delete="onDelete" @click="onFileClick" />
    <n-pagination v-model:page="page" :item-count="fileStore.total" :page-size="30" @update:page="onPageChange" />

    <n-modal v-model:show="showUpload" title="上传文件">
      <n-upload multiple :action="uploadUrl" :headers="uploadHeaders" @finish="onUploadFinish" />
    </n-modal>
    <n-modal v-model:show="showCreateFolder" title="新建文件夹">
      <n-space vertical>
        <n-input v-model:value="newFolderName" placeholder="文件夹名称" />
        <n-button @click="onCreateFolder">创建</n-button>
      </n-space>
    </n-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { useFileStore } from "@/stores/file";
import { useUserStore } from "@/stores/user";
import FileTable from "@/components/desktop/FileTable.vue";

const fileStore = useFileStore();
const userStore = useUserStore();
const showUpload = ref(false);
const showCreateFolder = ref(false);
const newFolderName = ref("");
const page = ref(1);
const filterType = ref<string | null>(null);

const uploadUrl = "/api/v1/files/upload";
const uploadHeaders = { Authorization: userStore.accessToken };

const typeOptions = [
  { label: "全部", value: null },
  { label: "图片", value: "IMAGE" },
  { label: "视频", value: "VIDEO" },
  { label: "文档", value: "DOCUMENT" },
];

onMounted(() => fileStore.fetchFiles(0));

function onFilter(value: string | null) {
  fileStore.fetchFiles(fileStore.currentParentId, 1, 30, value || undefined);
}
function onDelete(id: number) { fileStore.deleteFile(id); }
function onFileClick(item: any) {
  if (item.isDir) fileStore.fetchFiles(item.id);
}
function onPageChange(p: number) {
  page.value = p;
  fileStore.fetchFiles(fileStore.currentParentId, p);
}
function onUploadFinish() {
  fileStore.fetchFiles(fileStore.currentParentId);
  showUpload.value = false;
}
async function onCreateFolder() {
  if (newFolderName.value.trim()) {
    await fileStore.createFolder(fileStore.currentParentId, newFolderName.value.trim());
    newFolderName.value = "";
    showCreateFolder.value = false;
  }
}
</script>

<style scoped>
.file-manager { padding: 16px; }
.toolbar { display: flex; gap: 8px; margin-bottom: 16px; }
</style>
