package com.smartlearning.platform.service;

import com.smartlearning.platform.dto.auth.*;
import com.smartlearning.platform.dto.common.ApiResponse;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(AuthRequest request);
    ApiResponse forgotPassword(ForgotPasswordRequest request);
    ApiResponse resetPassword(ResetPasswordRequest request);
}
