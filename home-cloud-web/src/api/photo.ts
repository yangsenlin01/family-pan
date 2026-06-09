import request from "./request";

export const photoApi = {
  timeline: (params?: { year?: number; month?: number }) =>
    request.get("/photos/timeline", { params }),
  list: (params?: { page?: number; size?: number; type?: string }) =>
    request.get("/photos/list", { params }),
};
