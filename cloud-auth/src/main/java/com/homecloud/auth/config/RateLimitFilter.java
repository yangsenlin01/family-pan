package com.homecloud.auth.config;

import com.homecloud.common.constant.ErrorCode;
import com.homecloud.common.exception.BusinessException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter implements Filter {

    private static final int MAX_ATTEMPTS = 20;
    private static final long WINDOW_MS = 60 * 1000; // 1 minute
    private static final long BLOCK_MS = 5 * 60 * 1000; // 5 minute block

    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        String path = request.getRequestURI();

        // Only rate-limit auth endpoints
        if (!path.startsWith("/api/v1/auth/login") && !path.startsWith("/api/v1/auth/register")) {
            chain.doFilter(req, res);
            return;
        }

        String ip = getClientIp(request);
        String key = ip + ":" + path;
        long now = System.currentTimeMillis();
        Attempt attempt = attempts.get(key);

        if (attempt != null && attempt.blockUntil > now) {
            throw new BusinessException(ErrorCode.RATE_LIMITED);
        }

        if (attempt == null || attempt.windowStart + WINDOW_MS < now) {
            attempt = new Attempt(now, 1, 0);
            attempts.put(key, attempt);
        } else {
            attempt.count++;
            if (attempt.count > MAX_ATTEMPTS) {
                attempt.blockUntil = now + BLOCK_MS;
                throw new BusinessException(ErrorCode.RATE_LIMITED);
            }
        }

        chain.doFilter(req, res);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class Attempt {
        long windowStart;
        int count;
        long blockUntil;

        Attempt(long windowStart, int count, long blockUntil) {
            this.windowStart = windowStart;
            this.count = count;
            this.blockUntil = blockUntil;
        }
    }
}
