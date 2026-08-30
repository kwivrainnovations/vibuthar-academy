package com.academy.project.security;

import org.springframework.security.core.Authentication;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * JwtAuthFilter stores the JWT subject as a Long user id; form login uses UserPrincipal.
     */
    public static Long resolveUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getId();
        }
        if (principal instanceof Long userId) {
            return userId;
        }
        if (principal instanceof String subject) {
            try {
                return Long.parseLong(subject);
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }
}
