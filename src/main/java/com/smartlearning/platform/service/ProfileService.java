package com.smartlearning.platform.service;

import com.smartlearning.platform.dto.profile.ProfileResponse;
import com.smartlearning.platform.dto.profile.ProfileUpdateRequest;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {
    ProfileResponse getMyProfile(String userEmail);
    ProfileResponse updateMyProfile(ProfileUpdateRequest request, MultipartFile avatar, String userEmail);
}
