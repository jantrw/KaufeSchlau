package de.kaufeschlau.discounter.config;

import static org.assertj.core.api.Assertions.assertThat;

import de.kaufeschlau.discounter.model.RegionType;
import de.kaufeschlau.discounter.model.UrlMode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DiscounterConfigTest {

    @Autowired
    private DiscounterConfig config;

    @Test
    void loadsEightDefinedDiscounters() {
        assertThat(config.discounters()).hasSize(8);
        assertThat(config.discounters())
                .extracting("id")
                .containsExactly(
                        "aldi-nord",
                        "aldi-sued",
                        "lidl",
                        "penny",
                        "netto-marken-discount",
                        "kaufland",
                        "rewe",
                        "edeka");
        assertThat(config.discounters())
                .filteredOn(discounter -> discounter.id().equals("rewe"))
                .singleElement()
                .extracting("urlMode")
                .isEqualTo(UrlMode.LOCATION_RESOLVED);
        assertThat(config.discounters())
                .filteredOn(discounter -> discounter.id().startsWith("aldi-"))
                .allSatisfy(discounter -> {
                    assertThat(discounter.regionType()).isEqualTo(RegionType.ALDI_REGION);
                    assertThat(discounter.requiresLocationContext()).isTrue();
                    assertThat(discounter.supportsManualSelectionWithoutPlz()).isTrue();
                    assertThat(discounter.requiresStoreSelection()).isFalse();
                });
        assertThat(config.discounters())
                .filteredOn(discounter -> discounter.id().matches("rewe|edeka|netto-marken-discount"))
                .allSatisfy(discounter -> {
                    assertThat(discounter.regionType()).isEqualTo(RegionType.PLZ_BASIERT);
                    assertThat(discounter.urlMode()).isEqualTo(UrlMode.LOCATION_RESOLVED);
                    assertThat(discounter.requiresLocationContext()).isTrue();
                    assertThat(discounter.supportsManualSelectionWithoutPlz()).isFalse();
                    assertThat(discounter.requiresStoreSelection()).isTrue();
                });
    }
}
