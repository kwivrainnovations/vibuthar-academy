package com.academy.project.serviceImplementation.subscription;

import com.academy.project.dto.subscription.CreateSubscriptionRequest;
import com.academy.project.dto.subscription.SubscriptionResponse;
import com.academy.project.entity.course.Course;
import com.academy.project.entity.subscription.CourseSubscription;
import com.academy.project.entity.user.User;
import com.academy.project.entity.user.UserRole;
import com.academy.project.enums.SubscriptionStatus;
import com.academy.project.exception.ApiException;
import com.academy.project.repository.course.CourseRepository;
import com.academy.project.repository.subscription.CourseSubscriptionRepository;
import com.academy.project.repository.user.UserRepository;
import com.academy.project.service.subscription.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImplementation implements SubscriptionService {

    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseSubscriptionRepository courseSubscriptionRepository;

    @Override
    @Transactional
    public SubscriptionResponse createSubscription(CreateSubscriptionRequest request) {
        User student = userRepository.findByUserId(request.getStudentId())
                .filter(user -> user.getDeletedAt() == null)
                .orElseThrow(() -> ApiException.notFound("Student not found"));

        if (student.getRole() != UserRole.STUDENT) {
            throw ApiException.badRequest("Subscriptions can only be added for students");
        }

        if (!student.isActive()) {
            throw ApiException.badRequest("Student account is not active");
        }

        Course course = courseRepository.findByCourseId(request.getCourseId())
                .orElseThrow(() -> ApiException.notFound("Course not found"));

        LocalDateTime now = LocalDateTime.now();
        List<CourseSubscription> activeSubscriptions = courseSubscriptionRepository
                .findActiveSubscriptionsForUser(student.getUserId(), SubscriptionStatus.ACTIVE, now);

        boolean alreadySubscribed = activeSubscriptions.stream()
                .anyMatch(sub -> sub.getCourseId().equals(course.getCourseId()));

        if (alreadySubscribed) {
            throw ApiException.conflict("Student is already subscribed to this course");
        }

        if (request.getExpiresAt() != null && !request.getExpiresAt().isAfter(now)) {
            throw ApiException.badRequest("Expiry date must be in the future");
        }

        CourseSubscription subscription = CourseSubscription.builder()
                .userId(student.getUserId())
                .courseId(course.getCourseId())
                .status(SubscriptionStatus.ACTIVE)
                .subscribedAt(now)
                .expiresAt(request.getExpiresAt())
                .build();

        CourseSubscription saved = courseSubscriptionRepository.save(subscription);
        return SubscriptionResponse.from(saved, student, course);
    }
}
