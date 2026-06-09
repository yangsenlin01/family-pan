<template>
  <div v-if="visible" class="viewer-overlay" @click.self="close">
    <div class="viewer-toolbar">
      <n-button @click="close">关闭</n-button>
      <span style="color:white">{{ currentIndex + 1 }} / {{ images.length }}</span>
    </div>
    <div class="viewer-content">
      <img v-if="isImage" :src="getStreamUrl(currentItem.id)" :alt="currentItem.fileName" />
      <video v-else :src="getStreamUrl(currentItem.id)" controls autoplay style="max-width:90vw;max-height:85vh" />
    </div>
    <n-button class="nav prev" @click="prev">&lt;</n-button>
    <n-button class="nav next" @click="next">&gt;</n-button>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from "vue";
import { fileApi } from "@/api/file";

const visible = ref(false);
const images = ref<any[]>([]);
const currentIndex = ref(0);
const currentItem = computed(() => images.value[currentIndex.value] || {});
const isImage = computed(() => currentItem.value.fileType === "IMAGE");

function getStreamUrl(id: number) { return fileApi.getStreamUrl(id); }
function open(newImages: any[], startIndex = 0) {
  images.value = newImages;
  currentIndex.value = startIndex;
  visible.value = true;
}
function close() { visible.value = false; }
function prev() { if (currentIndex.value > 0) currentIndex.value--; }
function next() { if (currentIndex.value < images.value.length - 1) currentIndex.value++; }

defineExpose({ open });
</script>

<style scoped>
.viewer-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.92); z-index: 1000; display: flex; flex-direction: column; align-items: center; justify-content: center; }
.viewer-toolbar { position: absolute; top: 0; left: 0; right: 0; padding: 12px; display: flex; justify-content: space-between; align-items: center; z-index: 10; }
.viewer-content { display: flex; align-items: center; justify-content: center; max-width: 90vw; max-height: 85vh; }
.viewer-content img { max-width: 90vw; max-height: 85vh; object-fit: contain; }
.nav { position: absolute; top: 50%; transform: translateY(-50%); opacity: 0.6; }
.nav:hover { opacity: 1; }
.prev { left: 16px; }
.next { right: 16px; }
</style>
