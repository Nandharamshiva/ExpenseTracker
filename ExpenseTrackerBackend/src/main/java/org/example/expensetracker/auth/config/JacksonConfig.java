package org.example.expensetracker.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Ensures an ObjectMapper bean exists for JSON error responses.
 *
 * Spring Boot normally auto-configures this when Jackson is on the classpath,
 * but we define it explicitly to keep startup deterministic.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}

