package com.academy.project.dto.intrest;

import com.academy.project.enums.EmailStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InterestResponse {

    private Long id;
    private String username;
    private String emailId;
    private String courseOfInterest;
    private EmailStatus emailStatus;
    private LocalDateTime createdAt;
}