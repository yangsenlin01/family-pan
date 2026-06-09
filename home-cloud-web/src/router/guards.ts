import type { Router } from "vue-router";
import { useUserStore } from "@/stores/user";

export function setupGuards(router: Router) {
  router.beforeEach((to, _from, next) => {
    const userStore = useUserStore();
    if (to.path !== "/login" && !userStore.isLoggedIn) {
      next("/login");
      return;
    }
    if (to.path === "/login" && userStore.isLoggedIn) {
      next("/");
      return;
    }
    if (to.path === "/") {
      const isMobile = window.innerWidth < 768;
      next(isMobile ? "/mobile/photos" : "/desktop/photos");
      return;
    }
    next();
  });
}
