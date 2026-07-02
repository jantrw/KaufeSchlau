package de.kaufeschlau.discounter.model;

public record Discounter(
        String id,
        String name,
        RegionType regionType,
        UrlMode urlMode,
        AldiRegion aldiRegion,
        String prospectUrl,
        String marketSearchUrl,
        boolean requiresLocationContext,
        boolean requiresStoreSelection) {
}
