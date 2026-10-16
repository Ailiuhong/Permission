package com.liu.permission.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.liu.permission.entity.SysRole;
import com.liu.permission.mapper.SysPermissionMapper;
import com.liu.permission.mapper.SysRoleMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * 用户权限 / 角色的 Redis 缓存。
 * 设计要点：权限判定是每次接口调用都要走的热路径，不能每次都查三张表；角色或权限变更后立即失效。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionCacheService {

    private static final String PERM_KEY = "perm:user:";
    private static final String ROLE_KEY = "role:user:";

    private final StringRedisTemplate redisTemplate;
    private final SysPermissionMapper permissionMapper;
    private final SysRoleMapper roleMapper;
    /** 缓存里会存 SysRole（带 LocalDateTime），必须注册 JavaTimeModule，否则反序列化会失败 */
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .addModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .build();

    @Value("${permission.cache-seconds:1800}")
    private long cacheSeconds;

    public Set<String> getPermissionCodes(Long userId) {
        String json = redisTemplate.opsForValue().get(PERM_KEY + userId);
        if (json != null) {
            try {
                return objectMapper.readValue(json, new TypeReference<Set<String>>() {
                });
            } catch (Exception e) {
                log.warn("权限缓存解析失败，回源数据库，userId={}", userId, e);
            }
        }
        Set<String> codes = new HashSet<>(permissionMapper.selectCodesByUserId(userId));
        cacheValue(PERM_KEY + userId, codes);
        return codes;
    }

    public List<SysRole> getRoles(Long userId) {
        String json = redisTemplate.opsForValue().get(ROLE_KEY + userId);
        if (json != null) {
            try {
                return objectMapper.readValue(json, new TypeReference<List<SysRole>>() {
                });
            } catch (Exception e) {
                log.warn("角色缓存解析失败，回源数据库，userId={}", userId, e);
            }
        }
        List<SysRole> roles = roleMapper.selectRolesByUserId(userId);
        cacheValue(ROLE_KEY + userId, roles);
        return roles;
    }

    /** 角色授权变更后调用，清理该用户缓存 */
    public void evictUser(Long userId) {
        redisTemplate.delete(List.of(PERM_KEY + userId, ROLE_KEY + userId));
    }

    /** 权限点本身变更后调用（影响面是全局），演示项目直接按前缀清理 */
    public void evictAll() {
        Set<String> keys = new HashSet<>();
        keys.addAll(nullSafeKeys(PERM_KEY + "*"));
        keys.addAll(nullSafeKeys(ROLE_KEY + "*"));
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("已清理权限缓存 {} 个", keys.size());
        }
    }

    private Set<String> nullSafeKeys(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        return keys == null ? Set.of() : keys;
    }

    private void cacheValue(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(value), Duration.ofSeconds(cacheSeconds));
        } catch (Exception e) {
            log.warn("写权限缓存失败，key={}", key, e);
        }
    }
}
