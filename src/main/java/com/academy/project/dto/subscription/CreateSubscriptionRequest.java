package com.academy.project.dto.subscription;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateSubscriptionRequest {

    @NotBlank(message = "Student id is required")
    private String studentId;

    @NotBlank(message = "Course id is required")
    private String courseId;

    /** Optional. Leave null for no expiry. */
    private LocalDateTime expiresAt;
}
