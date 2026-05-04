package com.smartlearning.platform.dto.learningpath;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record LearningPathRequest(
        @NotBlank String title,
        @NotBlank String description,
        @NotEmpty List<Long> courseIds
) {
}
