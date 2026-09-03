package com.academy.project.dto.subscription;

import com.academy.project.entity.course.Course;
import com.academy.project.entity.subscription.CourseSubscription;
import com.academy.project.entity.user.User;
import com.academy.project.enums.PaymentStatus;
import com.academy.project.enums.PaymentType;
import com.academy.project.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
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
    private PaymentType paymentType;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private BigDecimal coursePrice;
    private BigDecimal remainingAmount;
    private LocalDateTime subscribedAt;
    private LocalDateTime expiresAt;

    public static SubscriptionResponse from(CourseSubscription subscription, User student, Course course) {
        BigDecimal paidAmount = subscription.getPaidAmount() != null
                ? subscription.getPaidAmount()
                : BigDecimal.ZERO;
        BigDecimal coursePrice = course.getPrice();
        BigDecimal remainingAmount = coursePrice == null
                ? null
                : coursePrice.subtract(paidAmount).max(BigDecimal.ZERO);

        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .studentId(student.getUserId())
                .studentName(student.getName())
                .courseId(course.getCourseId())
                .courseTitle(course.getTitle())
                .status(subscription.getStatus())
                .paymentType(subscription.getPaymentType())
                .paymentStatus(subscription.getPaymentStatus())
                .amount(paidAmount)
                .coursePrice(coursePrice)
                .remainingAmount(remainingAmount)
                .subscribedAt(subscription.getSubscribedAt())
                .expiresAt(subscription.getExpiresAt())
                .build();
    }
}
