package com.homecloud.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TokenResponse {
    private String accessToken;
    private String refreshToken;
    private long expiresIn;
    private UserInfo userInfo;
}
