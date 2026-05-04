package com.smartlearning.platform.dto.course;

public record LessonResponse(
        Long id,
        String title,
        Integer sortOrder,
        String streamUrl,
        Integer durationSeconds,
        boolean completed,
        Integer lastWatchedSecond
) {
}
