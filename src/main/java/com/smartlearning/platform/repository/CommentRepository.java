package com.smartlearning.platform.repository;

import com.smartlearning.platform.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByLesson_IdOrderByCreatedAtDesc(Long lessonId);
}
