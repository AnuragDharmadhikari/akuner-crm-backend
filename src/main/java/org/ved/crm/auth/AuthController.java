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
            @Valid @RequestBody RegisterRequest request) {

        AuthService.TokenPair tokenPair = authService.register(request);

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
        String refreshToken = extractCookieValue(request, "akuner_refresh");

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
        String refreshToken = extractCookieValue(request, "akuner_refresh");
        authService.logout(refreshToken);

        clearCookie(response, "akuner_jwt", "/");
        clearCookie(response, "akuner_refresh", "/api/v1/auth/refresh");

        return ResponseEntity.ok(ApiResponse.success("Logged out successfully"));
    }

    // ── Cookie Helpers ────────────────────────────────────────

    private void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("akuner_jwt", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge((int) (accessExpirationMs / 1000));
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("akuner_refresh", token);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/api/v1/auth/refresh");
        cookie.setMaxAge((int) (refreshExpirationMs / 1000));
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

    private void clearCookie(HttpServletResponse response, String name, String path) {
        Cookie cookie = new Cookie(name, "");
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath(path);
        cookie.setMaxAge(0);
        cookie.setAttribute("SameSite", "Lax");
        response.addCookie(cookie);
    }

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