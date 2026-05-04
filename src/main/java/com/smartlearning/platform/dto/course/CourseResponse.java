package com.smartlearning.platform.dto.course;

import java.math.BigDecimal;
import java.util.List;

public record CourseResponse(
        Long id,
        String title,
        String description,
        BigDecimal price,
        String thumbnailUrl,
        String category,
        Long creatorId,
        String creatorName,
        List<ModuleResponse> modules,
        boolean enrolled,
        double completionPercentage
) {
}
