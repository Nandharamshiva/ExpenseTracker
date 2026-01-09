package org.example.expensetracker.auth.security;

import java.io.IOException;
import java.net.URI;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final OAuth2RedirectResolver redirectResolver;

    public OAuth2FailureHandler(OAuth2RedirectResolver redirectResolver) {
        this.redirectResolver = redirectResolver;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {

        // Don't leak internal exception details to the client.
        String requestedRedirect = request.getParameter("redirect_uri");
        URI redirect = redirectResolver.resolveAndValidateRedirect(requestedRedirect);

        // Use fragment to avoid sending error codes to servers via referrers/logs.
        String base = redirect.toString();
        int hash = base.indexOf('#');
        if (hash >= 0) base = base.substring(0, hash);
        response.sendRedirect(base + "#error=oauth2_failed");
    }
}
