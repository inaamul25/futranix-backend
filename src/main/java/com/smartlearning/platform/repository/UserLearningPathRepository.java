package com.smartlearning.platform.repository;

import com.smartlearning.platform.entity.UserLearningPath;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserLearningPathRepository extends JpaRepository<UserLearningPath, Long> {
    boolean existsByUser_IdAndLearningPath_Id(Long userId, Long learningPathId);
    List<UserLearningPath> findByUser_Id(Long userId);
}
