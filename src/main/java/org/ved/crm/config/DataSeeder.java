package org.ved.crm.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.ved.crm.user.Role;
import org.ved.crm.user.User;
import org.ved.crm.user.UserRepository;

// ── DataSeeder ────────────────────────────────────────────────
// Runs automatically on every application startup
// Creates the owner account if no OWNER exists in the database
// Idempotent — safe to run multiple times, only acts if needed
// Reads owner credentials from environment variables so they
// are never hardcoded in source code

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${application.owner.email}")
    private String ownerEmail;

    @Value("${application.owner.password}")
    private String ownerPassword;

    @Value("${application.owner.full-name}")
    private String ownerFullName;

    // ── run() ─────────────────────────────────────────────────
    // Called automatically by Spring Boot after startup
    // ApplicationArguments contains command-line args — we don't use them
    @Override
    @Transactional
    public void run(ApplicationArguments args) {

        // Check if any OWNER already exists
        // If yes — skip, don't create a duplicate
        boolean ownerExists = userRepository.existsByRole(Role.OWNER);

        if (ownerExists) {
            log.info("Owner account already exists — skipping seed");
            return;
        }

        // No owner found — create one from environment variables
        log.info("No owner found — creating owner account for: {}", ownerEmail);

        User owner = User.builder()
                .fullName(ownerFullName)
                .email(ownerEmail)
                // Never store plain text password — always bcrypt hash
                .passwordHash(passwordEncoder.encode(ownerPassword))
                .role(Role.OWNER)
                .build();

        userRepository.save(owner);

        log.info("Owner account created successfully for: {}", ownerEmail);
    }
}