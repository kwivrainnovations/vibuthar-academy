package com.academy.project.dto.course;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddCourseVideoRequest {

    @NotBlank(message = "Video title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Video URL is required")
    @Size(max = 500, message = "Video URL must not exceed 500 characters")
    private String videoUrl;

    private Integer sortOrder;

    private Integer durationMinutes;
}
