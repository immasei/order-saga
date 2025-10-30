package com.example.emailservice.config;

import org.flywaydb.core.Flyway;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Development convenience: if migration SQL files changed after being applied,
 * repair the schema history to match current checksums, then migrate.
 * Do not use in production.
 */
@Configuration
public class FlywayRepairOnStartConfig {

    @Bean
    public FlywayMigrationStrategy repairThenMigrate() {
        return flyway -> {
            // Update checksums to match current scripts, then apply pending migrations
            flyway.repair();
            flyway.migrate();
        };
    }
}

