<script setup lang="ts">
import Button from "primevue/button";
import Message from "primevue/message";
import { computed, ref } from "vue";
import DiscounterList from "../components/DiscounterList.vue";
import RegionInput from "../components/RegionInput.vue";
import RetailerFilter from "../components/RetailerFilter.vue";
import { fetchProspects, toUserMessage } from "../services/api";
import type { ProspectLink, Retailer } from "../types";

const retailers: Retailer[] = [
  { id: "aldi-nord", name: "Aldi Nord", requiresLocationContext: false, requiresStoreSelection: false },
  { id: "aldi-sued", name: "Aldi Süd", requiresLocationContext: false, requiresStoreSelection: false },
  { id: "lidl", name: "Lidl", requiresLocationContext: false, requiresStoreSelection: false },
  { id: "penny", name: "Penny", requiresLocationContext: false, requiresStoreSelection: false },
  { id: "netto-marken-discount", name: "Netto Marken-Discount", requiresLocationContext: true, requiresStoreSelection: true },
  { id: "kaufland", name: "Kaufland", requiresLocationContext: false, requiresStoreSelection: false },
  { id: "rewe", name: "REWE", requiresLocationContext: true, requiresStoreSelection: true },
  { id: "edeka", name: "EDEKA", requiresLocationContext: true, requiresStoreSelection: true },
];

const selectedIds = ref<string[]>([]);
const plz = ref("");
const region = ref("");
const prospects = ref<ProspectLink[]>([]);
const error = ref("");
const loading = ref(false);
const hasLoaded = ref(false);
let latestRequestId = 0;

const selectedRetailers = computed(() =>
  selectedIds.value.length ? retailers.filter((retailer) => selectedIds.value.includes(retailer.id)) : retailers,
);

const locationRequired = computed(() =>
  selectedIds.value.length === 0 || selectedRetailers.value.some((retailer) => retailer.requiresLocationContext),
);

const hasValidPlz = computed(() => /^\d{5}$/.test(plz.value));
const hasLocation = computed(() => hasValidPlz.value || region.value.trim().length > 0);
const locationError = computed(() => {
  if (plz.value.length > 0 && !hasValidPlz.value) return "Bitte fünfstellige PLZ eingeben oder Feld leeren.";
  return locationRequired.value && !hasLocation.value ? "Bitte PLZ oder Region eingeben." : "";
});

async function loadProspects() {
  const requestId = ++latestRequestId;
  error.value = "";
  if (locationError.value) {
    error.value = locationError.value;
    prospects.value = [];
    loading.value = false;
    return;
  }

  loading.value = true;
  try {
    const loadedProspects = await fetchProspects({
      plz: plz.value,
      region: region.value.trim(),
      retailerIds: selectedIds.value,
    });
    if (requestId !== latestRequestId) return;
    prospects.value = loadedProspects;
    hasLoaded.value = true;
  } catch (caughtError) {
    if (requestId !== latestRequestId) return;
    prospects.value = [];
    error.value = toUserMessage(caughtError);
  } finally {
    if (requestId !== latestRequestId) return;
    loading.value = false;
  }
}
</script>

<template>
  <main class="app-shell">
    <header>
      <p>KaufeSchlau</p>
      <h1>Prospekte finden</h1>
    </header>

    <RetailerFilter v-model:selected-ids="selectedIds" :retailers="retailers" />
    <RegionInput v-model:plz="plz" v-model:region="region" :required="locationRequired" />

    <Message v-if="locationRequired" severity="warn">
      Die aktuelle Auswahl braucht Standortkontext.
    </Message>
    <Message v-else severity="success">
      Diese Händler können ohne PLZ oder Region geladen werden.
    </Message>

    <Button label="Prospekte laden" :loading="loading" @click="loadProspects" />
    <DiscounterList :prospects="prospects" :loading="loading" :error="error" :has-loaded="hasLoaded" />
  </main>
</template>
