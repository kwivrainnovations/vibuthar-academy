package com.academy.project.repository.interest;

import com.academy.project.entity.intrest.Interest;
import com.academy.project.enums.EmailStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InterestRepository extends JpaRepository<Interest, Long> {

    @Query("""
            SELECT i FROM Interest i
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(i.username) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(i.emailId) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR i.mobileNumber LIKE CONCAT('%', :search, '%'))
              AND (:courseOfInterest IS NULL OR :courseOfInterest = ''
                   OR LOWER(i.courseOfInterest) LIKE LOWER(CONCAT('%', :courseOfInterest, '%')))
              AND (:emailStatus IS NULL OR i.emailStatus = :emailStatus)
            """)
    Page<Interest> searchInterests(
            @Param("search") String search,
            @Param("courseOfInterest") String courseOfInterest,
            @Param("emailStatus") EmailStatus emailStatus,
            Pageable pageable
    );
}
