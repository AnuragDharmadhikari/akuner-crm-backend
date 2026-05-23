package org.ved.crm.auth;

// With httpOnly cookie auth, the token is set as a cookie — not returned in the body.
// The response body only needs to confirm success and provide basic user info
// so the frontend can populate its auth state without a separate /me call.
public record AuthResponse(
        String email,
        String role,
        String fullName
) {
    public static AuthResponse of(String email, String role, String fullName) {
        return new AuthResponse(email, role, fullName);
    }
}