package com.smartlearning.platform.service;

import com.smartlearning.platform.dto.progress.DashboardResponse;
import com.smartlearning.platform.dto.progress.ProgressUpdateRequest;

public interface ProgressService {
    DashboardResponse getDashboard(String userEmail);
    void updateProgress(ProgressUpdateRequest request, String userEmail);
    double calculateCourseCompletion(Long courseId, Long userId);
}
