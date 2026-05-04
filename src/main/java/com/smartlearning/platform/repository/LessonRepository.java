package com.smartlearning.platform.repository;

import com.smartlearning.platform.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Long> {
    List<Lesson> findByModuleIdOrderBySortOrderAsc(Long moduleId);
}
