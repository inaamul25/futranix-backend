package com.smartlearning.platform.controller;

import com.smartlearning.platform.dto.progress.DashboardResponse;
import com.smartlearning.platform.dto.progress.ProgressUpdateRequest;
import com.smartlearning.platform.service.ProgressService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress")
@RequiredArgsConstructor
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> dashboard(Authentication authentication) {
        return ResponseEntity.ok(progressService.getDashboard(authentication.getName()));
    }

    @PostMapping
    public ResponseEntity<Void> update(@Valid @RequestBody ProgressUpdateRequest request, Authentication authentication) {
        progressService.updateProgress(request, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
