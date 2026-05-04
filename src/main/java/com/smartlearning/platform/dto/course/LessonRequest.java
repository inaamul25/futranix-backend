package com.smartlearning.platform.dto.course;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record LessonRequest(
        Long id,
        @NotBlank String title,
        @NotNull @Min(0) Integer sortOrder,
        String videoPath,
        String contentType,
        Long fileSize,
        Integer durationSeconds
) {
}
