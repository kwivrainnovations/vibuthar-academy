package com.academy.project.serviceImplementation.member;

import com.academy.project.dto.member.MemberResponse;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImplementation implements MemberService {

    private final UserRepository userRepository;
    private final CourseSubscriptionRepository subscriptionRepository;
    private final CourseRepository courseRepository;

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MemberResponse> listSubscribedMembers(Long courseId, String search, int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        List<Long> subscriberIds = subscriptionRepository.findActiveSubscriberUserIds(
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

        Map<Long, List<String>> coursesByUser = buildSubscribedCourseNamesMap(subscriberIds, now);

        Page<MemberResponse> mapped = userPage.map(user ->
                MemberResponse.fromUser(user, coursesByUser.getOrDefault(user.getId(), Collections.emptyList()))
        );
        return PagedResponse.from(mapped);
    }

    @Override
    @Transactional(readOnly = true)
    public PagedResponse<MemberResponse> listNonSubscribedMembers(String search, int page, int size) {
        LocalDateTime now = LocalDateTime.now();
        List<Long> subscriberIds = subscriptionRepository.findActiveSubscriberUserIds(
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

        Page<MemberResponse> mapped = userPage.map(user -> MemberResponse.fromUser(user, Collections.emptyList()));
        return PagedResponse.from(mapped);
    }

    private Map<Long, List<String>> buildSubscribedCourseNamesMap(List<Long> userIds, LocalDateTime now) {
        Map<Long, String> courseNames = courseRepository.findAll().stream()
                .collect(Collectors.toMap(Course::getId, Course::getTitle));

        return userIds.stream()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> subscriptionRepository.findActiveSubscriptionsForUser(
                                userId, SubscriptionStatus.ACTIVE, now
                        ).stream()
                                .map(CourseSubscription::getCourseId)
                                .map(courseNames::get)
                                .filter(name -> name != null)
                                .toList()
                ));
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }
        return search.trim();
    }
}
