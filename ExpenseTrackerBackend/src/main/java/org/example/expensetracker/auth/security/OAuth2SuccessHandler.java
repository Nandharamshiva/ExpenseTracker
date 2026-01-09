package org.example.expensetracker.auth.security;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Locale;

import org.example.expensetracker.auth.entity.Role;
import org.example.expensetracker.auth.entity.User;
import org.example.expensetracker.auth.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final OAuth2RedirectResolver redirectResolver;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        if (!(authentication.getPrincipal() instanceof OAuth2User oAuth2User)) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized");
            return;
        }

        String email = asString(oAuth2User.getAttributes().get("email"));
        if (email == null || email.isBlank()) {
            // GitHub may not return email unless scope user:email and the email is public/accessible.
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Email not available from OAuth provider");
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .username(defaultUsernameFromEmail(email))
                        .email(email)
                        // Not used for OAuth sign-in; store a non-empty placeholder.
                        .password("{oauth2}")
                        .role(Role.USER)
                        .enabled(true)
                        .build()));

        String token = jwtUtil.generateToken(user);

        // redirect_uri is optional, but MUST be allowlisted (origin check) to prevent open redirect/token exfiltration.
        String requestedRedirect = request.getParameter("redirect_uri");
        URI redirect = redirectResolver.resolveAndValidateRedirect(requestedRedirect);

        // Prefer URL fragment so the token isn't sent to servers in HTTP logs/referrers.
        // Frontend reads: window.location.hash
        response.sendRedirect(withFragmentParam(redirect, "token", urlEncode(token)));
    }

    private static String asString(Object v) {
        return v == null ? null : String.valueOf(v);
    }

    private static String defaultUsernameFromEmail(String email) {
        String base = email.split("@", 2)[0]
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9._-]", "");
        if (base.length() < 6) {
            base = (base + "user");
        }
        return base.length() > 50 ? base.substring(0, 50) : base;
    }

    private static String urlEncode(String raw) {
        // Minimal encoding for JWT in URL.
        return java.net.URLEncoder.encode(raw, StandardCharsets.UTF_8);
    }

    private static String withFragmentParam(URI base, String key, String value) {
        String fragment = base.getFragment();
        String nextFragment;
        if (fragment == null || fragment.isBlank()) {
            nextFragment = key + "=" + value;
        } else {
            // Preserve existing fragment and append.
            nextFragment = fragment + (fragment.contains("=") ? "&" : "&") + key + "=" + value;
        }

        // Drop existing fragment from base string and re-add ours.
        String baseStr = base.toString();
        int hash = baseStr.indexOf('#');
        if (hash >= 0) {
            baseStr = baseStr.substring(0, hash);
        }
        return baseStr + "#" + nextFragment;
    }
}
