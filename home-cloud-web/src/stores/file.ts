import { defineStore } from "pinia";
import { fileApi } from "@/api/file";

interface FileItem {
  id: number;
  fileName: string;
  fileType: string | null;
  fileSize: number;
  isDir: number;
  createdAt: string;
  thumbnail200: string | null;
  mimeType: string | null;
}

export const useFileStore = defineStore("file", {
  state: () => ({
    files: [] as FileItem[],
    total: 0,
    currentParentId: 0,
    loading: false,
  }),
  actions: {
    async fetchFiles(parentId = 0, page = 1, size = 30, type?: string) {
      this.loading = true;
      try {
        const res = await fileApi.list({ parentId, page, size, type });
        this.files = res.data.data.list;
        this.total = res.data.data.total;
        this.currentParentId = parentId;
      } finally {
        this.loading = false;
      }
    },
    async deleteFile(id: number) {
      await fileApi.delete(id);
      await this.fetchFiles(this.currentParentId);
    },
    async createFolder(parentId: number, name: string) {
      await fileApi.createFolder(parentId, name);
      await this.fetchFiles(this.currentParentId);
    },
  },
});
