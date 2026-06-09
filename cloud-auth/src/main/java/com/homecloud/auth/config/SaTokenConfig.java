package com.homecloud.auth.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter
                .match("/api/**")
                .notMatch("/api/v1/auth/register",
                          "/api/v1/auth/login",
                          "/api/v1/auth/refresh",
                          "/api/v1/common/health")
                .check(r -> StpUtil.checkLogin());
        })).addPathPatterns("/api/**");
    }
}
