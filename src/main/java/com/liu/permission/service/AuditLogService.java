package com.liu.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liu.permission.entity.SysAuditLog;
import com.liu.permission.mapper.SysAuditLogMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final SysAuditLogMapper auditLogMapper;

    public IPage<SysAuditLog> page(long pageNo, long pageSize, String username, String module) {
        LambdaQueryWrapper<SysAuditLog> wrapper = new LambdaQueryWrapper<SysAuditLog>()
                .like(StringUtils.hasText(username), SysAuditLog::getUsername, username)
                .like(StringUtils.hasText(module), SysAuditLog::getModule, module)
                .orderByDesc(SysAuditLog::getId);
        return auditLogMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    }
}
