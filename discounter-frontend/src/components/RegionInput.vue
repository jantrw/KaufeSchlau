<script setup lang="ts">
import InputText from "primevue/inputtext";

defineProps<{
  plz: string;
  region: string;
  required: boolean;
}>();

const emit = defineEmits<{
  "update:plz": [value: string];
  "update:region": [value: string];
}>();
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
        <InputText
          :model-value="plz"
          inputmode="numeric"
          maxlength="5"
          placeholder="65185"
          :invalid="required && plz.length > 0 && !/^\\d{5}$/.test(plz)"
          @update:model-value="emit('update:plz', String($event).replace(/\\D/g, '').slice(0, 5))"
        />
      </label>
      <label>
        Region
        <InputText
          :model-value="region"
          placeholder="hessen"
          @update:model-value="emit('update:region', String($event))"
        />
      </label>
    </div>

    <p v-if="required" class="hint">
      Diese Auswahl braucht PLZ oder Region, damit regionale Prospekte korrekt angezeigt werden.
    </p>
  </section>
</template>
