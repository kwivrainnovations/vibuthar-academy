package com.academy.project.service.subscription;

import com.academy.project.dto.subscription.CreateSubscriptionRequest;
import com.academy.project.dto.subscription.SubscriptionResponse;
import com.academy.project.dto.subscription.UpdateSubscriptionPaymentRequest;

public interface SubscriptionService {

    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);

    SubscriptionResponse updateSubscriptionPayment(Long subscriptionId, UpdateSubscriptionPaymentRequest request);
}
