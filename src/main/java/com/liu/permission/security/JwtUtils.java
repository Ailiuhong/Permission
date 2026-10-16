package com.liu.permission.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * JWT 生成与解析：access 令牌用于接口访问，refresh 令牌用于换取新的 access 令牌
 */
@Component
public class JwtUtils {

    public static final String TYPE_ACCESS = "access";
    public static final String TYPE_REFRESH = "refresh";

    private final JwtProperties properties;
    private final SecretKey secretKey;

    public JwtUtils(JwtProperties properties) {
        this.properties = properties;
        this.secretKey = Keys.hmacShaKeyFor(properties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long userId, String username) {
        return create(userId, username, TYPE_ACCESS, properties.getAccessTokenMinutes() * 60 * 1000);
    }

    public String createRefreshToken(Long userId, String username) {
        return create(userId, username, TYPE_REFRESH, properties.getRefreshTokenDays() * 24 * 60 * 60 * 1000);
    }

    private String create(Long userId, String username, String type, long ttlMillis) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("type", type)
                .issuedAt(new Date(now))
                .expiration(new Date(now + ttlMillis))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 解析并校验签名与过期时间，失败会抛 JwtException（由调用方决定如何处理）
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public long getRefreshTokenSeconds() {
        return properties.getRefreshTokenDays() * 24 * 60 * 60;
    }
}
