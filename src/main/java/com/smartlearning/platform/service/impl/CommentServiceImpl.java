package com.smartlearning.platform.service.impl;

import com.smartlearning.platform.dto.comment.CommentRequest;
import com.smartlearning.platform.dto.comment.CommentResponse;
import com.smartlearning.platform.entity.Comment;
import com.smartlearning.platform.entity.Lesson;
import com.smartlearning.platform.entity.User;
import com.smartlearning.platform.repository.CommentRepository;
import com.smartlearning.platform.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final SupportService supportService;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public CommentResponse addComment(CommentRequest request, String userEmail) {
        User user = supportService.getUserByEmail(userEmail);
        Lesson lesson = supportService.getLesson(request.lessonId());
        Comment comment = new Comment();
        comment.setLesson(lesson);
        comment.setUser(user);
        comment.setContent(request.content());
        comment.setQuestion(request.question());
        commentRepository.save(comment);
        return toResponse(comment);
    }

    @Override
    public List<CommentResponse> getLessonComments(Long lessonId, String userEmail) {
        supportService.getUserByEmail(userEmail);
        return commentRepository.findByLesson_IdOrderByCreatedAtDesc(lessonId).stream().map(this::toResponse).toList();
    }

    private CommentResponse toResponse(Comment comment) {
        return new CommentResponse(
                comment.getId(),
                comment.getContent(),
                comment.isQuestion(),
                comment.getUser().getId(),
                comment.getUser().getFullName(),
                comment.getCreatedAt()
        );
    }
}
