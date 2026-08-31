package com.academy.project.dto.response;

import com.academy.project.entity.course.Course;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class SubscribedCourseResponse {

    private String courseId;
    private String title;
    private LocalDateTime subscribedAt;

    public static SubscribedCourseResponse from(Course course, LocalDateTime subscribedAt) {
        return SubscribedCourseResponse.builder()
                .courseId(course.getCourseId())
                .title(course.getTitle())
                .subscribedAt(subscribedAt)
                .build();
    }
}
