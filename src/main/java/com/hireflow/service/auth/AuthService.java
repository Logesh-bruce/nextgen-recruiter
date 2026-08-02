package com.hireflow.service.auth;

import com.hireflow.dto.request.LoginRequest;
import com.hireflow.dto.request.RefreshTokenRequest;
import com.hireflow.dto.request.RegisterRequest;
import com.hireflow.dto.response.AuthResponse;
import com.hireflow.dto.response.UserResponse;

/**
 * Service contract for authentication workflows.
 */
public interface AuthService {

    UserResponse register(RegisterRequest request);

    AuthResponse login(LoginRequest request);

    AuthResponse refreshToken(RefreshTokenRequest request);

    void logout(RefreshTokenRequest request);
}
