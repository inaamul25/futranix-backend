package com.smartlearning.platform.service;

import com.smartlearning.platform.dto.common.ApiResponse;

public interface EnrollmentService {
    ApiResponse enroll(Long courseId, String userEmail);
    boolean isEnrolled(Long courseId, Long userId);
}
