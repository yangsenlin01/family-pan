import { defineStore } from "pinia";

interface UserInfo {
  id: number;
  username: string;
  nickname: string;
  avatar: string | null;
}

export const useUserStore = defineStore("user", {
  state: () => {
    const savedInfo = localStorage.getItem("userInfo");
    return {
      accessToken: localStorage.getItem("accessToken") || "",
      refreshToken: localStorage.getItem("refreshToken") || "",
      userInfo: savedInfo ? JSON.parse(savedInfo) : (null as UserInfo | null),
    };
  },
  getters: {
    isLoggedIn: (state) => !!state.accessToken,
  },
  actions: {
    setToken(accessToken: string, refreshToken: string) {
      this.accessToken = accessToken;
      this.refreshToken = refreshToken;
      localStorage.setItem("accessToken", accessToken);
      localStorage.setItem("refreshToken", refreshToken);
    },
    setUserInfo(userInfo: UserInfo) {
      this.userInfo = userInfo;
      localStorage.setItem("userInfo", JSON.stringify(userInfo));
    },
    logout() {
      this.accessToken = "";
      this.refreshToken = "";
      this.userInfo = null;
      localStorage.removeItem("accessToken");
      localStorage.removeItem("refreshToken");
      localStorage.removeItem("userInfo");
    },
  },
});
