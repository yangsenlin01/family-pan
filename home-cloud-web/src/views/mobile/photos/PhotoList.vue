<template>
  <div class="mobile-photos">
    <van-tabs v-model:active="mediaType" @change="onFilter">
      <van-tab title="全部" name="all" />
      <van-tab title="照片" name="IMAGE" />
      <van-tab title="视频" name="VIDEO" />
    </van-tabs>
    <van-pull-refresh v-model="refreshing" @refresh="onRefresh">
      <div class="photo-grid">
        <div v-for="photo in photos" :key="photo.id" class="photo-item" @click="preview(photo)">
          <img v-if="photo.fileType==='IMAGE'" :src="getThumb(photo.id)" />
          <video v-else :src="getStream(photo.id)" preload="metadata" />
        </div>
      </div>
    </van-pull-refresh>
    <van-uploader :after-read="onUpload" />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from "vue";
import { fileApi } from "@/api/file";
const mediaType = ref("all");
const refreshing = ref(false);
const photos = ref<any[]>([]);

onMounted(() => fetchPhotos());

async function fetchPhotos(type?: string) {
  const res = await fileApi.list({ size: 200, type });
  photos.value = res.data.data.list.filter((f: any) => !f.isDir && (f.fileType==="IMAGE"||f.fileType==="VIDEO"));
}
function onFilter(name: string | number) { fetchPhotos(name==="all"?undefined:name as string); }
function getThumb(id: number) { return fileApi.getThumbnailUrl(id, 200); }
function getStream(id: number) { return fileApi.getStreamUrl(id); }
function preview(_item: any) { /* will be enhanced later */ }
async function onUpload(file: any) {
  const formData = new FormData();
  formData.append("file", file.file);
  await fileApi.upload(formData);
  fetchPhotos();
}
async function onRefresh() {
  await fetchPhotos(mediaType.value==="all"?undefined:mediaType.value);
  refreshing.value = false;
}
</script>

<style scoped>
.photo-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 2px; padding: 2px; }
.photo-item { aspect-ratio: 1; overflow: hidden; background: #f0f0f0; }
.photo-item img, .photo-item video { width: 100%; height: 100%; object-fit: cover; }
</style>
