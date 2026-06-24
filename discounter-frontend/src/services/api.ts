import axios, { AxiosError } from "axios";
import type { LocationRequiredError, ProspectLink } from "../types";

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
});

type ApiProspectLink = Pick<ProspectLink, "id" | "name" | "prospectUrl"> &
  Partial<Omit<ProspectLink, "id" | "name" | "prospectUrl">>;

type ApiProspectsResponse = ApiProspectLink[] | { items: ApiProspectLink[] };

export async function fetchProspects(params: {
  plz?: string;
  region?: string;
  retailerIds?: string[];
}): Promise<ProspectLink[]> {
  const response = await client.get<ApiProspectsResponse>("/api/v1/prospects", {
    params: {
      plz: params.plz || undefined,
      region: params.region || undefined,
      retailerIds: params.retailerIds?.length ? params.retailerIds.join(",") : undefined,
    },
  });
  const items = Array.isArray(response.data) ? response.data : response.data.items;
  return items.map((item) => ({
    ...item,
    requiresLocationContext: item.requiresLocationContext ?? false,
    requiresStoreSelection: item.requiresStoreSelection ?? false,
  }));
}

export function toUserMessage(error: unknown): string {
  const axiosError = error as AxiosError<LocationRequiredError>;
  if (axiosError.response?.data?.message) {
    return axiosError.response.data.message;
  }
  return "Prospekte konnten nicht geladen werden. Bitte später erneut versuchen.";
}
