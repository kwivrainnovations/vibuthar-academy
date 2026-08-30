package com.academy.project.dto.course;

import com.academy.project.entity.course.CourseVideo;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CourseVideoResponse {

    private Long id;
    private Long courseId;
    private String title;
    private String videoUrl;
    private Integer sortOrder;
    private Integer durationMinutes;
    private LocalDateTime createdAt;

    public static CourseVideoResponse fromEntity(CourseVideo video) {
        return CourseVideoResponse.builder()
                .id(video.getId())
                .courseId(video.getCourseId())
                .title(video.getTitle())
                .videoUrl(video.getVideoUrl())
                .sortOrder(video.getSortOrder())
                .durationMinutes(video.getDurationMinutes())
                .createdAt(video.getCreatedAt())
                .build();
    }
}
