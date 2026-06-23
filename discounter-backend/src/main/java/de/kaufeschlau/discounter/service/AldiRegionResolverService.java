package de.kaufeschlau.discounter.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.kaufeschlau.discounter.model.AldiRegion;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class AldiRegionResolverService {

    private final Map<String, String> plzPrefixToBundesland;
    private final Map<String, AldiRegion> bundeslandToAldiRegion;

    public AldiRegionResolverService() {
        try {
            var objectMapper = new ObjectMapper();
            var resource = new ClassPathResource("plz-bundesland.json");
            var mapping = objectMapper.readValue(resource.getInputStream(), new TypeReference<PlzMapping>() {});
            this.plzPrefixToBundesland = Map.copyOf(mapping.plzPrefixToBundesland());
            this.bundeslandToAldiRegion = Map.copyOf(mapping.bundeslandToAldiRegion());
        } catch (IOException exception) {
            throw new UncheckedIOException("PLZ-Bundesland-Mapping konnte nicht geladen werden.", exception);
        }
    }

    public AldiRegion resolve(String plz) {
        if (plz == null || !plz.matches("\\d{5}")) {
            throw new IllegalArgumentException("PLZ muss fünfstellig numerisch sein.");
        }

        var bundesland = plzPrefixToBundesland.get(plz.substring(0, 2));
        if (bundesland == null) {
            throw new IllegalArgumentException("PLZ-Präfix ist unbekannt: " + plz.substring(0, 2));
        }

        var region = bundeslandToAldiRegion.get(bundesland);
        if (region == null) {
            throw new IllegalArgumentException("Bundesland hat keine Aldi-Region: " + bundesland);
        }

        return region;
    }

    private record PlzMapping(
            Map<String, String> plzPrefixToBundesland,
            Map<String, AldiRegion> bundeslandToAldiRegion) {
    }
}
