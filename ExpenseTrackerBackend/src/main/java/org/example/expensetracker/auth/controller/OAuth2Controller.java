package org.example.expensetracker.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth/oauth2")
public class OAuth2Controller {

    /**
     * Default redirect target for OAuth2 login.
     * If you don't pass redirect_uri during OAuth login, OAuth2SuccessHandler redirects here with ?token=...
     */
    @GetMapping("/success")
    public ResponseEntity<?> success(@RequestParam("token") String token) {
        return ResponseEntity.ok(java.util.Map.of(
                "message", "OAuth2 login successful",
                "tokenType", "Bearer",
                "token", token
        ));
    }
}

