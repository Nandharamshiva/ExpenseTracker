package org.example.expensetracker.auth.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.jwt")
public class JwtProperties {

    /**
     * HS256 secret. Must be at least 32 bytes (256 bits). Prefer a long random value.
     */
    private String secret;

    /**
     * Token issuer claim.
     */
    private String issuer = "expense-tracker";

    /**
     * Access token time-to-live.
     */
    private Duration accessTokenTtl = Duration.ofHours(24);
}

