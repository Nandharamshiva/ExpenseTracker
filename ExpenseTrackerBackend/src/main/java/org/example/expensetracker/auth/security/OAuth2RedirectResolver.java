package org.example.expensetracker.auth.security;

import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OAuth2RedirectResolver {

    /**
     * Comma-separated list of allowed redirect origins, e.g. "http://localhost:5173,https://app.example.com".
     * Only the origin is checked (scheme + host + optional port).
     */
    @Value("${app.oauth2.allowed-redirect-origins:http://localhost:5173}")
    private String allowedRedirectOrigins;

    /** Default path on the frontend to land on after OAuth2 login */
    @Value("${app.oauth2.default-redirect-path:/login}")
    private String defaultRedirectPath;

    public URI resolveAndValidateRedirect(String requestedRedirect) {
        URI fallback = URI.create("http://localhost:5173" + defaultRedirectPath);

        URI candidate;
        if (requestedRedirect == null || requestedRedirect.isBlank()) {
            candidate = fallback;
        } else {
            try {
                candidate = URI.create(requestedRedirect);
            } catch (IllegalArgumentException ex) {
                return fallback;
            }
        }

        // Only allow absolute URIs with an allowed origin.
        if (candidate.getScheme() == null || candidate.getHost() == null) {
            return fallback;
        }

        String candidateOrigin = originOf(candidate);
        if (!allowedOrigins().contains(candidateOrigin)) {
            return fallback;
        }

        return candidate;
    }

    private Set<String> allowedOrigins() {
        Set<String> out = new HashSet<>();
        Arrays.stream(allowedRedirectOrigins.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .forEach(out::add);
        return out;
    }

    private static String originOf(URI uri) {
        int port = uri.getPort();
        String origin = uri.getScheme() + "://" + uri.getHost();
        return port == -1 ? origin : origin + ":" + port;
    }
}
