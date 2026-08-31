package com.academy.project.util;

import com.academy.project.entity.user.User;
import com.academy.project.entity.user.UserRole;

public final class UserIdGenerator {

    private UserIdGenerator() {
    }

    public static String generate(User user) {
        String prefix = switch (user.getRole()) {
            case ADMIN -> "ADM";
            case TRAINER -> "TRN";
            case STUDENT -> "STU";
        };
        return prefix + String.format("%06d", user.getId());
    }
}
