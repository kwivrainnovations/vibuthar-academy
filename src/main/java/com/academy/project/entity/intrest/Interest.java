package com.academy.project.entity.intrest;

import com.academy.project.enums.EmailStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "interests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String username;

    @Column(name = "email_id", nullable = false, length = 255)
    private String emailId;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "course_of_interest", nullable = false, length = 200)
    private String courseOfInterest;

    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "email_status", nullable = false, length = 30)
    private EmailStatus emailStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}