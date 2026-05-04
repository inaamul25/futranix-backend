package com.smartlearning.platform.dto.progress;

public record CourseProgressCard(
        Long courseId,
        String title,
        String category,
        String thumbnailUrl,
        double completionPercentage
) {
}
