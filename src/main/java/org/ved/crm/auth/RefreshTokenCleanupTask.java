package org.ved.crm.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

// ── RefreshTokenCleanupTask ───────────────────────────────────
// Runs automatically every day at midnight
// Deletes expired refresh tokens from the database
// Without this, the refresh_tokens table grows forever
// Each login creates a new token — over months this adds up

@Slf4j
@Component
@RequiredArgsConstructor
public class RefreshTokenCleanupTask {

    private final RefreshTokenRepository refreshTokenRepository;

    // Cron expression: "0 0 0 * * *"
    // Seconds(0) Minutes(0) Hours(0) Day(*) Month(*) DayOfWeek(*)
    // = Every day at midnight (00:00:00)
    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Starting expired refresh token cleanup...");

        refreshTokenRepository.deleteAllExpiredTokens(Instant.now());

        log.info("Expired refresh token cleanup completed");
    }
}