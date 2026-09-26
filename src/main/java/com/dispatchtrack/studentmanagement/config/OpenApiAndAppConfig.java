package com.dispatchtrack.studentmanagement.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.config.MeterFilter;
import org.springframework.boot.actuate.autoconfigure.metrics.MeterRegistryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * General application-level bean configuration.
 *
 * Kept deliberately small for this project, but this is the natural home
 * for cross-cutting @Bean definitions (CORS, ObjectMapper customizers,
 * OpenAPI/Swagger config, etc.) as the application grows - which is why the
 * "config" package exists as its own layer even though it only has one
 * class today.
 */
@Configuration
public class OpenApiAndAppConfig {

    /**
     * Tags every metric published by Actuator/Micrometer with
     * application="student-management-system", so Prometheus/Grafana can
     * filter or group by application when multiple services share the
     * same Prometheus instance.
     */
    @Bean
    public MeterRegistryCustomizer<MeterRegistry> metricsCommonTags() {
        return registry -> registry.config().commonTags("application", "student-management-system");
    }

    /**
     * Example of a MeterFilter bean: silently drop noisy JVM metrics from the
     * /actuator/prometheus output if you ever need a leaner dashboard.
     * Disabled by default (returns MeterFilter.accept() for everything).
     */
    @Bean
    public MeterFilter meterFilter() {
        return MeterFilter.accept();
    }
}
