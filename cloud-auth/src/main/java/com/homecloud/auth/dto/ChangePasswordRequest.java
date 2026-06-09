package com.homecloud.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequest {
    @NotBlank
    private String oldPassword;

    @NotBlank
    @Size(min = 8, message = "新密码至少8位")
    private String newPassword;
}
