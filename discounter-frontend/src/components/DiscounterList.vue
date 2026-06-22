<script setup lang="ts">
import Message from "primevue/message";
import ProgressSpinner from "primevue/progressspinner";
import type { ProspectLink } from "../types";
import DiscounterCard from "./DiscounterCard.vue";

defineProps<{
  prospects: ProspectLink[];
  loading: boolean;
  error: string;
  hasLoaded: boolean;
}>();
</script>

<template>
  <section class="results">
    <ProgressSpinner v-if="loading" aria-label="Prospekte werden geladen" />
    <Message v-else-if="error" severity="error">{{ error }}</Message>
    <div v-else-if="prospects.length > 0" class="prospect-grid">
      <DiscounterCard v-for="prospect in prospects" :key="prospect.id" :prospect="prospect" />
    </div>
    <Message v-else-if="hasLoaded" severity="secondary">
      Keine Prospekte für diese Auswahl gefunden.
    </Message>
    <Message v-else severity="secondary">
      Noch keine Prospekte geladen.
    </Message>
  </section>
</template>
