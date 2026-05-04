package com.smartlearning.platform.repository;

import com.smartlearning.platform.entity.LearningPath;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LearningPathRepository extends JpaRepository<LearningPath, Long> {
}
