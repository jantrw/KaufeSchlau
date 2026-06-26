<script setup lang="ts">
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

function hasGenericOfficialHint(prospect: ProspectLink): boolean {
  return Boolean(
    !prospect.notice &&
      !prospect.requiresStoreSelection &&
      !prospect.fallbackUsed &&
      (prospect.regionType || prospect.urlMode),
  );
}
</script>

<template>
  <article class="prospect-card">
    <header class="prospect-card-header">
      <h2>{{ prospect.name }}</h2>
      <span v-if="regionLabel(prospect)" class="tag">{{ regionLabel(prospect) }}</span>
    </header>
    <div class="prospect-card-content">
      <p v-if="prospect.notice" class="hint">{{ prospect.notice }}</p>
      <p v-else-if="prospect.requiresStoreSelection" class="hint">
        Filiale oder PLZ beim Händler wählen. KaufeSchlau nutzt aktuell den offiziellen Einstiegspunkt.
      </p>
      <p v-if="prospect.fallbackUsed" class="hint">
        Offizieller Einstiegspunkt angezeigt; konkrete Wochenauflösung folgt später.
      </p>
      <p v-if="hasGenericOfficialHint(prospect)" class="hint">
        Offizieller Prospektlink verfügbar.
      </p>
    </div>
    <div class="prospect-card-actions">
      <a class="button-link" :href="prospect.prospectUrl" target="_blank" rel="noopener">Zum Prospekt</a>
      <a v-if="prospect.marketSearchUrl" class="button-link secondary" :href="prospect.marketSearchUrl" target="_blank" rel="noopener">
        Zur Marktsuche
      </a>
    </div>
  </article>
</template>
