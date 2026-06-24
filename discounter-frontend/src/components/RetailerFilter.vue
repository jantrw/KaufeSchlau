<script setup lang="ts">
import type { Retailer } from "../types";

defineProps<{
  retailers: Retailer[];
  selectedIds: string[];
}>();

const emit = defineEmits<{
  "update:selectedIds": [value: string[]];
}>();

function toggle(id: string, event: Event, selectedIds: string[]) {
  const checked = (event.target as HTMLInputElement).checked;
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
        <input type="checkbox" :checked="selectedIds.includes(retailer.id)" @change="toggle(retailer.id, $event, selectedIds)" />
        <span>
          {{ retailer.name }}
          <small v-if="retailer.requiresLocationContext">Standort nötig</small>
          <small v-else>ohne Standort</small>
        </span>
      </label>
    </div>
  </section>
</template>
