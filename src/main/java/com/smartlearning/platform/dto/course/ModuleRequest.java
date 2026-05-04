package com.smartlearning.platform.dto.course;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record ModuleRequest(
        Long id,
        @NotBlank String title,
        @NotNull @Min(0) Integer sortOrder,
        @Valid List<LessonRequest> lessons
) {
}
