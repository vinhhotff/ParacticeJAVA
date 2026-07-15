package org.example.homework.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app.allocation")
@Getter
@Setter
public class AllocationProperties {
    private int maxPercent = 100;
}
