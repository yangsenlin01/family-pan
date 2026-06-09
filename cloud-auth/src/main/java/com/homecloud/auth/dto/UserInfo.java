package com.homecloud.auth.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserInfo {
    private Long id;
    private String username;
    private String nickname;
    private String avatar;
}
