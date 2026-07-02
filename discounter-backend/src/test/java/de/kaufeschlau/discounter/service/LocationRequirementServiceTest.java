package de.kaufeschlau.discounter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LocationRequirementServiceTest {

    @Autowired
    private LocationRequirementService service;

    @Test
    void requiresLocationForAllDiscounters() {
        var result = service.evaluate(List.of());

        assertThat(result.required()).isTrue();
        assertThat(result.discounterIds())
                .containsExactly("aldi-nord", "aldi-sued", "netto-marken-discount", "rewe", "edeka");
    }

    @Test
    void allowsManualSelectionWithoutLocationForLocationFreeDiscounters() {
        var result = service.evaluate(List.of("lidl", "penny", "kaufland"));

        assertThat(result.required()).isFalse();
    }

    @Test
    void allowsExplicitAldiSelectionWithoutLocation() {
        var result = service.evaluate(List.of("aldi-sued"));

        assertThat(result.required()).isFalse();
    }

    @Test
    void requiresLocationForPlzBasedDiscounter() {
        var result = service.evaluate(List.of("rewe"));

        assertThat(result.required()).isTrue();
        assertThat(result.discounterIds()).containsExactly("rewe");
    }

    @Test
    void rejectsUnknownDiscounter() {
        assertThatThrownBy(() -> service.evaluate(List.of("unknown")))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unbekannter Händler");
    }
}
