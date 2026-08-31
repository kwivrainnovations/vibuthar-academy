package com.academy.project.util;

import com.academy.project.entity.course.Course;

public final class CourseIdGenerator {

    private CourseIdGenerator() {
    }

    public static String generate(Course course) {
        return "CRS" + String.format("%03d", course.getId());
    }
}
