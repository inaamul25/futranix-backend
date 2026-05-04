package com.smartlearning.platform.service;

import com.smartlearning.platform.dto.comment.CommentRequest;
import com.smartlearning.platform.dto.comment.CommentResponse;

import java.util.List;

public interface CommentService {
    CommentResponse addComment(CommentRequest request, String userEmail);
    List<CommentResponse> getLessonComments(Long lessonId, String userEmail);
}
