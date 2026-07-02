import { afterEach, describe, expect, it, vi } from "vitest";
import { fetchProspects } from "../services/api";

describe("fetchProspects", () => {
  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it("liest den Backend-Response mit items und minimalem Prospect-Shape", async () => {
    const fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({
        items: [{
          id: "lidl",
          name: "Lidl",
          prospectUrl: "https://www.lidl.de/c/online-prospekte/s10005610",
          urlMode: "STATIC_ENTRYPOINT",
        }],
      }),
    });
    vi.stubGlobal("fetch", fetch);

    await expect(fetchProspects({ plz: "65185", region: "", retailerIds: [] })).resolves.toEqual([
      {
        id: "lidl",
        name: "Lidl",
        prospectUrl: "https://www.lidl.de/c/online-prospekte/s10005610",
        urlMode: "STATIC_ENTRYPOINT",
        requiresLocationContext: false,
        requiresStoreSelection: false,
      },
    ]);
    expect(fetch).toHaveBeenCalledWith("http://localhost:8080/api/v1/prospects?plz=65185");
  });

  it("wirft Backend-Fehler als Nutzermeldung weiter", async () => {
    vi.stubGlobal("fetch", vi.fn().mockResolvedValue({
      ok: false,
      json: async () => ({ message: "PLZ oder Region ist erforderlich." }),
    }));

    await expect(fetchProspects({})).rejects.toEqual({ message: "PLZ oder Region ist erforderlich." });
  });

  it("kodiert Händlerfilter im Query-String", async () => {
    const fetch = vi.fn().mockResolvedValue({
      ok: true,
      json: async () => ({ items: [] }),
    });
    vi.stubGlobal("fetch", fetch);

    await fetchProspects({ retailerIds: ["lidl", "penny"] });

    expect(fetch).toHaveBeenCalledWith("http://localhost:8080/api/v1/prospects?retailerIds=lidl%2Cpenny");
  });
});
