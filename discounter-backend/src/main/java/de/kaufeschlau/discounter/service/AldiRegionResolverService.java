package de.kaufeschlau.discounter.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.kaufeschlau.discounter.model.AldiRegion;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;
import java.util.Locale;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class AldiRegionResolverService {

    private final Map<String, String> plzPrefixToBundesland;
    private final Map<String, AldiRegion> bundeslandToAldiRegion;
    private final Map<String, AldiRegion> regionToAldiRegion;

    public AldiRegionResolverService() {
        try {
            var objectMapper = new ObjectMapper();
            var resource = new ClassPathResource("plz-bundesland.json");
            var mapping = objectMapper.readValue(resource.getInputStream(), new TypeReference<PlzMapping>() {});
            this.plzPrefixToBundesland = Map.copyOf(mapping.plzPrefixToBundesland());
            this.bundeslandToAldiRegion = Map.copyOf(mapping.bundeslandToAldiRegion());
            this.regionToAldiRegion = Map.ofEntries(
                    Map.entry("baden-wuerttemberg", AldiRegion.SUED),
                    Map.entry("baden-württemberg", AldiRegion.SUED),
                    Map.entry("bayern", AldiRegion.SUED),
                    Map.entry("hessen", AldiRegion.SUED),
                    Map.entry("rheinland-pfalz", AldiRegion.SUED),
                    Map.entry("saarland", AldiRegion.SUED),
                    Map.entry("berlin", AldiRegion.NORD),
                    Map.entry("brandenburg", AldiRegion.NORD),
                    Map.entry("bremen", AldiRegion.NORD),
                    Map.entry("hamburg", AldiRegion.NORD),
                    Map.entry("mecklenburg-vorpommern", AldiRegion.NORD),
                    Map.entry("niedersachsen", AldiRegion.NORD),
                    Map.entry("nordrhein-westfalen", AldiRegion.NORD),
                    Map.entry("sachsen", AldiRegion.NORD),
                    Map.entry("sachsen-anhalt", AldiRegion.NORD),
                    Map.entry("schleswig-holstein", AldiRegion.NORD),
                    Map.entry("thueringen", AldiRegion.NORD),
                    Map.entry("thüringen", AldiRegion.NORD),
                    Map.entry("nord", AldiRegion.NORD),
                    Map.entry("sued", AldiRegion.SUED),
                    Map.entry("süd", AldiRegion.SUED));
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

    public AldiRegion resolveRegion(String region) {
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region darf nicht leer sein.");
        }

        var aldiRegion = regionToAldiRegion.get(region.trim().toLowerCase(Locale.GERMAN));
        if (aldiRegion == null) {
            throw new IllegalArgumentException("Region ist unbekannt: " + region);
        }
        return aldiRegion;
    }

    private record PlzMapping(
            Map<String, String> plzPrefixToBundesland,
            Map<String, AldiRegion> bundeslandToAldiRegion) {
    }
}
