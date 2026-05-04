package com.smartlearning.platform.dto.auth;

import java.util.Set;

public record AuthResponse(
        String token,
        Long userId,
        String fullName,
        String email,
        String profileImageUrl,
        Set<String> roles
) {
}
