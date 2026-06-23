package de.kaufeschlau.discounter.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record LocationRequirement(boolean required, LocationRequirementType type, Set<String> discounterIds) {

    public static LocationRequirement none() {
        return new LocationRequirement(false, LocationRequirementType.NONE, Set.of());
    }

    public static LocationRequirement plzOrRegion(Set<String> discounterIds) {
        return new LocationRequirement(
                true,
                LocationRequirementType.PLZ_OR_REGION,
                Collections.unmodifiableSet(new LinkedHashSet<>(discounterIds)));
    }
}
