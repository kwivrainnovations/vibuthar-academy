package com.academy.project.dto.course;

import com.academy.project.entity.course.Course;
import com.academy.project.entity.course.CourseVideo;
import com.academy.project.enums.CourseStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CourseResponse {

    private Long id;
    private String title;
    private String description;
    private Integer durationHours;
    private BigDecimal price;
    private CourseStatus status;
    private String thumbnailUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<CourseVideoResponse> videos;

    public static CourseResponse fromEntity(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .title(course.getTitle())
                .description(course.getDescription())
                .durationHours(course.getDurationHours())
                .price(course.getPrice())
                .status(course.getStatus())
                .thumbnailUrl(course.getThumbnailUrl())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }

    public static CourseResponse fromEntityWithVideos(Course course, List<CourseVideo> videos) {
        CourseResponse response = fromEntity(course);
        return CourseResponse.builder()
                .id(response.getId())
                .title(response.getTitle())
                .description(response.getDescription())
                .durationHours(response.getDurationHours())
                .price(response.getPrice())
                .status(response.getStatus())
                .thumbnailUrl(response.getThumbnailUrl())
                .createdAt(response.getCreatedAt())
                .updatedAt(response.getUpdatedAt())
                .videos(videos.stream().map(CourseVideoResponse::fromEntity).toList())
                .build();
    }
}
