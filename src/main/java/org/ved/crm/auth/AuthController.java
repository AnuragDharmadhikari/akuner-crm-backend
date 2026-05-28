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
    private long accessExpirationMs;

    @Value("${application.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // ── Register ──────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletResponse response) {

        AuthService.TokenPair tokenPair = authService.register(request);
        setAccessTokenCookie(response, tokenPair.accessToken());
        setRefreshTokenCookie(response, tokenPair.refreshToken());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully",
                        tokenPair.authResponse()));
    }

    // ── Login ─────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletResponse response) {

        AuthService.TokenPair tokenPair = authService.login(request);
        setAccessTokenCookie(response, tokenPair.accessToken());
        setRefreshTokenCookie(response, tokenPair.refreshToken());

        return ResponseEntity.ok(ApiResponse.success("Login successful",
                tokenPair.authResponse()));
    }

    // ── Refresh ───────────────────────────────────────────────
    // Frontend calls this automatically when access token expires
    // Reads refresh token from cookie, issues new access + refresh tokens
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(
            HttpServletRequest request,
            HttpServletResponse response) {

        // Extract refresh token from cookie
        String refreshToken = extractCookieValue(request, "vedpharm_refresh");

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.failure("No refresh token found"));
        }

        AuthService.TokenPair tokenPair = authService.refresh(refreshToken);
        setAccessTokenCookie(response, tokenPair.accessToken());
        setRefreshTokenCookie(response, tokenPair.refreshToken());

        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully",
                tokenPair.authResponse()));
    }

    // ── Logout ────────────────────────────────────────────────
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            HttpServletRequest request,
            HttpServletResponse response) {

        // Extract refresh token and delete from DB
        String refreshToken = extractCookieValue(request, "vedpharm_refresh");
        authService.logout(refreshToken);

        // Clear both cookies by setting maxAge to 0
        clearCookie(response, "vedpharm_jwt", "/");
        clearCookie(response, "vedpharm_refresh", "/api/v1/auth/refresh");

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    // ── Cookie Helpers ────────────────────────────────────────

    // Sets the short-lived access token cookie
    // path=/ → sent with every request to the server
    private void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("vedpharm_jwt", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (accessExpirationMs / 1000));
        response.addCookie(cookie);
    }

    // Sets the long-lived refresh token cookie
    // path=/api/v1/auth/refresh → browser ONLY sends this cookie
    // to the refresh endpoint — not to every API call
    // This minimizes exposure of the long-lived token
    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("vedpharm_refresh", token);
        cookie.setHttpOnly(true);
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge((int) (refreshExpirationMs / 1000));
        response.addCookie(cookie);
    }

    // Clears a cookie by setting its maxAge to 0
    private void clearCookie(HttpServletResponse response, String name, String path) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    // Extracts a cookie value by name from the request
    private String extractCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        for (Cookie cookie : cookies) {
            if (cookieName.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}