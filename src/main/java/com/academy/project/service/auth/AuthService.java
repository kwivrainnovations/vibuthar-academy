package com.academy.project.service.auth;

import com.academy.project.dto.auth.AuthResponse;
import com.academy.project.dto.auth.LoginRequest;
import com.academy.project.dto.auth.RefreshTokenRequest;
import com.academy.project.dto.register.RegisterRequest;

public interface AuthService {

    AuthResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request, String ipAddress);

    AuthResponse refresh(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}
