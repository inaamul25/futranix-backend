package com.smartlearning.platform.service.impl;

import com.smartlearning.platform.dto.profile.ProfileResponse;
import com.smartlearning.platform.dto.profile.ProfileUpdateRequest;
import com.smartlearning.platform.entity.User;
import com.smartlearning.platform.repository.UserRepository;
import com.smartlearning.platform.service.ProfileService;
import com.smartlearning.platform.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final SupportService supportService;
    private final UserRepository userRepository;
    private final StorageService storageService;

    @Override
    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(String userEmail) {
        return toResponse(supportService.getUserByEmail(userEmail));
    }

    @Override
    @Transactional
    public ProfileResponse updateMyProfile(ProfileUpdateRequest request, MultipartFile avatar, String userEmail) {
        User user = supportService.getUserByEmail(userEmail);
        user.setFullName(request.fullName());
        user.setPhone(request.phone());
        user.setAlternatePhone(request.alternatePhone());
        user.setGender(request.gender());
        user.setDateOfBirth(request.dateOfBirth());
        user.setExperience(request.experience());
        user.setCareerGap(request.careerGap());
        user.setCurrentState(request.currentState());
        user.setCurrentCity(request.currentCity());
        user.setPreferredLocation(request.preferredLocation());
        user.setGithubUrl(request.githubUrl());
        user.setLinkedinUrl(request.linkedinUrl());
        user.setResumeUrl(request.resumeUrl());

        if (avatar != null && !avatar.isEmpty()) {
            user.setProfileImagePath(storageService.store(avatar, "profile-images/" + user.getId()));
        }

        return toResponse(userRepository.save(user));
    }

    private ProfileResponse toResponse(User user) {
        Set<String> roles = user.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.toSet());

        return new ProfileResponse(
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getProfileImagePath() == null ? null : storageService.publicUrl(user.getProfileImagePath()),
                user.getPhone(),
                user.getAlternatePhone(),
                user.getGender(),
                user.getDateOfBirth(),
                user.getExperience(),
                user.getCareerGap(),
                user.getCurrentState(),
                user.getCurrentCity(),
                user.getPreferredLocation(),
                user.getGithubUrl(),
                user.getLinkedinUrl(),
                user.getResumeUrl(),
                roles
        );
    }
}
