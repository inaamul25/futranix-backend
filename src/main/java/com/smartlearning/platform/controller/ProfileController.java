package com.smartlearning.platform.controller;

import com.smartlearning.platform.dto.profile.ProfileResponse;
import com.smartlearning.platform.dto.profile.ProfileUpdateRequest;
import com.smartlearning.platform.service.ProfileService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final ObjectMapper objectMapper;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> me(Authentication authentication) {
        return ResponseEntity.ok(profileService.getMyProfile(authentication.getName()));
    }

    @PutMapping(value = "/me", consumes = {"multipart/form-data"})
    public ResponseEntity<ProfileResponse> update(
            @RequestPart("profile") String profileJson,
            @RequestPart(value = "avatar", required = false) MultipartFile avatar,
            Authentication authentication
    ) throws Exception {
        ProfileUpdateRequest request = objectMapper.readValue(profileJson, ProfileUpdateRequest.class);
        return ResponseEntity.ok(profileService.updateMyProfile(request, avatar, authentication.getName()));
    }
}
