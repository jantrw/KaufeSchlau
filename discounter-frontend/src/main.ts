import Aura from "@primeuix/themes/aura";
import PrimeVue from "primevue/config";
import { createApp } from "vue";
import App from "./App.vue";
import "./style.css";

createApp(App).use(PrimeVue, { theme: { preset: Aura } }).mount("#app");
