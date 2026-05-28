package org.ved.crm.auth;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.ved.crm.user.User;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    // Find a token by its string value
    // Used on every refresh request to validate the incoming token
    Optional<RefreshToken> findByToken(String token);

    // Delete all tokens for a specific user
    // Called on logout — invalidates all sessions across all devices
    void deleteByUser(User user);

    // Delete all expired tokens from the database
    // Called periodically to keep the table clean
    // @Modifying — required for DELETE/UPDATE queries in Spring Data
    // @Query — custom JPQL since Spring can't auto-generate this
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt < :now")
    void deleteAllExpiredTokens(@Param("now") Instant now);
}