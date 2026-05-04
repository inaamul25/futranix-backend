package com.smartlearning.platform.dto.learningpath;

import com.smartlearning.platform.dto.course.CourseResponse;

import java.util.List;

public record LearningPathResponse(
        Long id,
        String title,
        String description,
        List<CourseResponse> courses,
        boolean followed
) {
}
