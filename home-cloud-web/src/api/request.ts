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

function notifyExpired(msg: string) {
  const win = window as any;
  if (win.$message) {
    win.$message.warning(msg, { duration: 3000 });
  }
}

function handleAuthExpired() {
  const userStore = useUserStore();
  userStore.logout();
  notifyExpired("登录已过期，请重新登录");
  router.push("/login");
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

    // Handle 401: try to refresh, otherwise redirect to login
    if (response?.status === 401 && !config._retry) {
      const userStore = useUserStore();

      // No refresh token available — go to login directly
      if (!userStore.refreshToken) {
        handleAuthExpired();
        return Promise.reject(error);
      }

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
        handleAuthExpired();
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    return Promise.reject(error);
  }
);

export default request;
