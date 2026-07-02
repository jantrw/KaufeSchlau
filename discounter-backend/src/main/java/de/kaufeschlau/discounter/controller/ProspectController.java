package de.kaufeschlau.discounter.controller;

import de.kaufeschlau.discounter.model.AldiRegion;
import de.kaufeschlau.discounter.model.Discounter;
import de.kaufeschlau.discounter.model.LocationRequirement;
import de.kaufeschlau.discounter.model.RegionType;
import de.kaufeschlau.discounter.model.UrlMode;
import de.kaufeschlau.discounter.service.AldiRegionResolverService;
import de.kaufeschlau.discounter.service.LocationRequirementService;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/api/v1/prospects")
class ProspectController {

    private static final String FALLBACK_HINT =
            "Phase 1 nutzt den offiziellen Einstiegspunkt. Filialgenaue Auflösung folgt später.";

    private final LocationRequirementService locationRequirementService;
    private final AldiRegionResolverService aldiRegionResolverService;

    ProspectController(
            LocationRequirementService locationRequirementService,
            AldiRegionResolverService aldiRegionResolverService) {
        this.locationRequirementService = locationRequirementService;
        this.aldiRegionResolverService = aldiRegionResolverService;
    }

    @GetMapping
    ProspectListResponse listProspects(
            @RequestParam(required = false) List<String> retailerIds,
            @RequestParam(required = false) String plz,
            @RequestParam(required = false) String region) {
        var selectedIds = normalizeIds(retailerIds);
        var requestedDiscounters = selectedIds.isEmpty()
                ? locationRequirementService.allDiscounters()
                : selectedIds.stream().map(this::getDiscounter).toList();
        var requirement = validateLocation(requestedDiscounters, selectedIds, plz, region);
        var discounters = selectedIds.isEmpty()
                ? filterAutomaticAldi(requestedDiscounters, plz, region)
                : requestedDiscounters;

        return new ProspectListResponse(discounters.stream()
                .map(discounter -> toResponse(discounter, requirement))
                .toList());
    }

    @GetMapping("/{id}")
    ProspectResponse getProspect(
            @PathVariable String id,
            @RequestParam(required = false) String plz,
            @RequestParam(required = false) String region) {
        var discounter = getDiscounter(id);
        var requirement = validateLocation(List.of(discounter), List.of(id), plz, region);
        return toResponse(discounter, requirement);
    }

    private LocationRequirement validateLocation(
            Collection<Discounter> discounters,
            List<String> selectedIds,
            String plz,
            String region) {
        var requirement = locationRequirementService.evaluate(selectedIds);
        try {
            if (StringUtils.hasText(plz)) {
                aldiRegionResolverService.resolve(plz);
                return requirement;
            }
            if (StringUtils.hasText(region)) {
                if (discounters.stream().anyMatch(discounter -> discounter.regionType() == RegionType.PLZ_BASIERT)) {
                    aldiRegionResolverService.resolveBundeslandRegion(region);
                } else {
                    aldiRegionResolverService.resolveRegion(region);
                }
                return requirement;
            }
        } catch (IllegalArgumentException exception) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage());
        }

        if (requirement.required()) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "LOCATION_REQUIRED",
                    "PLZ oder Region ist erforderlich für: " + String.join(", ", requirement.discounterIds()));
        }
        return requirement;
    }

    private Discounter getDiscounter(String id) {
        return locationRequirementService.findDiscounter(id)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "RETAILER_NOT_FOUND",
                        "Unbekannter Händler: " + id));
    }

    private List<Discounter> filterAutomaticAldi(Collection<Discounter> discounters, String plz, String region) {
        var aldiRegion = resolveAldiRegion(plz, region);
        if (aldiRegion == null) {
            return List.copyOf(discounters);
        }

        return discounters.stream()
                .filter(discounter -> discounter.regionType() != RegionType.ALDI_REGION
                        || discounter.aldiRegion() == aldiRegion)
                .toList();
    }

    private AldiRegion resolveAldiRegion(String plz, String region) {
        if (StringUtils.hasText(plz)) {
            return aldiRegionResolverService.resolve(plz);
        }
        if (!StringUtils.hasText(region)) {
            return null;
        }

        return aldiRegionResolverService.resolveRegion(region);
    }

    private ProspectResponse toResponse(Discounter discounter, LocationRequirement requirement) {
        return new ProspectResponse(
                discounter.id(),
                discounter.name(),
                discounter.prospectUrl(),
                discounter.regionType(),
                discounter.urlMode(),
                discounter.aldiRegion(),
                requirement.discounterIds().contains(discounter.id()),
                discounter.requiresStoreSelection(),
                notice(discounter),
                discounter.marketSearchUrl());
    }

    private String notice(Discounter discounter) {
        return discounter.urlMode() == UrlMode.LOCATION_RESOLVED ? FALLBACK_HINT : null;
    }

    private List<String> normalizeIds(List<String> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream()
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
    }

    record ProspectListResponse(List<ProspectResponse> items) {
    }

    record ProspectResponse(
            String id,
            String name,
            String prospectUrl,
            RegionType regionType,
            UrlMode urlMode,
            AldiRegion resolvedRegion,
            boolean requiresLocationContext,
            boolean requiresStoreSelection,
            String notice,
            String marketSearchUrl) {
    }
}
