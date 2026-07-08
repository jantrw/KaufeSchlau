import { mount } from "@vue/test-utils";
import { beforeEach, describe, expect, it, vi } from "vitest";
import HomeView from "../views/HomeView.vue";
import { fetchProspects } from "../services/api";
import type { ProspectLink } from "../types";

vi.mock("../services/api", async (importOriginal) => ({
  ...(await importOriginal<typeof import("../services/api")>()),
  fetchProspects: vi.fn(),
}));

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

function prospect(fields: Partial<ProspectLink> & Pick<ProspectLink, "id" | "name" | "prospectUrl">): ProspectLink {
  return {
    requiresLocationContext: false,
    requiresStoreSelection: false,
    ...fields,
  };
}

describe("HomeView", () => {
  beforeEach(() => {
    vi.mocked(fetchProspects).mockReset();
  });

  it("fordert Standort nur bei standortpflichtiger Auswahl", async () => {
    const wrapper = mount(HomeView);

    expect(wrapper.text()).toContain("Die aktuelle Auswahl braucht Standortkontext.");

    await inputByLabel(wrapper, "Lidl").setValue(true);

    expect(wrapper.text()).toContain("Diese Händler können ohne PLZ oder Region geladen werden.");
  });

  it("sendet keine ungültige optionale PLZ", async () => {
    const wrapper = mount(HomeView);
    await inputByLabel(wrapper, "Lidl").setValue(true);
    await inputByLabel(wrapper, "PLZ").setValue("123");
    await wrapper.get("button").trigger("click");

    expect(fetchProspects).not.toHaveBeenCalled();
    expect(wrapper.text()).toContain("Bitte fünfstellige PLZ eingeben oder Feld leeren.");
  });

  it("zeigt geladene Prospekte mit Händlerhinweis an", async () => {
    vi.mocked(fetchProspects).mockResolvedValue([
      prospect({
        id: "rewe",
        name: "REWE",
        regionType: "PLZ_BASIERT",
        urlMode: "LOCATION_RESOLVED",
        prospectUrl: "https://www.rewe.de/angebote/nationale-angebote/",
        marketSearchUrl: "https://www.rewe.de/marktsuche/",
        requiresLocationContext: true,
        requiresStoreSelection: true,
      }),
    ]);

    const wrapper = mount(HomeView);
    await inputByLabel(wrapper, "PLZ").setValue("65185");
    await wrapper.get("button").trigger("click");

    expect(fetchProspects).toHaveBeenCalledWith({ plz: "65185", region: "", retailerIds: [] });
    expect(wrapper.text()).toContain("REWE");
    expect(wrapper.text()).toContain("PLZ-basiert");
    expect(wrapper.text()).not.toContain("PLZ_BASIERT");
    expect(wrapper.text()).toContain("Filiale oder PLZ beim Händler wählen.");
    expect(wrapper.text()).toContain("Zur Marktsuche");
    expect(wrapper.html()).toContain("https://www.rewe.de/angebote/nationale-angebote/");
    expect(wrapper.html()).toContain("https://www.rewe.de/marktsuche/");
  });

  it("zeigt EDEKA-Angebote und Marktsuche an", async () => {
    vi.mocked(fetchProspects).mockResolvedValue([
      prospect({
        id: "edeka",
        name: "EDEKA",
        regionType: "PLZ_BASIERT",
        urlMode: "LOCATION_RESOLVED",
        prospectUrl: "https://www.edeka.de/angebote/",
        marketSearchUrl: "https://www.edeka.de/marktsuche.jsp",
        requiresLocationContext: true,
        requiresStoreSelection: true,
      }),
    ]);

    const wrapper = mount(HomeView);
    await inputByLabel(wrapper, "PLZ").setValue("65185");
    await wrapper.get("button").trigger("click");

    expect(wrapper.text()).toContain("EDEKA");
    expect(wrapper.text()).toContain("Zum Prospekt");
    expect(wrapper.text()).toContain("Zur Marktsuche");
    expect(wrapper.html()).toContain("https://www.edeka.de/angebote/");
    expect(wrapper.html()).toContain("https://www.edeka.de/marktsuche.jsp");
  });

  it("zeigt minimale Prospect-Objekte ohne erfundene Region an", async () => {
    vi.mocked(fetchProspects).mockResolvedValue([
      prospect({
        id: "lidl",
        name: "Lidl",
        prospectUrl: "https://www.lidl.de/c/online-prospekte/s10005610",
        notice: "Offizieller Prospektlink",
      }),
    ]);

    const wrapper = mount(HomeView);
    await inputByLabel(wrapper, "Lidl").setValue(true);
    await wrapper.get("button").trigger("click");

    expect(wrapper.text()).toContain("Lidl");
    expect(wrapper.text()).toContain("Offizieller Prospektlink");
    expect(wrapper.text()).not.toContain("Offizieller Prospektlink verfügbar.");
    expect(wrapper.text()).not.toContain("Region unbekannt");
    expect(wrapper.html()).toContain("https://www.lidl.de/c/online-prospekte/s10005610");
  });

  it("zeigt bei minimalen Prospect-Objekten keinen erfundenen Hinweis an", async () => {
    vi.mocked(fetchProspects).mockResolvedValue([
      prospect({
        id: "lidl",
        name: "Lidl",
        prospectUrl: "https://www.lidl.de/c/online-prospekte/s10005610",
      }),
    ]);

    const wrapper = mount(HomeView);
    await inputByLabel(wrapper, "Lidl").setValue(true);
    await wrapper.get("button").trigger("click");

    expect(wrapper.text()).toContain("Lidl");
    expect(wrapper.text()).not.toContain("Offizieller Prospektlink verfügbar.");
    expect(wrapper.html()).toContain("https://www.lidl.de/c/online-prospekte/s10005610");
  });

  it("zeigt Fallback-Hinweis mit verständlicher Aldi-Region an", async () => {
    vi.mocked(fetchProspects).mockResolvedValue([
      prospect({
        id: "aldi-sued",
        name: "Aldi Süd",
        regionType: "ALDI_REGION",
        resolvedRegion: "SUED",
        urlMode: "STATIC_ENTRYPOINT",
        prospectUrl: "https://www.aldi-sued.de/prospekte",
        notice: "Offizieller Einstiegspunkt angezeigt; konkrete Wochenauflösung folgt später.",
      }),
    ]);

    const wrapper = mount(HomeView);
    await inputByLabel(wrapper, "PLZ").setValue("65185");
    await wrapper.get("button").trigger("click");

    expect(wrapper.text()).toContain("Aldi Süd");
    expect(wrapper.text()).not.toContain("SUED");
    expect(wrapper.text()).toContain("Offizieller Einstiegspunkt angezeigt; konkrete Wochenauflösung folgt später.");
  });

  it("zeigt Filialpflicht und Fallback-Hinweis zusammen an", async () => {
    vi.mocked(fetchProspects).mockResolvedValue([
      prospect({
        id: "rewe",
        name: "REWE",
        regionType: "PLZ_BASIERT",
        urlMode: "STATIC_ENTRYPOINT",
        prospectUrl: "https://www.rewe.de/angebote/nationale-angebote/",
        requiresLocationContext: true,
        requiresStoreSelection: true,
        notice: "Offizieller Einstiegspunkt angezeigt; konkrete Wochenauflösung folgt später.",
      }),
    ]);

    const wrapper = mount(HomeView);
    await inputByLabel(wrapper, "PLZ").setValue("65185");
    await wrapper.get("button").trigger("click");

    expect(wrapper.text()).toContain("Filiale oder PLZ beim Händler wählen.");
    expect(wrapper.text()).toContain("Offizieller Einstiegspunkt angezeigt; konkrete Wochenauflösung folgt später.");
  });


  it("zeigt leere Backend-Erfolge als keine Ergebnisse an", async () => {
    vi.mocked(fetchProspects).mockResolvedValue([]);

    const wrapper = mount(HomeView);
    await inputByLabel(wrapper, "PLZ").setValue("65185");
    await wrapper.get("button").trigger("click");

    expect(wrapper.text()).toContain("Keine Prospekte für diese Auswahl gefunden.");
    expect(wrapper.text()).not.toContain("Noch keine Prospekte geladen.");
  });

  it("ignoriert ältere Antworten bei parallelen Requests", async () => {
    const slowRequest = deferred<ProspectLink[]>();
    const fastRequest = deferred<ProspectLink[]>();
    vi.mocked(fetchProspects).mockReturnValueOnce(slowRequest.promise).mockReturnValueOnce(fastRequest.promise);

    const wrapper = mount(HomeView);
    await inputByLabel(wrapper, "PLZ").setValue("65185");
    await wrapper.get("button").trigger("click");
    await inputByLabel(wrapper, "PLZ").setValue("10115");
    await wrapper.get("button").trigger("click");

    fastRequest.resolve([
      prospect({
        id: "lidl",
        name: "Lidl",
        regionType: "OPTIONAL_FILIALE",
        urlMode: "STATIC_ENTRYPOINT",
        prospectUrl: "https://www.lidl.de/c/prospekt/",
      }),
    ]);
    await wrapper.vm.$nextTick();

    slowRequest.resolve([
      prospect({
        id: "rewe",
        name: "REWE",
        regionType: "PLZ_BASIERT",
        urlMode: "LOCATION_RESOLVED",
        prospectUrl: "https://www.rewe.de/angebote/nationale-angebote/",
        requiresLocationContext: true,
        requiresStoreSelection: true,
      }),
    ]);
    await wrapper.vm.$nextTick();

    expect(wrapper.text()).toContain("Lidl");
    expect(wrapper.html()).toContain("https://www.lidl.de/c/prospekt/");
    expect(wrapper.html()).not.toContain("https://www.rewe.de/angebote/nationale-angebote/");
  });

  it("zeigt Backend-Fehler an und entfernt alte Ergebnisse", async () => {
    vi.mocked(fetchProspects)
      .mockResolvedValueOnce([
        prospect({
          id: "kaufland",
          name: "Kaufland",
          regionType: "OPTIONAL_FILIALE",
          urlMode: "STATIC_ENTRYPOINT",
          prospectUrl: "https://filiale.kaufland.de/prospekte.html",
        }),
      ])
      .mockRejectedValueOnce({ message: "Backend-Fehler" });

    const wrapper = mount(HomeView);
    await inputByLabel(wrapper, "PLZ").setValue("65185");
    await wrapper.get("button").trigger("click");
    expect(wrapper.text()).toContain("Kaufland");
    expect(wrapper.html()).toContain("https://filiale.kaufland.de/prospekte.html");

    await wrapper.get("button").trigger("click");

    expect(wrapper.text()).toContain("Backend-Fehler");
    expect(wrapper.html()).not.toContain("https://filiale.kaufland.de/prospekte.html");
  });
});
