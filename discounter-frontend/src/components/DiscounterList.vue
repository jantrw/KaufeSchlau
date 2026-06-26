<script setup lang="ts">
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
    <div v-if="loading" class="spinner" aria-label="Prospekte werden geladen" role="status" />
    <div v-else-if="error" class="message error">{{ error }}</div>
    <div v-else-if="prospects.length > 0" class="prospect-grid">
      <DiscounterCard v-for="prospect in prospects" :key="prospect.id" :prospect="prospect" />
    </div>
    <div v-else-if="hasLoaded" class="message secondary">
      Keine Prospekte für diese Auswahl gefunden.
    </div>
    <div v-else class="message secondary">
      Noch keine Prospekte geladen.
    </div>
  </section>
</template>
