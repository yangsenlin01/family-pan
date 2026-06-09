import { createApp } from "vue";
import { createPinia } from "pinia";
import App from "./App.vue";
import router from "./router";
import { setupGuards } from "./router/guards";
import "@/styles/global.css";

const app = createApp(App);
const pinia = createPinia();

app.use(pinia);
app.use(router);
setupGuards(router);
app.mount("#app");
