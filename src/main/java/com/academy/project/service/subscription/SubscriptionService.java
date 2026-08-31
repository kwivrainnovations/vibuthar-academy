package com.academy.project.service.subscription;

import com.academy.project.dto.subscription.CreateSubscriptionRequest;
import com.academy.project.dto.subscription.SubscriptionResponse;

public interface SubscriptionService {

    SubscriptionResponse createSubscription(CreateSubscriptionRequest request);
}
