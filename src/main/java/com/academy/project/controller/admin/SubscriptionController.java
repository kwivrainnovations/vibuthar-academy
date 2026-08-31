package com.academy.project.controller.admin;

import com.academy.project.dto.subscription.CreateSubscriptionRequest;
import com.academy.project.dto.subscription.SubscriptionResponse;
import com.academy.project.dto.response.ApiResponse;
import com.academy.project.service.subscription.SubscriptionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/subscriptions")
@RequiredArgsConstructor
@CrossOrigin
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','TRAINER')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        SubscriptionResponse response = subscriptionService.createSubscription(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Subscription created successfully", response));
    }
}
