package com.smartlearning.platform.controller;

import com.smartlearning.platform.exception.UnauthorizedException;
import com.smartlearning.platform.service.LessonStreamingService;
import com.smartlearning.platform.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class LessonController {

    private final LessonStreamingService lessonStreamingService;
    private final StorageService storageService;

    @GetMapping("/lessons/{lessonId}/stream")
    public ResponseEntity<Resource> streamLesson(
            @PathVariable Long lessonId,
            @RequestHeader(value = "Range", required = false) String rangeHeader,
            Authentication authentication) {
        return lessonStreamingService.streamLesson(lessonId, rangeHeader, authentication.getName());
    }

    @GetMapping("/files")
    public ResponseEntity<Resource> file(@RequestParam String path) {
        if (!path.startsWith("thumbnails/") && !path.startsWith("profile-images/")) {
            throw new UnauthorizedException("Direct access is allowed only for public thumbnail and profile image assets");
        }
        return ResponseEntity.ok(storageService.loadAsResource(path));
    }

    @PatchMapping("/modules/{moduleId}")
    public ResponseEntity<Void> modulePlaceholder(@PathVariable Long moduleId) {
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/lessons/{lessonId}")
    public ResponseEntity<Void> lessonPlaceholder(@PathVariable Long lessonId) {
        return ResponseEntity.noContent().build();
    }
}
