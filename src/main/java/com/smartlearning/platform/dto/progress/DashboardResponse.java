package com.smartlearning.platform.dto.progress;

import java.util.List;

public record DashboardResponse(
        List<CourseProgressCard> enrolledCourses,
        List<CourseProgressCard> recommendedCourses
) {
}
