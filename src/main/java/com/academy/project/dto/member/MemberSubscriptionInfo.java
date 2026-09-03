package com.academy.project.dto.member;

import com.academy.project.enums.PaymentStatus;
import com.academy.project.enums.PaymentType;
import com.academy.project.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class MemberSubscriptionInfo {

    private Long subscriptionId;
    private String courseId;
    private String courseTitle;
    private SubscriptionStatus status;
    private PaymentType paymentType;
    private PaymentStatus paymentStatus;
    private BigDecimal amount;
    private BigDecimal coursePrice;
    private BigDecimal remainingAmount;
}
