package com.academy.project.controller.auth;

import com.academy.project.dto.response.ApiResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Proves the role-based wiring end to end. Real feature controllers (courses, videos,
 * payments, etc. in later phases) follow the exact same @PreAuthorize pattern -
 * this class exists purely to demonstrate and smoke-test Phase 1.
 *
 * Two ways to gate a route are shown on purpose:
 *  - URL-pattern rules in SecurityConfig (coarse, applies to a whole path prefix)
 *  - @PreAuthorize on the method (fine-grained, can combine roles/expressions per endpoint)
 */
@RestController
public class DemoRoleController {

    @GetMapping("/api/admin/ping")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<String> adminPing(Authentication auth) {
        return ApiResponse.ok("pong (admin-only)", "user id: " + auth.getPrincipal());
    }

    @GetMapping("/api/trainer/ping")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ApiResponse<String> trainerPing(Authentication auth) {
        return ApiResponse.ok("pong (trainer+)", "user id: " + auth.getPrincipal());
    }

    @GetMapping("/api/student/ping")
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER','STUDENT')")
    public ApiResponse<String> studentPing(Authentication auth) {
        return ApiResponse.ok("pong (any authenticated user)", "user id: " + auth.getPrincipal());
    }
}
