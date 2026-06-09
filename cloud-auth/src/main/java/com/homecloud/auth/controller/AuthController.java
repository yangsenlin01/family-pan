package com.homecloud.auth.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.homecloud.auth.dto.*;
import com.homecloud.auth.service.AuthService;
import com.homecloud.common.result.Result;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Result<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return Result.ok();
    }

    @PostMapping("/login")
    public Result<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    public Result<TokenResponse> refresh(@RequestHeader("Authorization") String token) {
        String rawToken = token.startsWith("Bearer ") ? token.substring(7) : token;
        return Result.ok(authService.refresh(rawToken));
    }

    @PostMapping("/logout")
    public Result<Void> logout() {
        authService.logout();
        return Result.ok();
    }

    @PutMapping("/password")
    public Result<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(StpUtil.getLoginIdAsLong(),
                request.getOldPassword(), request.getNewPassword());
        return Result.ok();
    }
}
