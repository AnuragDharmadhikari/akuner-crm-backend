package org.ved.crm.security;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.ved.crm.user.UserRepository;

import java.util.UUID;

// Spring Security Expression helper bean.
// Referenced in @PreAuthorize as @userSecurity.methodName(...)
// The bean name is derived from the class name — "userSecurity"
@Component
@RequiredArgsConstructor
public class UserSecurity {

    private final UserRepository userRepository;

    // Returns true if the user with the given ID has the same email
    // as the currently authenticated user.
    // Used to allow users to update/change-password only on their own profile.
    public boolean isOwner(UUID userId, Authentication authentication) {
        String currentUserEmail = authentication.getName();
        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(currentUserEmail))
                .orElse(false);
    }
}