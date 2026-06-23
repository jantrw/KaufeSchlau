package de.kaufeschlau.discounter.config;

import de.kaufeschlau.discounter.model.Discounter;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "kaufeschlau")
public record DiscounterConfig(List<Discounter> discounters) {
}
