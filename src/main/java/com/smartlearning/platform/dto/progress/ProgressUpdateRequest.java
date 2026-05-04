package com.smartlearning.platform.dto.progress;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ProgressUpdateRequest(
        @NotNull Long lessonId,
        @NotNull Boolean completed,
        @NotNull @Min(0) Integer lastWatchedSecond
) {
}
