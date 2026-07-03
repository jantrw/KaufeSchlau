import type { ProspectLink } from "../types";

const baseUrl = import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080";

type ApiProspectLink = Pick<ProspectLink, "id" | "name" | "prospectUrl"> &
  Partial<Omit<ProspectLink, "id" | "name" | "prospectUrl">>;

type ApiProspectsResponse = { items: ApiProspectLink[] };
type ApiErrorResponse = { message?: string };

export async function fetchProspects(params: {
  plz?: string;
  region?: string;
  retailerIds?: string[];
}): Promise<ProspectLink[]> {
  const query = new URLSearchParams();
  add(query, "plz", params.plz);
  add(query, "region", params.region);
  if (params.retailerIds?.length) {
    query.set("retailerIds", params.retailerIds.join(","));
  }

  const response = await fetch(`${baseUrl}/api/v1/prospects?${query}`);
  if (!response.ok) {
    throw await response.json().catch(() => ({}));
  }

  const data = (await response.json()) as ApiProspectsResponse;
  return data.items.map((item) => ({
    ...item,
    requiresLocationContext: item.requiresLocationContext ?? false,
    requiresStoreSelection: item.requiresStoreSelection ?? false,
  }));
}

export function toUserMessage(error: unknown): string {
  const apiError = error as ApiErrorResponse;
  if (apiError.message) {
    return apiError.message;
  }
  return "Prospekte konnten nicht geladen werden. Bitte später erneut versuchen.";
}

function add(params: URLSearchParams, name: string, value: string | undefined) {
  if (value) {
    params.set(name, value);
  }
}
