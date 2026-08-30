package com.academy.project.repository.course;

import com.academy.project.entity.course.Course;
import com.academy.project.enums.CourseStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {

    Page<Course> findByStatus(CourseStatus status, Pageable pageable);

    Page<Course> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Course> findByStatusAndTitleContainingIgnoreCase(CourseStatus status, String title, Pageable pageable);
}
