package com.academy.project.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginRequest {

    /** Email or phone - whichever the user registered with. */
    @NotBlank(message = "Email or phone is required")
    private String identifier;

    @NotBlank(message = "Password is required")
    private String password;

    /** Optional, purely informational - stored on the session row for the user's "active devices" view. */
    private String deviceInfo;
}
