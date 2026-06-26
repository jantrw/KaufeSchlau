package de.kaufeschlau.discounter.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.kaufeschlau.discounter.model.AldiRegion;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Locale;
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

    public AldiRegion resolveRegion(String region) {
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region darf nicht leer sein.");
        }

        var normalizedRegion = region.trim().toLowerCase(Locale.ROOT);
        var directAldiRegion = switch (normalizedRegion) {
            case "nord", "aldi-nord" -> AldiRegion.NORD;
            case "sued", "süd", "aldi-sued", "aldi-süd" -> AldiRegion.SUED;
            default -> null;
        };
        if (directAldiRegion != null) {
            return directAldiRegion;
        }

        var bundesland = bundeslandCode(normalizedRegion, region);
        var aldiRegion = bundeslandToAldiRegion.get(bundesland);
        if (aldiRegion == null) {
            throw new IllegalArgumentException("Region ist unbekannt: " + region);
        }

        return aldiRegion;
    }

    public AldiRegion resolveBundeslandRegion(String region) {
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region darf nicht leer sein.");
        }

        var bundesland = bundeslandCode(region.trim().toLowerCase(Locale.ROOT), region);
        var aldiRegion = bundeslandToAldiRegion.get(bundesland);
        if (aldiRegion == null) {
            throw new IllegalArgumentException("Region ist unbekannt: " + region);
        }

        return aldiRegion;
    }

    private String bundeslandCode(String normalizedRegion, String originalRegion) {
        return switch (normalizedRegion) {
            case "baden-württemberg", "baden-wuerttemberg", "bw" -> "BW";
            case "bayern", "by" -> "BY";
            case "berlin", "be" -> "BE";
            case "brandenburg", "bb" -> "BB";
            case "bremen", "hb" -> "HB";
            case "hamburg", "hh" -> "HH";
            case "hessen", "he" -> "HE";
            case "mecklenburg-vorpommern", "mv" -> "MV";
            case "niedersachsen", "ni" -> "NI";
            case "nordrhein-westfalen", "nrw", "nw" -> "NW";
            case "rheinland-pfalz", "rp" -> "RP";
            case "saarland", "sl" -> "SL";
            case "sachsen", "sn" -> "SN";
            case "sachsen-anhalt", "st" -> "ST";
            case "schleswig-holstein", "sh" -> "SH";
            case "thüringen", "thueringen", "th" -> "TH";
            default -> throw new IllegalArgumentException("Region ist unbekannt: " + originalRegion);
        };
    }

    private record PlzMapping(
            Map<String, String> plzPrefixToBundesland,
            Map<String, AldiRegion> bundeslandToAldiRegion) {
    }
}
