package com.smartlearning.platform.service;

import com.smartlearning.platform.dto.common.ApiResponse;
import com.smartlearning.platform.dto.learningpath.LearningPathRequest;
import com.smartlearning.platform.dto.learningpath.LearningPathResponse;

import java.util.List;

public interface LearningPathService {
    LearningPathResponse create(LearningPathRequest request);
    List<LearningPathResponse> list(String userEmail);
    ApiResponse follow(Long pathId, String userEmail);
}
