<template>
  <div class="photo-timeline">
    <div class="toolbar">
      <n-radio-group v-model:value="mediaType" @update:value="onFilter">
        <n-radio-button value="all">全部</n-radio-button>
        <n-radio-button value="IMAGE">照片</n-radio-button>
        <n-radio-button value="VIDEO">视频</n-radio-button>
      </n-radio-group>
    </div>
    <n-spin :show="loading">
      <PhotoGrid v-if="!loading" :groups="groups" @preview="openPreview" />
    </n-spin>
    <PhotoViewer ref="viewerRef" :images="previewImages" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { fileApi } from "@/api/file";
import PhotoGrid from "@/components/desktop/PhotoGrid.vue";
import PhotoViewer from "@/components/desktop/PhotoViewer.vue";

const loading = ref(true);
const mediaType = ref("all");
const groups = ref<any[]>([]);
const previewImages = ref<any[]>([]);
const viewerRef = ref();

onMounted(() => fetchPhotos());

async function fetchPhotos(type?: string) {
  loading.value = true;
  try {
    const res = await fileApi.list({ size: 500, type: type || undefined });
    const items = res.data.data.list.filter(
      (f: any) => !f.isDir && (f.fileType === "IMAGE" || f.fileType === "VIDEO")
    );
    const grouped: Record<string, any[]> = {};
    for (const item of items) {
      const month = item.createdAt.substring(0, 7);
      if (!grouped[month]) grouped[month] = [];
      grouped[month].push(item);
    }
    groups.value = Object.entries(grouped)
      .sort(([a], [b]) => b.localeCompare(a))
      .map(([month, photos]) => ({ dateLabel: month, photos }));
  } finally {
    loading.value = false;
  }
}

function onFilter(value: string) {
  fetchPhotos(value === "all" ? undefined : value);
}

function openPreview(images: any[]) {
  previewImages.value = images;
  viewerRef.value?.open();
}
</script>

<style scoped>
.photo-timeline { padding: 16px; }
.toolbar { margin-bottom: 16px; }
</style>
