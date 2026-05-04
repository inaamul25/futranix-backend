package com.smartlearning.platform.controller;

import com.smartlearning.platform.dto.common.ApiResponse;
import com.smartlearning.platform.dto.learningpath.LearningPathRequest;
import com.smartlearning.platform.dto.learningpath.LearningPathResponse;
import com.smartlearning.platform.service.LearningPathService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning-paths")
@RequiredArgsConstructor
public class LearningPathController {

    private final LearningPathService learningPathService;

    @PostMapping
    public ResponseEntity<LearningPathResponse> create(@Valid @RequestBody LearningPathRequest request) {
        return ResponseEntity.ok(learningPathService.create(request));
    }

    @GetMapping
    public ResponseEntity<List<LearningPathResponse>> list(Authentication authentication) {
        return ResponseEntity.ok(learningPathService.list(authentication != null ? authentication.getName() : null));
    }

    @PostMapping("/{pathId}/follow")
    public ResponseEntity<ApiResponse> follow(@PathVariable Long pathId, Authentication authentication) {
        return ResponseEntity.ok(learningPathService.follow(pathId, authentication.getName()));
    }
}
