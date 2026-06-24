package de.kaufeschlau.discounter.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import de.kaufeschlau.discounter.model.AldiRegion;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class AldiRegionResolverServiceTest {

    @Autowired
    private AldiRegionResolverService service;

    @Test
    void resolvesHessianPlzToAldiSued() {
        assertThat(service.resolve("65185")).isEqualTo(AldiRegion.SUED);
    }

    @Test
    void resolvesHamburgPlzToAldiNord() {
        assertThat(service.resolve("20095")).isEqualTo(AldiRegion.NORD);
    }

    @Test
    void resolvesHessenRegionToAldiSued() {
        assertThat(service.resolveRegion("hessen")).isEqualTo(AldiRegion.SUED);
    }

    @Test
    void rejectsInvalidPlz() {
        assertThatThrownBy(() -> service.resolve("6518"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("fünfstellig");
    }
}
