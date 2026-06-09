import request from "./request";

export interface LoginParams {
  username: string;
  password: string;
}
export interface RegisterParams {
  username: string;
  password: string;
  nickname: string;
}

export const authApi = {
  login: (data: LoginParams) => request.post("/auth/login", data),
  register: (data: RegisterParams) => request.post("/auth/register", data),
  refresh: () => request.post("/auth/refresh"),
  logout: () => request.post("/auth/logout"),
  changePassword: (data: { oldPassword: string; newPassword: string }) =>
    request.put("/auth/password", data),
};
