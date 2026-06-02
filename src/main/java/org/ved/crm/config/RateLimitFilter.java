package org.ved.crm.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

// Runs after CorrelationFilter (@Order(1)) but before JwtAuthFilter
// Limits each authenticated user to MAX_REQUESTS per minute
// Uses Redis sliding window — key expires after 1 minute automatically
@Component
@Order(2)
@RequiredArgsConstructor
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;

    // 300 requests per minute per user — generous for legitimate use
    // Blocks bots and scrapers without affecting real users
    private static final int MAX_REQUESTS = 300;
    private static final Duration WINDOW = Duration.ofMinutes(1);

    // Public endpoints — skip rate limiting entirely
    // These are either unauthenticated or health checks
    private static final String[] EXCLUDED_PATHS = {
            "/api/v1/auth/login",
            "/actuator/health",
            "/actuator/info",
            "/actuator/prometheus"
    };

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        String path = request.getRequestURI();

        // Skip rate limiting for public endpoints
        for (String excluded : EXCLUDED_PATHS) {
            if (path.startsWith(excluded)) {
                filterChain.doFilter(request, response);
                return;
            }
        }

        // Get authenticated user from Authorization header
        // We use the JWT token itself as the key — unique per user session
        // Extract JWT from the akuner_jwt cookie — same source as JwtAuthFilter
// Previously read from Authorization header, but auth moved to httpOnly cookies
// Without this fix, all cookie-authenticated requests bypassed rate limiting
        String jwt = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("akuner_jwt".equals(cookie.getName())) {
                    jwt = cookie.getValue();
                    break;
                }
            }
        }

        if (jwt == null) {
            // Unauthenticated request — Spring Security will handle it
            filterChain.doFilter(request, response);
            return;
        }

// Use first 16 chars of JWT as Redis key — unique enough per session, not full token
// Format: rate_limit:{token_prefix}:{minute_window}
        String tokenPrefix = jwt.substring(0, Math.min(16, jwt.length()));
        long currentMinute = System.currentTimeMillis() / 60000;
        String redisKey = "rate_limit:" + tokenPrefix + ":" + currentMinute;

        // Increment counter and set expiry on first request
        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1) {
            redisTemplate.expire(redisKey, WINDOW);
        }

        if (count != null && count > MAX_REQUESTS) {
            log.warn("Rate limit exceeded for key: {} on path: {}", tokenPrefix, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write(
                    "{\"success\":false,\"message\":\"Too many requests. Please slow down.\",\"timestamp\":\""
                            + java.time.Instant.now() + "\"}"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }
}