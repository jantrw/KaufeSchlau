export interface Retailer {
  id: string;
  name: string;
  requiresLocationContext: boolean;
}

export interface ProspectLink {
  id: string;
  name: string;
  regionType?: string;
  urlMode?: string;
  prospectUrl: string;
  marketSearchUrl?: string;
  notice?: string;
  resolvedRegion?: string;
  requiresLocationContext: boolean;
  requiresStoreSelection: boolean;
}
