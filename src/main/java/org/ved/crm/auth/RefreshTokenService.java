package org.ved.crm.auth;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.ved.crm.user.User;

import java.time.Instant;
import java.util.UUID;

// ── RefreshTokenService ───────────────────────────────────────
// Manages the full lifecycle of refresh tokens:
//   create → verify → rotate → delete
// All methods are @Transactional because they modify the DB

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    // Reads refresh expiration from application.yml
    // Value: 604800000ms = 7 days
    @Value("${application.jwt.refresh-expiration-ms}")
    private long refreshExpirationMs;

    // ── Create ────────────────────────────────────────────────
    // Generates a new refresh token for a user
    // Called after successful login or registration
    // Returns the token STRING — this gets set as a cookie
    @Transactional
    public String createRefreshToken(User user) {

        RefreshToken refreshToken = RefreshToken.builder()
                // UUID.randomUUID() generates a cryptographically random string
                // This is the token value stored in the cookie
                .token(UUID.randomUUID().toString())
                .user(user)
                // Set expiry to now + 7 days
                .expiresAt(Instant.now().plusMillis(refreshExpirationMs))
                .build();

        refreshTokenRepository.save(refreshToken);
        return refreshToken.getToken();
    }

    // ── Verify ────────────────────────────────────────────────
    // Validates an incoming refresh token string
    // Throws exception if token not found or expired
    // Returns the RefreshToken entity (contains the User)
    @Transactional
    public RefreshToken verifyRefreshToken(String token) {

        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Invalid refresh token — not found"));

        // Check if token has expired
        if (refreshToken.getExpiresAt().isBefore(Instant.now())) {
            // Delete the expired token from DB — cleanup
            refreshTokenRepository.delete(refreshToken);
            throw new IllegalArgumentException(
                    "Refresh token expired — please login again");
        }

        return refreshToken;
    }

    // ── Rotate ────────────────────────────────────────────────
    // Deletes the old token and creates a new one
    // Called after every successful refresh — one-time use tokens
    // Returns the new token string to be set as a cookie
    @Transactional
    public String rotateRefreshToken(RefreshToken oldToken) {
        User user = oldToken.getUser();
        // Delete the old token — it can never be used again
        refreshTokenRepository.delete(oldToken);
        // Create and return a fresh token
        return createRefreshToken(user);
    }

    // ── Delete All For User ───────────────────────────────────
    // Called on logout — invalidates ALL sessions for this user
    // If user logs out on one device, all other devices also logged out
    @Transactional
    public void deleteAllTokensForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    // ── Cleanup Expired Tokens ────────────────────────────────
    // Removes all expired tokens from the DB
    // Should be called periodically — we'll schedule this later
    @Transactional
    public void deleteExpiredTokens() {
        refreshTokenRepository.deleteAllExpiredTokens(Instant.now());
    }
}