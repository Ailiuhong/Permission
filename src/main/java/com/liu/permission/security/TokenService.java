package com.liu.permission.security;

import com.liu.permission.common.BizException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * 登录 / 刷新 / 登出。
 * access 令牌无状态（不落 Redis），refresh 令牌落 Redis，登出即删除，避免刷新令牌被反复使用。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {

    private static final String REFRESH_KEY = "auth:refresh:";

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final StringRedisTemplate redisTemplate;
    private final UserDetailsServiceImpl userDetailsService;

    public Map<String, Object> login(String username, String password) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        return issueTokens(loginUser);
    }

    public Map<String, Object> refresh(String refreshToken) {
        String key = REFRESH_KEY + refreshToken;
        if (Boolean.FALSE.equals(redisTemplate.hasKey(key))) {
            throw new BizException(401, "刷新令牌已失效，请重新登录");
        }
        Claims claims;
        try {
            claims = jwtUtils.parse(refreshToken);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BizException(401, "刷新令牌无效");
        }
        if (!JwtUtils.TYPE_REFRESH.equals(claims.get("type", String.class))) {
            throw new BizException(401, "令牌类型错误");
        }
        redisTemplate.delete(key);
        LoginUser loginUser = userDetailsService.loadUserById(jwtUtils.getUserId(claims));
        return issueTokens(loginUser);
    }

    public void logout(String refreshToken) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            redisTemplate.delete(REFRESH_KEY + refreshToken);
        }
    }

    private Map<String, Object> issueTokens(LoginUser loginUser) {
        String accessToken = jwtUtils.createAccessToken(loginUser.getUserId(), loginUser.getUsername());
        String refreshToken = jwtUtils.createRefreshToken(loginUser.getUserId(), loginUser.getUsername());
        redisTemplate.opsForValue().set(REFRESH_KEY + refreshToken, String.valueOf(loginUser.getUserId()),
                Duration.ofSeconds(jwtUtils.getRefreshTokenSeconds()));

        Map<String, Object> result = new HashMap<>();
        result.put("accessToken", accessToken);
        result.put("refreshToken", refreshToken);
        result.put("userId", loginUser.getUserId());
        result.put("username", loginUser.getUsername());
        result.put("nickname", loginUser.getNickname());
        return result;
    }
}
