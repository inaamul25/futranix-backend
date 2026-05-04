package com.smartlearning.platform.repository;

import com.smartlearning.platform.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    Optional<Enrollment> findByUser_IdAndCourse_Id(Long userId, Long courseId);
    List<Enrollment> findByUser_Id(Long userId);
    boolean existsByUser_IdAndCourse_Id(Long userId, Long courseId);
}
