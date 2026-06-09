<template>
  <div class="file-manager">
    <div class="toolbar">
      <n-button @click="showUpload = true">上传文件</n-button>
      <n-button @click="showCreateFolder = true">新建文件夹</n-button>
      <n-select v-model:value="filterType" :options="typeOptions" placeholder="类型筛选" clearable style="width:140px" @update:value="onFilter" />
    </div>
    <FileTable :files="fileStore.files" :loading="fileStore.loading" @delete="onDelete" @click="onFileClick" />
    <n-pagination v-model:page="page" :item-count="fileStore.total" :page-size="30" @update:page="onPageChange" />

    <n-modal v-model:show="showUpload" title="上传文件" preset="card" style="width:500px">
      <n-upload multiple :action="uploadUrl" :headers="uploadHeaders" :max-size="100 * 1024 * 1024" :data="{ parentId: fileStore.currentParentId }"
        @finish="onUploadFinish"
        @error="onUploadError">
        <n-button>选择文件</n-button>
      </n-upload>
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
import { ref, computed, onMounted } from "vue";
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
const uploadHeaders = computed(() => ({ Authorization: userStore.accessToken }));

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
function onUploadError({ file, event }: any) {
  console.error("Upload failed:", file?.name, event);
  // Try to extract error message from response
  if (event?.target?.response) {
    try {
      const r = JSON.parse(event.target.response);
      alert("上传失败: " + (r.message || "未知错误"));
    } catch {
      alert("上传失败: " + file?.name);
    }
  } else {
    alert("上传失败: " + (file?.name || "未知错误"));
  }
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
