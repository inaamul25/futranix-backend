package com.smartlearning.platform.repository;

import com.smartlearning.platform.entity.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {
    List<CourseModule> findByCourseIdOrderBySortOrderAsc(Long courseId);
}
