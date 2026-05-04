package com.smartlearning.platform.service.impl;

import com.smartlearning.platform.config.AppProperties;
import com.smartlearning.platform.dto.auth.*;
import com.smartlearning.platform.dto.common.ApiResponse;
import com.smartlearning.platform.entity.PasswordResetToken;
import com.smartlearning.platform.entity.Role;
import com.smartlearning.platform.entity.User;
import com.smartlearning.platform.entity.enums.RoleType;
import com.smartlearning.platform.exception.BadRequestException;
import com.smartlearning.platform.repository.PasswordResetTokenRepository;
import com.smartlearning.platform.repository.RoleRepository;
import com.smartlearning.platform.repository.UserRepository;
import com.smartlearning.platform.security.JwtService;
import com.smartlearning.platform.security.UserPrincipal;
import com.smartlearning.platform.service.AuthService;
import com.smartlearning.platform.service.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final StorageService storageService;
    private final JavaMailSender mailSender;
    private final AppProperties appProperties;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BadRequestException("Email is already in use");
        }
        RoleType requestedRole = request.role() == RoleType.ADMIN ? RoleType.STUDENT : request.role();
        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new BadRequestException("Role not configured"));

        User user = new User();
        user.setFullName(request.fullName());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRoles(Set.of(role));
        userRepository.save(user);
        return toAuthResponse(user);
    }

    @Override
    public AuthResponse login(AuthRequest request) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("Invalid credentials"));
        return toAuthResponse(user);
    }

    @Override
    @Transactional
    public ApiResponse forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BadRequestException("No user found for email"));

        resetTokenRepository.deleteByUser_Id(user.getId());
        PasswordResetToken token = new PasswordResetToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiresAt(LocalDateTime.now().plusMinutes(appProperties.getPasswordReset().getExpiryMinutes()));
        resetTokenRepository.save(token);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setSubject("Reset your Smart Modular Learning Platform password");
        message.setText("Use this token to reset your password: " + token.getToken());
        mailSender.send(message);

        return new ApiResponse(true, "Password reset token sent to email");
    }

    @Override
    @Transactional
    public ApiResponse resetPassword(ResetPasswordRequest request) {
        PasswordResetToken token = resetTokenRepository.findByToken(request.token())
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));
        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token expired");
        }
        token.getUser().setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(token.getUser());
        resetTokenRepository.delete(token);
        return new ApiResponse(true, "Password reset successful");
    }

    private AuthResponse toAuthResponse(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        return new AuthResponse(
                jwtService.generateToken(principal),
                user.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getProfileImagePath() == null ? null : storageService.publicUrl(user.getProfileImagePath()),
                user.getRoles().stream().map(role -> role.getName().name()).collect(Collectors.toSet())
        );
    }
}
