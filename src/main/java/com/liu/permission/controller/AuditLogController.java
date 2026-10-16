package com.liu.permission.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.liu.permission.common.Result;
import com.liu.permission.entity.SysAuditLog;
import com.liu.permission.service.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/system/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogService auditLogService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:audit:list')")
    public Result<IPage<SysAuditLog>> page(@RequestParam(defaultValue = "1") long pageNo,
                                           @RequestParam(defaultValue = "10") long pageSize,
                                           @RequestParam(required = false) String username,
                                           @RequestParam(required = false) String module) {
        return Result.ok(auditLogService.page(pageNo, pageSize, username, module));
    }
}
