package de.kaufeschlau.discounter.controller;

import de.kaufeschlau.discounter.config.DiscounterConfig;
import de.kaufeschlau.discounter.model.AldiRegion;
import de.kaufeschlau.discounter.model.Discounter;
import de.kaufeschlau.discounter.service.AldiRegionResolverService;
import de.kaufeschlau.discounter.service.LocationRequirementService;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProspectController {

    private final DiscounterConfig config;
    private final AldiRegionResolverService aldiRegionResolverService;
    private final LocationRequirementService locationRequirementService;

    public ProspectController(
            DiscounterConfig config,
            AldiRegionResolverService aldiRegionResolverService,
            LocationRequirementService locationRequirementService) {
        this.config = config;
        this.aldiRegionResolverService = aldiRegionResolverService;
        this.locationRequirementService = locationRequirementService;
    }

    @GetMapping("/api/v1/prospects")
    public ResponseEntity<?> list(
            @RequestParam(required = false) String plz,
            @RequestParam(required = false) String region,
            @RequestParam(required = false) List<String> retailerIds) {
        var selectedIds = selectedIds(retailerIds);
        try {
            var requirement = locationRequirementService.evaluate(selectedIds);
            if (requirement.required() && blank(plz) && blank(region)) {
                return badRequest(
                        "LOCATION_REQUIRED",
                        "PLZ oder Region erforderlich für: " + requirement.discounterIds());
            }
        } catch (IllegalArgumentException e) {
            return badRequest("UNKNOWN_DISCOUNTER", e.getMessage());
        }

        AldiRegion aldiRegion;
        try {
            aldiRegion = selectedIds.isEmpty() ? aldiRegion(plz, region) : null;
        } catch (IllegalArgumentException e) {
            return badRequest("INVALID_LOCATION", e.getMessage());
        }

        var prospects = config.discounters().stream()
                .filter(discounter -> selectedIds.isEmpty() || selectedIds.contains(discounter.id()))
                .filter(discounter -> matchesAldiRegion(discounter, selectedIds, aldiRegion))
                .map(ProspectResponse::from)
                .toList();
        return ResponseEntity.ok(prospects);
    }

    private AldiRegion aldiRegion(String plz, String region) {
        if (!blank(plz)) {
            return aldiRegionResolverService.resolve(plz);
        }
        if (!blank(region)) {
            return aldiRegionResolverService.resolveRegion(region);
        }
        return null;
    }

    private static boolean matchesAldiRegion(Discounter discounter, Collection<String> selectedIds, AldiRegion aldiRegion) {
        if (!selectedIds.isEmpty() || aldiRegion == null || discounter.aldiRegion() == null) {
            return true;
        }
        return discounter.aldiRegion() == aldiRegion;
    }

    private static List<String> selectedIds(Collection<String> retailerIds) {
        if (retailerIds == null) {
            return List.of();
        }
        return retailerIds.stream()
                .flatMap(value -> Arrays.stream(value.split(",")))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .toList();
    }

    private static boolean blank(String value) {
        return value == null || value.isBlank();
    }

    record ProspectResponse(String id, String name, String prospectUrl, String notice) {

        static ProspectResponse from(Discounter discounter) {
            return new ProspectResponse(
                    discounter.id(),
                    discounter.name(),
                    discounter.prospectUrl(),
                    discounter.locationRequirementReason());
        }
    }

    private static ResponseEntity<ErrorResponse> badRequest(String code, String message) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new ErrorResponse(code, message));
    }

    record ErrorResponse(String code, String message) {
    }
}
