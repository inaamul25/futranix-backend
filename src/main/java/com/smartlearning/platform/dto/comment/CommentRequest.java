package com.smartlearning.platform.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CommentRequest(
        @NotNull Long lessonId,
        @NotBlank String content,
        boolean question
) {
}
