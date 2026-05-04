package com.smartlearning.platform.dto.course;

import java.util.List;

public record ModuleResponse(
        Long id,
        String title,
        Integer sortOrder,
        boolean completed,
        List<LessonResponse> lessons
) {
}
