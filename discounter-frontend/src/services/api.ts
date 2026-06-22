import axios, { AxiosError } from "axios";
import type { LocationRequiredError, ProspectLink } from "../types";

const client = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL ?? "http://localhost:8080",
});

export async function fetchProspects(params: {
  plz?: string;
  region?: string;
  retailerIds?: string[];
}): Promise<ProspectLink[]> {
  const response = await client.get<ProspectLink[]>("/api/v1/prospects", {
    params: {
      plz: params.plz || undefined,
      region: params.region || undefined,
      retailerIds: params.retailerIds?.length ? params.retailerIds.join(",") : undefined,
    },
  });
  return response.data;
}

export function toUserMessage(error: unknown): string {
  const axiosError = error as AxiosError<LocationRequiredError>;
  if (axiosError.response?.data?.message) {
    return axiosError.response.data.message;
  }
  return "Prospekte konnten nicht geladen werden. Bitte später erneut versuchen.";
}
