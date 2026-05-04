package com.smartlearning.platform.controller;

import com.smartlearning.platform.dto.common.ApiResponse;
import com.smartlearning.platform.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enroll")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/{courseId}")
    public ResponseEntity<ApiResponse> enroll(@PathVariable Long courseId, Authentication authentication) {
        return ResponseEntity.ok(enrollmentService.enroll(courseId, authentication.getName()));
    }
}
