package com.smartlearning.platform.dto.profile;

public record ProfileUpdateRequest(
        String fullName,
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
        String resumeUrl
) {
}
