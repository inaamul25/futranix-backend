package com.smartlearning.platform.dto.comment;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        boolean question,
        Long userId,
        String userName,
        LocalDateTime createdAt
) {
}
