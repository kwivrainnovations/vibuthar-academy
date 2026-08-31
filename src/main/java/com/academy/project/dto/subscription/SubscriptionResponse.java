package com.academy.project.dto.subscription;

import com.academy.project.entity.course.Course;
import com.academy.project.entity.subscription.CourseSubscription;
import com.academy.project.entity.user.User;
import com.academy.project.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubscriptionResponse {

    private Long id;
    private String studentId;
    private String studentName;
    private String courseId;
    private String courseTitle;
    private SubscriptionStatus status;
    private LocalDateTime subscribedAt;
    private LocalDateTime expiresAt;

    public static SubscriptionResponse from(CourseSubscription subscription, User student, Course course) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .studentId(student.getUserId())
                .studentName(student.getName())
                .courseId(course.getCourseId())
                .courseTitle(course.getTitle())
                .status(subscription.getStatus())
                .subscribedAt(subscription.getSubscribedAt())
                .expiresAt(subscription.getExpiresAt())
                .build();
    }
}
