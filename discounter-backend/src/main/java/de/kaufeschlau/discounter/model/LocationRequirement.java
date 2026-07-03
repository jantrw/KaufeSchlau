package de.kaufeschlau.discounter.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public record LocationRequirement(boolean required, Set<String> discounterIds) {

    public static LocationRequirement none() {
        return new LocationRequirement(false, Set.of());
    }

    public static LocationRequirement plzOrRegion(Set<String> discounterIds) {
        return new LocationRequirement(true, Collections.unmodifiableSet(new LinkedHashSet<>(discounterIds)));
    }
}
