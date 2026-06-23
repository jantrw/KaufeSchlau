package de.kaufeschlau.discounter;

import de.kaufeschlau.discounter.config.DiscounterConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(DiscounterConfig.class)
public class DiscounterApplication {

    public static void main(String[] args) {
        SpringApplication.run(DiscounterApplication.class, args);
    }
}
