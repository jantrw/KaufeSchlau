import { mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import HomeView from "../views/HomeView.vue";
import { fetchProspects } from "../services/api";
import type { ProspectLink } from "../types";

vi.mock("../services/api", async (importOriginal) => ({
  ...(await importOriginal<typeof import("../services/api")>()),
  fetchProspects: vi.fn(),
}));

const global = {
  stubs: {
    Button: {
      props: ["label", "loading"],
      emits: ["click"],
      template: '<button @click="$emit(\'click\')">{{ label }}</button>',
    },
    Checkbox: {
      props: ["modelValue"],
      emits: ["update:modelValue"],
      template: '<input type="checkbox" :checked="modelValue" @change="$emit(\'update:modelValue\', $event.target.checked)" />',
    },
    InputText: {
      props: ["modelValue"],
      emits: ["update:modelValue"],
      template: '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
    },
    Message: { template: "<div><slot /></div>" },
    ProgressSpinner: { template: "<div>Lädt</div>" },
    Card: { template: "<article><slot name='title' /><slot name='subtitle' /><slot name='content' /><slot name='footer' /></article>" },
    Tag: { props: ["value"], template: "<span>{{ value }}</span>" },
  },
};

function inputByLabel(wrapper: ReturnType<typeof mount>, labelText: string) {
  const label = wrapper.findAll("label").find((candidate) => candidate.text().includes(labelText));
  expect(label).toBeTruthy();
  return label!.get("input");
}

function deferred<T>() {
  let resolve!: (value: T) => void;
  const promise = new Promise<T>((promiseResolve) => {
    resolve = promiseResolve;
  });
  return { promise, resolve };
}

describe("HomeView", () => {
  beforeEach(() => {
    vi.mocked(fetchProspects).mockReset();
  });

  it("fordert Standort nur bei standortpflichtiger Auswahl", async () => {
    const wrapper = mount(HomeView, { global });

    expect(wrapper.text()).toContain("Die aktuelle Auswahl braucht Standortkontext.");

    await inputByLabel(wrapper, "Lidl").setValue(true);

    expect(wrapper.text()).toContain("Diese Händler können ohne PLZ oder Region geladen werden.");
  });

  it("zeigt geladene Prospekte mit Händlerhinweis an", async () => {
    vi.mocked(fetchProspects).mockResolvedValue([
      {
        id: "rewe",
        name: "REWE",
        regionType: "PLZ_BASIERT",
        urlMode: "LOCATION_RESOLVED",
        prospectUrl: "https://www.rewe.de/angebote/nationale-angebote/",
        requiresLocationContext: true,
        requiresStoreSelection: true,
      },
    ]);

    const wrapper = mount(HomeView, { global });
    await inputByLabel(wrapper, "PLZ").setValue("65185");
    await wrapper.get("button").trigger("click");

    expect(fetchProspects).toHaveBeenCalledWith({ plz: "65185", region: "", retailerIds: [] });
    expect(wrapper.text()).toContain("REWE");
    expect(wrapper.text()).toContain("PLZ-basiert");
    expect(wrapper.text()).not.toContain("PLZ_BASIERT");
    expect(wrapper.text()).toContain("Filiale oder PLZ beim Händler wählen.");
    expect(wrapper.html()).toContain("https://www.rewe.de/angebote/nationale-angebote/");
  });

  it("zeigt Fallback-Hinweis mit verständlicher Aldi-Region an", async () => {
    vi.mocked(fetchProspects).mockResolvedValue([
      {
        id: "aldi-sued",
        name: "Aldi Süd",
        regionType: "ALDI_REGION",
        resolvedRegion: "SUED",
        urlMode: "STATIC_ENTRYPOINT",
        prospectUrl: "https://www.aldi-sued.de/prospekte",
        requiresLocationContext: false,
        requiresStoreSelection: false,
        fallbackUsed: true,
      },
    ]);

    const wrapper = mount(HomeView, { global });
    await inputByLabel(wrapper, "PLZ").setValue("65185");
    await wrapper.get("button").trigger("click");

    expect(wrapper.text()).toContain("Aldi Süd");
    expect(wrapper.text()).not.toContain("SUED");
    expect(wrapper.text()).toContain("Offizieller Einstiegspunkt angezeigt; konkrete Wochenauflösung folgt später.");
  });

  it("zeigt Filialpflicht und Fallback-Hinweis zusammen an", async () => {
    vi.mocked(fetchProspects).mockResolvedValue([
      {
        id: "rewe",
        name: "REWE",
        regionType: "PLZ_BASIERT",
        urlMode: "STATIC_ENTRYPOINT",
        prospectUrl: "https://www.rewe.de/angebote/nationale-angebote/",
        requiresLocationContext: true,
        requiresStoreSelection: true,
        fallbackUsed: true,
      },
    ]);

    const wrapper = mount(HomeView, { global });
    await inputByLabel(wrapper, "PLZ").setValue("65185");
    await wrapper.get("button").trigger("click");

    expect(wrapper.text()).toContain("Filiale oder PLZ beim Händler wählen.");
    expect(wrapper.text()).toContain("Offizieller Einstiegspunkt angezeigt; konkrete Wochenauflösung folgt später.");
  });


  it("zeigt leere Backend-Erfolge als keine Ergebnisse an", async () => {
    vi.mocked(fetchProspects).mockResolvedValue([]);

    const wrapper = mount(HomeView, { global });
    await inputByLabel(wrapper, "PLZ").setValue("65185");
    await wrapper.get("button").trigger("click");

    expect(wrapper.text()).toContain("Keine Prospekte für diese Auswahl gefunden.");
    expect(wrapper.text()).not.toContain("Noch keine Prospekte geladen.");
  });

  it("ignoriert ältere Antworten bei parallelen Requests", async () => {
    const slowRequest = deferred<ProspectLink[]>();
    const fastRequest = deferred<ProspectLink[]>();
    vi.mocked(fetchProspects).mockReturnValueOnce(slowRequest.promise).mockReturnValueOnce(fastRequest.promise);

    const wrapper = mount(HomeView, { global });
    await inputByLabel(wrapper, "PLZ").setValue("65185");
    await wrapper.get("button").trigger("click");
    await inputByLabel(wrapper, "PLZ").setValue("10115");
    await wrapper.get("button").trigger("click");

    fastRequest.resolve([
      {
        id: "lidl",
        name: "Lidl",
        regionType: "OPTIONAL_FILIALE",
        urlMode: "STATIC_ENTRYPOINT",
        prospectUrl: "https://www.lidl.de/c/prospekt/",
        requiresLocationContext: false,
        requiresStoreSelection: false,
      },
    ]);
    await wrapper.vm.$nextTick();

    slowRequest.resolve([
      {
        id: "rewe",
        name: "REWE",
        regionType: "PLZ_BASIERT",
        urlMode: "LOCATION_RESOLVED",
        prospectUrl: "https://www.rewe.de/angebote/nationale-angebote/",
        requiresLocationContext: true,
        requiresStoreSelection: true,
      },
    ]);
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain("Lidl");
    expect(wrapper.html()).toContain("https://www.lidl.de/c/prospekt/");
    expect(wrapper.html()).not.toContain("https://www.rewe.de/angebote/nationale-angebote/");
  });

  it("zeigt Backend-Fehler an und entfernt alte Ergebnisse", async () => {
    vi.mocked(fetchProspects)
      .mockResolvedValueOnce([
        {
          id: "kaufland",
          name: "Kaufland",
          regionType: "OPTIONAL_FILIALE",
          urlMode: "STATIC_ENTRYPOINT",
          prospectUrl: "https://filiale.kaufland.de/prospekte.html",
          requiresLocationContext: false,
          requiresStoreSelection: false,
        },
      ])
      .mockRejectedValueOnce({ response: { data: { message: "Backend-Fehler" } } });

    const wrapper = mount(HomeView, { global });
    await inputByLabel(wrapper, "PLZ").setValue("65185");
    await wrapper.get("button").trigger("click");
    expect(wrapper.text()).toContain("Kaufland");
    expect(wrapper.html()).toContain("https://filiale.kaufland.de/prospekte.html");

    await wrapper.get("button").trigger("click");

    expect(wrapper.text()).toContain("Backend-Fehler");
    expect(wrapper.html()).not.toContain("https://filiale.kaufland.de/prospekte.html");
  });
});
