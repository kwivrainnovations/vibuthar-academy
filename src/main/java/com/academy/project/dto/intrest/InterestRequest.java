package com.academy.project.dto.intrest;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InterestRequest {

    @NotBlank(message = "Username is required")
    @Size(max = 150, message = "Username must not exceed 150 characters")
    private String username;

    @NotBlank(message = "Email ID is required")
    @Email(message = "Invalid email ID")
    private String emailId;

    @Size(max = 5000, message = "Description is too long")
    private String description;

    @NotBlank(message = "Course of interest is required")
    @Size(max = 200, message = "Course name must not exceed 200 characters")
    private String courseOfInterest;

    @NotBlank(message = "Mobile number is required")
    @Pattern(
            regexp = "^[0-9]{10}$",
            message = "Mobile number must contain exactly 10 digits"
    )
    private String mobileNumber;
}
