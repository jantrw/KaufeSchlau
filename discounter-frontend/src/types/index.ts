export interface Retailer {
  id: string;
  name: string;
  requiresLocationContext: boolean;
  requiresStoreSelection: boolean;
}

export interface ProspectLink extends Retailer {
  regionType?: string;
  urlMode?: string;
  prospectUrl: string;
  marketSearchUrl?: string;
  notice?: string;
  resolvedRegion?: string;
  resolvedDynamically?: boolean;
  fallbackUsed?: boolean;
}

export interface LocationRequiredError {
  error: "LOCATION_REQUIRED";
  message: string;
  requiredForRetailers?: string[];
}
