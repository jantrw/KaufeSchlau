<script setup lang="ts">
import Checkbox from "primevue/checkbox";
import type { Retailer } from "../types";

defineProps<{
  retailers: Retailer[];
  selectedIds: string[];
}>();

const emit = defineEmits<{
  "update:selectedIds": [value: string[]];
}>();

function toggle(id: string, checked: boolean, selectedIds: string[]) {
  emit("update:selectedIds", checked ? [...selectedIds, id] : selectedIds.filter((selected) => selected !== id));
}
</script>

<template>
  <section class="panel">
    <div class="section-title">
      <h2>Händler</h2>
      <span>Keine Auswahl = alle Händler</span>
    </div>

    <div class="retailer-grid">
      <label v-for="retailer in retailers" :key="retailer.id" class="retailer-option">
        <Checkbox
          :model-value="selectedIds.includes(retailer.id)"
          binary
          @update:model-value="toggle(retailer.id, Boolean($event), selectedIds)"
        />
        <span>
          {{ retailer.name }}
          <small v-if="retailer.requiresLocationContext">Standort nötig</small>
          <small v-else>ohne Standort</small>
        </span>
      </label>
    </div>
  </section>
</template>
