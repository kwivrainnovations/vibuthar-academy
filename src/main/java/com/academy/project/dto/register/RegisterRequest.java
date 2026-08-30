package com.academy.project.dto.register;

import com.academy.project.entity.user.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Public self-registration is for students only.
 * Admin and Trainer accounts are provisioned separately (seed data / admin-only endpoint),
 * never through this open endpoint - see README "Role provisioning" note.
 */
@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150)
    private String name;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Must be a valid phone number")
    private String phone;

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be at least 8 characters")
    private String password;

    private UserRole role;

}
