package com.liu.permission.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.liu.permission.annotation.Audit;
import com.liu.permission.common.Result;
import com.liu.permission.dto.AssignPermissionsRequest;
import com.liu.permission.dto.RoleRequest;
import com.liu.permission.entity.SysRole;
import com.liu.permission.service.RoleService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/system/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<IPage<SysRole>> page(@RequestParam(defaultValue = "1") long pageNo,
                                       @RequestParam(defaultValue = "10") long pageSize,
                                       @RequestParam(required = false) String name) {
        return Result.ok(roleService.page(pageNo, pageSize, name));
    }

    /** 下拉框数据：登录即可访问（分配角色时需要） */
    @GetMapping("/list")
    public Result<List<SysRole>> list() {
        return Result.ok(roleService.listAll());
    }

    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:list')")
    public Result<List<Long>> permissionIds(@PathVariable Long id) {
        return Result.ok(roleService.listPermissionIds(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:role:add')")
    @Audit(module = "角色管理", value = "新增角色")
    public Result<Long> create(@Valid @RequestBody RoleRequest request) {
        return Result.ok(roleService.create(request));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:role:edit')")
    @Audit(module = "角色管理", value = "修改角色")
    public Result<Void> update(@Valid @RequestBody RoleRequest request) {
        roleService.update(request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:role:delete')")
    @Audit(module = "角色管理", value = "删除角色")
    public Result<Void> delete(@PathVariable Long id) {
        roleService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('system:role:assign')")
    @Audit(module = "角色管理", value = "分配权限")
    public Result<Void> assignPermissions(@PathVariable Long id, @RequestBody AssignPermissionsRequest request) {
        roleService.assignPermissions(id, request.permissionIds());
        return Result.ok();
    }
}
