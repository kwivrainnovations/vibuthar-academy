package com.academy.project.security;

import org.springframework.security.core.Authentication;

public final class SecurityUtils {

    private SecurityUtils() {
    }

    /**
     * JwtAuthFilter stores the public user id (e.g. STU000001) as the JWT subject;
     * form login uses UserPrincipal.
     */
    public static String resolveUserId(Authentication authentication) {
        if (authentication == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal userPrincipal) {
            return userPrincipal.getUserId();
        }
        if (principal instanceof String userId) {
            return userId.isBlank() ? null : userId;
        }
        return null;
    }
}
