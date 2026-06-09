package com.homecloud.auth.service.impl;

import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.StpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.homecloud.auth.dto.*;
import com.homecloud.auth.entity.SysUser;
import com.homecloud.auth.mapper.SysUserMapper;
import com.homecloud.auth.service.AuthService;
import com.homecloud.common.constant.ErrorCode;
import com.homecloud.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper sysUserMapper;

    @Override
    public void register(RegisterRequest request) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        if (sysUserMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.USERNAME_EXISTS);
        }
        SysUser user = new SysUser();
        user.setUsername(request.getUsername());
        user.setPassword(BCrypt.hashpw(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setStorageLimit(53687091200L);
        sysUserMapper.insert(user);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getUsername, request.getUsername());
        SysUser user = sysUserMapper.selectOne(wrapper);
        if (user == null || !BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        if (user.getStatus() == 0) {
            throw new BusinessException(ErrorCode.USER_DISABLED);
        }
        StpUtil.login(user.getId());
        String accessToken = StpUtil.getTokenValue();
        UserInfo userInfo = UserInfo.builder()
                .id(user.getId()).username(user.getUsername())
                .nickname(user.getNickname()).avatar(user.getAvatar())
                .build();
        return TokenResponse.builder()
                .accessToken(accessToken)
                .refreshToken(accessToken)
                .expiresIn(900)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        StpUtil.checkLogin();
        String newToken = StpUtil.getTokenValue();
        Long userId = StpUtil.getLoginIdAsLong();
        SysUser user = sysUserMapper.selectById(userId);
        UserInfo userInfo = UserInfo.builder()
                .id(user.getId()).username(user.getUsername())
                .nickname(user.getNickname()).avatar(user.getAvatar())
                .build();
        return TokenResponse.builder()
                .accessToken(newToken)
                .refreshToken(newToken)
                .expiresIn(900)
                .userInfo(userInfo)
                .build();
    }

    @Override
    public void logout() {
        StpUtil.logout();
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || !BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }
        user.setPassword(BCrypt.hashpw(newPassword));
        sysUserMapper.updateById(user);
    }
}
