package com.academy.project.serviceImplementation.member;

import com.academy.project.dto.member.MemberResponse;
import com.academy.project.dto.member.MemberSubscriptionInfo;
import com.academy.project.dto.response.PagedResponse;
import com.academy.project.entity.course.Course;
import com.academy.project.entity.subscription.CourseSubscription;
import com.academy.project.entity.user.User;
import com.academy.project.entity.user.UserRole;
import com.academy.project.enums.SubscriptionStatus;
import com.academy.project.repository.course.CourseRepository;
import com.academy.project.repository.subscription.CourseSubscriptionRepository;
import com.academy.project.repository.user.UserRepository;
import com.academy.project.service.member.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImplementation implements MemberService {

    private final UserRepository userRepository;
    private final CourseSubscriptionRepository subscriptionRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MemberResponse> listSubscribedMembers(String courseId, String search, int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        List<String> subscriberIds = subscriptionRepository.findActiveSubscriberUserIds(
                SubscriptionStatus.ACTIVE, now, courseId
        );

        if (subscriberIds.isEmpty()) {
            return PagedResponse.<MemberResponse>builder()
                    .items(Collections.emptyList())
                    .page(page)
                    .size(size)
                    .totalItems(0)
                    .totalPages(0)
                    .build();
        }

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage = userRepository.findMembersByIds(
                UserRole.STUDENT, subscriberIds, normalizeSearch(search), pageRequest
        );

        Map<String, List<MemberSubscriptionInfo>> subscriptionsByUser =
                buildSubscribedCoursesMap(subscriberIds, now);

        Page<MemberResponse> mapped = userPage.map(user -> {
            List<MemberSubscriptionInfo> subscriptions =
                    subscriptionsByUser.getOrDefault(user.getUserId(), Collections.emptyList());
            List<String> courseTitles = subscriptions.stream()
                    .map(MemberSubscriptionInfo::getCourseTitle)
                    .filter(Objects::nonNull)
                    .toList();
            return MemberResponse.fromUser(user, courseTitles, subscriptions);
        });
        return PagedResponse.from(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MemberResponse> listNonSubscribedMembers(String search, int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        List<String> subscriberIds = subscriptionRepository.findActiveSubscriberUserIds(
                SubscriptionStatus.ACTIVE, now, null
        );

        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<User> userPage;

        if (subscriberIds.isEmpty()) {
            userPage = userRepository.findMembers(UserRole.STUDENT, normalizeSearch(search), pageRequest);
        } else {
            userPage = userRepository.findMembersExcludingIds(
                    UserRole.STUDENT, subscriberIds, normalizeSearch(search), pageRequest
            );
        }

        Page<MemberResponse> mapped = userPage.map(user ->
                MemberResponse.fromUser(user, Collections.emptyList(), Collections.emptyList())
        );
        return PagedResponse.from(mapped);
    }

    private Map<String, List<MemberSubscriptionInfo>> buildSubscribedCoursesMap(
            List<String> userIds,
            LocalDateTime now) {
        Map<String, Course> coursesById = courseRepository.findAll().stream()
                .collect(Collectors.toMap(Course::getCourseId, course -> course));

        return userIds.stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> subscriptionRepository.findActiveSubscriptionsForUser(
                                userId, SubscriptionStatus.ACTIVE, now
                        ).stream()
                                .map(subscription -> toSubscriptionInfo(subscription, coursesById))
                                .filter(Objects::nonNull)
                                .toList()
                ));
    }

    private MemberSubscriptionInfo toSubscriptionInfo(
            CourseSubscription subscription,
            Map<String, Course> coursesById) {
        Course course = coursesById.get(subscription.getCourseId());
        if (course == null) {
            return null;
        }
        BigDecimal paidAmount = subscription.getPaidAmount() != null
                ? subscription.getPaidAmount()
                : BigDecimal.ZERO;
        BigDecimal coursePrice = course.getPrice();
        BigDecimal remainingAmount = coursePrice == null
                ? null
                : coursePrice.subtract(paidAmount).max(BigDecimal.ZERO);

        return MemberSubscriptionInfo.builder()
                .subscriptionId(subscription.getId())
                .courseId(subscription.getCourseId())
                .courseTitle(course.getTitle())
                .status(subscription.getStatus())
                .paymentType(subscription.getPaymentType())
                .paymentStatus(subscription.getPaymentStatus())
                .amount(paidAmount)
                .coursePrice(coursePrice)
                .remainingAmount(remainingAmount)
                .build();
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.trim();
    }
}
