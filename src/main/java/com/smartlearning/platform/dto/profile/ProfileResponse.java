package com.smartlearning.platform.dto.profile;

import java.util.Set;

public record ProfileResponse(
        Long userId,
        String fullName,
        String email,
        String profileImageUrl,
        String phone,
        String alternatePhone,
        String gender,
        String dateOfBirth,
        String experience,
        Integer careerGap,
        String currentState,
        String currentCity,
        String preferredLocation,
        String githubUrl,
        String linkedinUrl,
        String resumeUrl,
        Set<String> roles
) {
}
