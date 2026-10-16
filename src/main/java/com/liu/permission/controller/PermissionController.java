package com.liu.permission.controller;

import com.liu.permission.annotation.Audit;
import com.liu.permission.common.Result;
import com.liu.permission.dto.PermissionRequest;
import com.liu.permission.entity.SysPermission;
import com.liu.permission.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system/permission")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @GetMapping("/tree")
    @PreAuthorize("hasAuthority('system:permission:list')")
    public Result<List<SysPermission>> tree() {
        return Result.ok(permissionService.tree());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:permission:add')")
    @Audit(module = "权限管理", value = "新增权限")
    public Result<Long> create(@Valid @RequestBody PermissionRequest request) {
        return Result.ok(permissionService.create(request));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:permission:edit')")
    @Audit(module = "权限管理", value = "修改权限")
    public Result<Void> update(@Valid @RequestBody PermissionRequest request) {
        permissionService.update(request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:permission:delete')")
    @Audit(module = "权限管理", value = "删除权限")
    public Result<Void> delete(@PathVariable Long id) {
        permissionService.delete(id);
        return Result.ok();
    }
}
