package de.kaufeschlau.discounter.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class DiscounterConfigTest {

    @Autowired
    private DiscounterConfig config;

    @Test
    void loadsRepresentativeDiscounterRules() {
        assertThat(config.discounters())
                .filteredOn(discounter -> discounter.id().equals("rewe"))
                .singleElement()
                .satisfies(discounter -> {
                    assertThat(discounter.requiresLocationContext()).isTrue();
                    assertThat(discounter.requiresStoreSelection()).isTrue();
                    assertThat(discounter.marketSearchUrl()).isEqualTo("https://www.rewe.de/marktsuche/");
                });

        assertThat(config.discounters())
                .filteredOn(discounter -> discounter.id().equals("lidl"))
                .singleElement()
                .satisfies(discounter -> {
                    assertThat(discounter.requiresLocationContext()).isFalse();
                    assertThat(discounter.requiresStoreSelection()).isFalse();
                });
    }
}
