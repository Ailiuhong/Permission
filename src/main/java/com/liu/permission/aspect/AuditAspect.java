package com.liu.permission.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.liu.permission.annotation.Audit;
import com.liu.permission.entity.SysAuditLog;
import com.liu.permission.mapper.SysAuditLogMapper;
import com.liu.permission.security.LoginUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * 审计日志：记录谁在什么时间对哪个接口做了什么、成功还是失败、耗时多少。
 * 审计失败不能影响主流程，所以这里把异常全部吞掉只打日志。
 */
@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private static final int MAX_PARAM_LENGTH = 1900;
    private static final int MAX_ERROR_LENGTH = 450;

    private final SysAuditLogMapper auditLogMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Around("@annotation(audit)")
    public Object around(ProceedingJoinPoint joinPoint, Audit audit) throws Throwable {
        long start = System.currentTimeMillis();
        SysAuditLog entity = new SysAuditLog();
        entity.setModule(audit.module());
        entity.setOperation(audit.value());
        entity.setMethod(joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());
        entity.setCreateTime(LocalDateTime.now());
        fillUser(entity);
        fillRequest(entity);
        entity.setParams(serializeArgs(joinPoint.getArgs()));

        Object result;
        try {
            result = joinPoint.proceed();
            entity.setStatus(1);
            return result;
        } catch (Throwable e) {
            entity.setStatus(0);
            entity.setErrorMsg(truncate(e.getMessage(), MAX_ERROR_LENGTH));
            throw e;
        } finally {
            entity.setCostTime(System.currentTimeMillis() - start);
            save(entity);
        }
    }

    private void fillUser(SysAuditLog entity) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof LoginUser loginUser) {
            entity.setUserId(loginUser.getUserId());
            entity.setUsername(loginUser.getUsername());
        }
    }

    private void fillRequest(SysAuditLog entity) {
        if (RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes) {
            HttpServletRequest request = attributes.getRequest();
            entity.setRequestUri(request.getRequestURI());
            entity.setRequestMethod(request.getMethod());
            entity.setIp(resolveIp(request));
        }
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private String serializeArgs(Object[] args) {
        try {
            String json = Arrays.stream(args)
                    .filter(arg -> !(arg instanceof HttpServletRequest)
                            && !(arg instanceof MultipartFile)
                            && arg != null)
                    .map(arg -> {
                        try {
                            return objectMapper.writeValueAsString(arg);
                        } catch (Exception e) {
                            return String.valueOf(arg);
                        }
                    })
                    .collect(Collectors.joining(","));
            return truncate(json, MAX_PARAM_LENGTH);
        } catch (Exception e) {
            return null;
        }
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return null;
        }
        return text.length() <= max ? text : text.substring(0, max) + "...";
    }

    private void save(SysAuditLog entity) {
        try {
            auditLogMapper.insert(entity);
        } catch (Exception e) {
            log.warn("写审计日志失败：{}", e.getMessage());
        }
    }
}
