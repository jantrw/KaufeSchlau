<script setup lang="ts">
defineProps<{
  plz: string;
  region: string;
  required: boolean;
}>();

const emit = defineEmits<{
  "update:plz": [value: string];
  "update:region": [value: string];
}>();

function updatePlz(event: Event) {
  emit("update:plz", (event.target as HTMLInputElement).value.replace(/\D/g, "").slice(0, 5));
}

function updateRegion(event: Event) {
  emit("update:region", (event.target as HTMLInputElement).value);
}
</script>

<template>
  <section class="panel">
    <div class="section-title">
      <h2>Standort</h2>
      <span v-if="required" class="required">erforderlich</span>
      <span v-else class="optional">optional</span>
    </div>

    <div class="location-grid">
      <label>
        PLZ
        <input
          :value="plz"
          inputmode="numeric"
          maxlength="5"
          placeholder="65185"
          :aria-invalid="plz.length > 0 && !/^\d{5}$/.test(plz) ? 'true' : 'false'"
          @input="updatePlz"
        />
      </label>
      <label>
        Region
        <input :value="region" placeholder="hessen" @input="updateRegion" />
      </label>
    </div>

    <p v-if="required" class="hint">
      Diese Auswahl braucht PLZ oder Region, damit regionale Prospekte korrekt angezeigt werden.
    </p>
  </section>
</template>
