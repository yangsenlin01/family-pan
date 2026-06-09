import { createRouter, createWebHistory } from "vue-router";

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: "/login",
      name: "login",
      component: () => import("@/views/shared/LoginView.vue"),
    },
    {
      path: "/",
      redirect: "/desktop/photos",
    },
    {
      path: "/desktop",
      component: () => import("@/layouts/DesktopLayout.vue"),
      children: [
        {
          path: "photos",
          name: "desktop-photos",
          component: () => import("@/views/desktop/photos/PhotoTimeline.vue"),
        },
        {
          path: "files",
          name: "desktop-files",
          component: () => import("@/views/desktop/files/FileManager.vue"),
        },
        {
          path: "settings",
          name: "desktop-settings",
          component: () => import("@/views/desktop/settings/SettingsView.vue"),
        },
      ],
    },
    {
      path: "/mobile",
      component: () => import("@/layouts/MobileLayout.vue"),
      children: [
        {
          path: "photos",
          name: "mobile-photos",
          component: () => import("@/views/mobile/photos/PhotoList.vue"),
        },
        {
          path: "files",
          name: "mobile-files",
          component: () => import("@/views/mobile/files/FileList.vue"),
        },
        {
          path: "me",
          name: "mobile-me",
          component: () => import("@/views/mobile/me/MeView.vue"),
        },
      ],
    },
  ],
});

export default router;
