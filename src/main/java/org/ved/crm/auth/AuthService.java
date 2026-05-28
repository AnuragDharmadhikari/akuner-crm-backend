package org.ved.crm.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ved.crm.audit.Audited;
import org.ved.crm.security.JwtService;
import org.ved.crm.security.LoginRateLimiter;
import org.ved.crm.user.Role;
import org.ved.crm.user.User;
import org.ved.crm.user.UserRepository;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final LoginRateLimiter loginRateLimiter;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${application.jwt.expiration-ms}")
    private long expirationMs;

    // ── Internal record ───────────────────────────────────────
    // Carries both tokens from service to controller
    // Using a record keeps it clean — no need for a separate DTO class
    // It's package-private (no modifier) — only used within auth package
    record TokenPair(String accessToken, String refreshToken, AuthResponse authResponse) {}

    // ── Register ──────────────────────────────────────────────
    @Audited(action = "USER_REGISTERED", entityType = "User")
    @PreAuthorize("hasRole('OWNER')")
    @Transactional
    public TokenPair register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException(
                    "Email already registered: " + request.email());
        }

        User user = User.builder()
                .fullName(request.fullName())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(request.role() != null ? request.role() : Role.REP)
                .phone(request.phone())
                .build();

        userRepository.save(user);

        var userDetails = org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();

        String accessToken = jwtService.generateToken(
                userDetails, user.getId(), user.getRole().name());
        String refreshToken = refreshTokenService.createRefreshToken(user);

        return new TokenPair(
                accessToken,
                refreshToken,
                AuthResponse.of(user.getEmail(), user.getRole().name(), user.getFullName())
        );
    }

    // ── Login ─────────────────────────────────────────────────
    @Transactional
    public TokenPair login(LoginRequest request) {

        // Step 1 — Check rate limit BEFORE attempting authentication
        loginRateLimiter.checkRateLimit(request.email());

        try {
            // Step 2 — Authenticate credentials
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.email(), request.password()));

            // Step 3 — Load user
            User user = userRepository.findByEmail(request.email())
                    .orElseThrow();

            // Step 4 — Check account is active
            if (!user.isActive()) {
                throw new BadCredentialsException(
                        "Account is deactivated. Please contact your administrator.");
            }

            // Step 5 — Clear failed attempts
            loginRateLimiter.clearFailedAttempts(request.email());

            var userDetails = org.springframework.security.core.userdetails.User
                    .builder()
                    .username(user.getEmail())
                    .password(user.getPasswordHash())
                    .roles(user.getRole().name())
                    .build();

            // Step 6 — Generate both tokens
            String accessToken = jwtService.generateToken(
                    userDetails, user.getId(), user.getRole().name());
            String refreshToken = refreshTokenService.createRefreshToken(user);

            return new TokenPair(
                    accessToken,
                    refreshToken,
                    AuthResponse.of(user.getEmail(), user.getRole().name(), user.getFullName())
            );

        } catch (BadCredentialsException ex) {
            loginRateLimiter.recordFailedAttempt(request.email());
            throw ex;
        }
    }

    // ── Refresh ───────────────────────────────────────────────
    // Called when access token expires
    // Validates the refresh token, rotates it, issues new access token
    @Transactional
    public TokenPair refresh(String refreshTokenValue) {

        // Step 1 — Verify the refresh token exists and is not expired
        RefreshToken refreshToken = refreshTokenService
                .verifyRefreshToken(refreshTokenValue);

        // Step 2 — Get the user from the token
        User user = refreshToken.getUser();

        // Step 3 — Check user is still active
        if (!user.isActive()) {
            refreshTokenService.deleteAllTokensForUser(user);
            throw new BadCredentialsException(
                    "Account is deactivated. Please contact your administrator.");
        }

        // Step 4 — Build UserDetails for JWT generation
        var userDetails = org.springframework.security.core.userdetails.User
                .builder()
                .username(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getRole().name())
                .build();

        // Step 5 — Generate new access token
        String newAccessToken = jwtService.generateToken(
                userDetails, user.getId(), user.getRole().name());

        // Step 6 — Rotate refresh token (delete old, create new)
        String newRefreshToken = refreshTokenService.rotateRefreshToken(refreshToken);

        return new TokenPair(
                newAccessToken,
                newRefreshToken,
                AuthResponse.of(user.getEmail(), user.getRole().name(), user.getFullName())
        );
    }

    // ── Logout ────────────────────────────────────────────────
    // Deletes all refresh tokens for the user
    // Called by controller which also clears both cookies
    // ── Logout ────────────────────────────────────────────────
    @Transactional
    public void logout(String refreshTokenValue) {
        if (refreshTokenValue == null || refreshTokenValue.isBlank()) return;

        refreshTokenRepository
                .findByToken(refreshTokenValue)
                .ifPresent(rt -> refreshTokenService.deleteAllTokensForUser(rt.getUser()));
    }

    // Helper — finds user from refresh token and deletes all their tokens
    private void refreshTokenRepository(String refreshTokenValue) {
        refreshTokenService
                .verifyRefreshToken(refreshTokenValue);
    }
}