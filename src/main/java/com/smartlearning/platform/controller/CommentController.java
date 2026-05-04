package com.smartlearning.platform.controller;

import com.smartlearning.platform.dto.comment.CommentRequest;
import com.smartlearning.platform.dto.comment.CommentResponse;
import com.smartlearning.platform.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<CommentResponse> addComment(@Valid @RequestBody CommentRequest request, Authentication authentication) {
        return ResponseEntity.ok(commentService.addComment(request, authentication.getName()));
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<CommentResponse>> getComments(@PathVariable Long lessonId, Authentication authentication) {
        return ResponseEntity.ok(commentService.getLessonComments(lessonId, authentication.getName()));
    }
}
