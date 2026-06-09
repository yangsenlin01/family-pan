<template>
  <div v-for="group in groups" :key="group.dateLabel" class="group">
    <h3 class="group-label">{{ group.dateLabel }}</h3>
    <div class="grid">
      <div v-for="photo in group.photos" :key="photo.id" class="grid-item" @click="preview(photo, group.photos)">
        <img v-if="photo.fileType === 'IMAGE'" :src="getThumb(photo.id)" :alt="photo.fileName" loading="lazy" />
        <div v-else class="video-item">
          <video :src="getStream(photo.id)" preload="metadata" />
          <n-icon size="40" class="play-icon" color="white"><PlayCircleOutline /></n-icon>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { NIcon } from "naive-ui";
import { PlayCircleOutline } from "@vicons/ionicons5";
import { fileApi } from "@/api/file";

defineProps<{ groups: any[] }>();
const emit = defineEmits(["preview"]);

function getThumb(id: number) {
  return fileApi.getThumbnailUrl(id, 200);
}
function getStream(id: number) {
  return fileApi.getStreamUrl(id);
}
function preview(photo: any, allPhotos: any[]) {
  const index = allPhotos.findIndex((p: any) => p.id === photo.id);
  emit("preview", allPhotos, index);
}
</script>

<style scoped>
.group { margin-bottom: 24px; }
.group-label { font-size: 16px; margin-bottom: 12px; color: var(--n-text-color); }
.grid { display: grid; grid-template-columns: repeat(auto-fill, minmax(180px, 1fr)); gap: 8px; }
.grid-item { aspect-ratio: 1; overflow: hidden; border-radius: 8px; cursor: pointer; position: relative; background: #f0f0f0; }
.grid-item img { width: 100%; height: 100%; object-fit: cover; }
.video-item { width: 100%; height: 100%; position: relative; background: #000; }
.video-item video { width: 100%; height: 100%; object-fit: cover; }
.play-icon { position: absolute; top: 50%; left: 50%; transform: translate(-50%,-50%); }
</style>
