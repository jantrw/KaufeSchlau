import { describe, expect, it, vi } from "vitest";
import { fetchProspects } from "../services/api";

const { get } = vi.hoisted(() => ({
  get: vi.fn(),
}));

vi.mock("axios", () => ({
  default: {
    create: vi.fn(() => ({ get })),
  },
}));

describe("fetchProspects", () => {
  it("liest den Backend-Response mit items und minimalem Prospect-Shape", async () => {
    get.mockResolvedValueOnce({
      data: {
        items: [
          {
            id: "lidl",
            name: "Lidl",
            prospectUrl: "https://www.lidl.de/c/online-prospekte/s10005610",
            urlMode: "STATIC_ENTRYPOINT",
          },
        ],
      },
    });

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
    expect(get).toHaveBeenCalledWith("/api/v1/prospects", {
      params: { plz: "65185", region: undefined, retailerIds: undefined },
    });
  });
});
