package com.academy.project.service.emailService;

import com.academy.project.entity.intrest.Interest;

public interface IntrestEmailService {
    void sendInterestNotification(Interest interest);
}
