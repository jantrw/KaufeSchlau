<script setup lang="ts">
import Button from "primevue/button";
import Card from "primevue/card";
import Tag from "primevue/tag";
import type { ProspectLink } from "../types";

defineProps<{
  prospect: ProspectLink;
}>();

const regionTypeLabels: Record<string, string> = {
  ALDI_REGION: "Aldi-Region",
  BUNDESWEIT: "Bundesweit",
  OPTIONAL_FILIALE: "Filiale optional",
  PLZ_BASIERT: "PLZ-basiert",
};

const resolvedRegionLabels: Record<string, string> = {
  NORD: "Aldi Nord",
  SUED: "Aldi Süd",
};

function regionLabel(prospect: ProspectLink): string | undefined {
  if (prospect.resolvedRegion) {
    return resolvedRegionLabels[prospect.resolvedRegion] ?? `Region: ${prospect.resolvedRegion}`;
  }
  if (!prospect.regionType) {
    return undefined;
  }
  return regionTypeLabels[prospect.regionType];
}
</script>

<template>
  <Card class="prospect-card">
    <template #title>{{ prospect.name }}</template>
    <template #subtitle>
      <Tag v-if="regionLabel(prospect)" :value="regionLabel(prospect)" severity="info" />
    </template>
    <template #content>
      <p v-if="prospect.notice" class="hint">{{ prospect.notice }}</p>
      <p v-else-if="prospect.requiresStoreSelection" class="hint">
        Filiale oder PLZ beim Händler wählen. KaufeSchlau nutzt aktuell den offiziellen Einstiegspunkt.
      </p>
      <p v-if="prospect.fallbackUsed" class="hint">
        Offizieller Einstiegspunkt angezeigt; konkrete Wochenauflösung folgt später.
      </p>
      <p v-if="!prospect.notice && !prospect.requiresStoreSelection && !prospect.fallbackUsed" class="hint">
        Offizieller Prospektlink verfügbar.
      </p>
    </template>
    <template #footer>
      <Button as="a" :href="prospect.prospectUrl" target="_blank" rel="noopener" label="Zum Prospekt" />
    </template>
  </Card>
</template>
