import request from "./request";

export const fileApi = {
  upload: (formData: FormData) =>
    request.post("/files/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    }),
  list: (params: { parentId?: number; page?: number; size?: number; type?: string }) =>
    request.get("/files/list", { params }),
  detail: (id: number) => request.get(`/files/detail/${id}`),
  delete: (id: number) => request.delete(`/files/${id}`),
  rename: (id: number, fileName: string) =>
    request.put(`/files/${id}/rename`, { fileName }),
  createFolder: (parentId: number, folderName: string) =>
    request.post("/files/folder", { parentId, folderName }),
  getThumbnailUrl: (id: number, size = 200) =>
    `/api/v1/files/thumbnail/${id}?size=${size}`,
  getStreamUrl: (id: number) => `/api/v1/files/stream/${id}`,
};
