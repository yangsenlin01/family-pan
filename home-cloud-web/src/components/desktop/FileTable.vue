<template>
  <n-data-table :columns="columns" :data="files" :loading="loading" :row-key="(row: any) => row.id" />
</template>

<script setup lang="ts">
import { h } from "vue";
import { NButton } from "naive-ui";
import FileIcon from "@/components/common/FileIcon.vue";

const props = defineProps<{ files: any[]; loading: boolean }>();
const emit = defineEmits(["delete", "click"]);

const columns = [
  {
    title: "名称", key: "fileName",
    render(row: any) {
      return h("div", { style: "display:flex;align-items:center;gap:8px;cursor:pointer", onClick: () => emit("click", row) }, [
        h(FileIcon, { fileType: row.fileType, isDir: row.isDir }),
        row.fileName,
      ]);
    },
  },
  { title: "大小", key: "fileSize", render(row: any) { return row.isDir ? "-" : formatSize(row.fileSize); } },
  { title: "类型", key: "fileType", render(row: any) { return row.isDir ? "文件夹" : (row.fileType || "-"); } },
  { title: "修改时间", key: "createdAt", render(row: any) { return row.createdAt?.substring(0, 10) || "-"; } },
  {
    title: "操作", key: "actions",
    render(row: any) {
      return h("div", { style: "display:flex;gap:8px" }, [
        h(NButton, { size: "small", onClick: () => emit("delete", row.id) }, { default: () => "删除" }),
      ]);
    },
  },
];

function formatSize(bytes: number): string {
  if (!bytes) return "-";
  if (bytes < 1024) return bytes + " B";
  if (bytes < 1048576) return (bytes / 1024).toFixed(1) + " KB";
  if (bytes < 1073741824) return (bytes / 1048576).toFixed(1) + " MB";
  return (bytes / 1073741824).toFixed(2) + " GB";
}
</script>
