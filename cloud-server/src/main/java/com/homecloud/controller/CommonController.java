package com.homecloud.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.homecloud.common.result.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/common")
public class CommonController {

    @GetMapping("/health")
    public Result<Map<String, String>> health() {
        return Result.ok(Map.of("status", "UP"));
    }

    @GetMapping("/storage")
    public Result<Map<String, Object>> storage() {
        Long userId = StpUtil.getLoginIdAsLong();
        return Result.ok(Map.of(
            "used", 0L,
            "limit", 53687091200L,
            "unit", "bytes"
        ));
    }
}
