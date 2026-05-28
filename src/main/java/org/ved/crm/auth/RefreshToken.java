package org.ved.crm.auth;

import jakarta.persistence.*;
import lombok.*;
import org.ved.crm.common.audit.BaseAuditEntity;
import org.ved.crm.user.User;

import java.time.Instant;

// ── RefreshToken Entity ───────────────────────────────────────
// Stores refresh tokens in the database so they can be revoked
// Each row represents one active session (one device / one browser)
// When a user logs out → their token row is deleted
// When a token is used → it is rotated (old deleted, new created)
// When a token expires → it is invalid and user must re-login
//
// Extends BaseAuditEntity for:
//   - id (UUID, auto-generated)
//   - createdAt (Instant, auto-set on insert)
//   - updatedAt (Instant, auto-set on update)

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseAuditEntity {

    // The actual token value — a randomly generated UUID string
    // NOT a JWT — refresh tokens are opaque random strings
    // Stored as plain text since they're already random and unguessable
    // unique = true → enforced at DB level, no two sessions share a token
    @Column(nullable = false, unique = true)
    private String token;

    // The user this token belongs to
    // ManyToOne → one user can have many tokens (multiple devices/browsers)
    // FetchType.LAZY → don't load the full User unless explicitly needed
    // ON DELETE CASCADE in SQL → if user deleted, token deleted automatically
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // When this token expires — set to Instant.now() + 7 days on creation
    // After this point, the token is rejected even if it exists in DB
    @Column(nullable = false)
    private Instant expiresAt;
}