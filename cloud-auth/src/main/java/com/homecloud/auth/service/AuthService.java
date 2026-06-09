package com.homecloud.auth.service;

import com.homecloud.auth.dto.*;

public interface AuthService {
    void register(RegisterRequest request);
    TokenResponse login(LoginRequest request);
    TokenResponse refresh(String refreshToken);
    void logout();
    void changePassword(Long userId, String oldPassword, String newPassword);
}
