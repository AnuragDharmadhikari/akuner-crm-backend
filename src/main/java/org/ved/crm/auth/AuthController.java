package org.ved.crm.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.ved.crm.common.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Value("${application.jwt.expiration-ms}")
    private long expirationMs;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.register(request);
        setJwtCookie(response, authService.getLastGeneratedToken(), expirationMs);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", authResponse));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {
        AuthResponse authResponse = authService.login(request);
        setJwtCookie(response, authService.getLastGeneratedToken(), expirationMs);
        return ResponseEntity.ok(ApiResponse.success("Login successful", authResponse));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpServletResponse response) {
        // Clear the JWT cookie by setting max age to 0
        Cookie cookie = new Cookie("vedpharm_jwt", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    // Helper — sets the JWT as an httpOnly cookie
    // httpOnly = true → JavaScript cannot read this cookie (blocks XSS token theft)
    // secure = false for local dev (no HTTPS); set to true in production
    // sameSite = Strict → cookie not sent on cross-site requests (CSRF protection)
    // path = / → cookie sent with all requests to this domain
    private void setJwtCookie(HttpServletResponse response, String token, long expirationMs) {
        Cookie cookie = new Cookie("vedpharm_jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (expirationMs / 1000));
        response.addCookie(cookie);
    }
}