package com.phantask.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Configuration class that runs initialization tasks on application startup.
 * Currently: delegates to AdminUserInitializerService to create default admin.
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitializer {

    // Inject the service that handles admin user creation
    private final AdminUserInitializerService initService;

    /**
     * CommandLineRunner bean that executes once when Spring Boot application starts.
     * Calls the service to create default admin user if needed.
     */
    @Bean
    CommandLineRunner initDatabase() {
        return args -> {
            log.info("Running database initialization checks...");
            initService.createDefaultAdminIfNotExists();
            log.info("Database initialization complete.");
        };
    }
}
