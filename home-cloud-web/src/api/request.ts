import axios from "axios";
import { useUserStore } from "@/stores/user";
import router from "@/router";

const request = axios.create({
  baseURL: "/api/v1",
  timeout: 30000,
});

let isRefreshing = false;
let pendingQueue: Array<{
  resolve: (token: string) => void;
  reject: (err: Error) => void;
}> = [];

function processQueue(error: Error | null, token: string | null) {
  pendingQueue.forEach((p) => {
    if (error) p.reject(error);
    else p.resolve(token!);
  });
  pendingQueue = [];
}

request.interceptors.request.use((config) => {
  const userStore = useUserStore();
  if (userStore.accessToken) {
    config.headers.Authorization = userStore.accessToken;
  }
  return config;
});

request.interceptors.response.use(
  (response) => response,
  async (error) => {
    const { config, response } = error;
    if (response?.status === 401 && !config._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          pendingQueue.push({
            resolve: (token: string) => {
              config.headers.Authorization = token;
              resolve(axios(config));
            },
            reject,
          });
        });
      }
      config._retry = true;
      isRefreshing = true;
      const userStore = useUserStore();
      try {
        const res = await axios.post("/api/v1/auth/refresh", null, {
          headers: { Authorization: userStore.refreshToken },
        });
        const newToken = res.data.data.accessToken;
        userStore.setToken(newToken, res.data.data.refreshToken);
        processQueue(null, newToken);
        config.headers.Authorization = newToken;
        return axios(config);
      } catch (refreshError) {
        processQueue(refreshError as Error, null);
        userStore.logout();
        router.push("/login");
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }
    return Promise.reject(error);
  }
);

export default request;
