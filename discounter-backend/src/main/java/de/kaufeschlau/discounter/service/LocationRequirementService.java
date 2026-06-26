package de.kaufeschlau.discounter.service;

import de.kaufeschlau.discounter.config.DiscounterConfig;
import de.kaufeschlau.discounter.model.Discounter;
import de.kaufeschlau.discounter.model.LocationRequirement;
import java.util.Collection;
import java.util.List;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class LocationRequirementService {

    private final Map<String, Discounter> discountersById;

    public LocationRequirementService(DiscounterConfig config) {
        this.discountersById = new LinkedHashMap<>();
        for (var discounter : config.discounters()) {
            discountersById.put(discounter.id(), discounter);
        }
    }

    public LocationRequirement evaluate(Collection<String> selectedDiscounterIds) {
        var allDiscountersRequested = selectedDiscounterIds == null || selectedDiscounterIds.isEmpty();
        var idsToCheck = allDiscountersRequested ? discountersById.keySet() : selectedDiscounterIds;
        var requiredIds = new LinkedHashSet<String>();

        for (var id : idsToCheck) {
            var discounter = discountersById.get(id);
            if (discounter == null) {
                throw new IllegalArgumentException("Unbekannter Händler: " + id);
            }
            if (requiresLocation(discounter, allDiscountersRequested)) {
                requiredIds.add(id);
            }
        }

        return requiredIds.isEmpty() ? LocationRequirement.none() : LocationRequirement.plzOrRegion(requiredIds);
    }

    public List<Discounter> allDiscounters() {
        return List.copyOf(discountersById.values());
    }

    public Optional<Discounter> findDiscounter(String id) {
        return Optional.ofNullable(discountersById.get(id));
    }

    private boolean requiresLocation(Discounter discounter, boolean allDiscountersRequested) {
        if (allDiscountersRequested) {
            return discounter.requiresLocationContext();
        }
        return discounter.requiresLocationContext() && !discounter.supportsManualSelectionWithoutPlz();
    }
}
