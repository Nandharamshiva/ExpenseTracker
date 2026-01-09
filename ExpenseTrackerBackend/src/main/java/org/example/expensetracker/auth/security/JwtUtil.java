package org.example.expensetracker.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.example.expensetracker.auth.entity.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    private final JwtProperties props;

    public JwtUtil(JwtProperties props) {
        this.props = props;
    }

    private Key getSigningKey() {
        if (props.getSecret() == null || props.getSecret().isBlank()) {
            throw new IllegalStateException("JWT secret is not configured (app.jwt.secret)");
        }

        byte[] keyBytes = props.getSecret().getBytes(StandardCharsets.UTF_8);
        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes for HS256");
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        long ttlMs = props.getAccessTokenTtl().toMillis();

        return Jwts.builder()
                .setIssuer(props.getIssuer())
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim("email", user.getEmail())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + ttlMs))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }

    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .requireIssuer(props.getIssuer())
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (ExpiredJwtException e) {
            logger.debug("JWT token expired");
        } catch (JwtException | IllegalArgumentException e) {
            // JwtException covers signature, malformed, unsupported, etc.
            logger.debug("JWT validation error: {}", e.getMessage());
        }

        return false;
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .requireIssuer(props.getIssuer())
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
