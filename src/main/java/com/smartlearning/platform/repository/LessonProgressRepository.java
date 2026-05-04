package com.smartlearning.platform.repository;

import com.smartlearning.platform.entity.LessonProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {
    Optional<LessonProgress> findByUser_IdAndLesson_Id(Long userId, Long lessonId);
    List<LessonProgress> findByUser_Id(Long userId);
}
