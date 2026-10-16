package com.liu.permission.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.liu.permission.annotation.Audit;
import com.liu.permission.common.Result;
import com.liu.permission.dto.AssignRolesRequest;
import com.liu.permission.dto.ResetPasswordRequest;
import com.liu.permission.dto.UserRequest;
import com.liu.permission.entity.SysUser;
import com.liu.permission.service.UserService;
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
import java.util.Map;

/**
 * 用户管理：每个接口都用权限点校验，这就是"接口权限"层。
 * 即使前端把按钮藏了，直接调接口依然会被 @PreAuthorize 拦下来。
 */
@RestController
@RequestMapping("/system/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/page")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<IPage<SysUser>> page(@RequestParam(defaultValue = "1") long pageNo,
                                       @RequestParam(defaultValue = "10") long pageSize,
                                       @RequestParam(required = false) String username,
                                       @RequestParam(required = false) String nickname,
                                       @RequestParam(required = false) Long orgId) {
        return Result.ok(userService.page(pageNo, pageSize, username, nickname, orgId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<SysUser> detail(@PathVariable Long id) {
        return Result.ok(userService.getById(id));
    }

    @GetMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<List<Long>> roleIds(@PathVariable Long id) {
        return Result.ok(userService.listRoleIds(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('system:user:add')")
    @Audit(module = "用户管理", value = "新增用户")
    public Result<Long> create(@Valid @RequestBody UserRequest request) {
        return Result.ok(userService.create(request));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Audit(module = "用户管理", value = "修改用户")
    public Result<Void> update(@Valid @RequestBody UserRequest request) {
        userService.update(request);
        return Result.ok();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('system:user:delete')")
    @Audit(module = "用户管理", value = "删除用户")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.ok();
    }

    @PutMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('system:user:assign')")
    @Audit(module = "用户管理", value = "分配角色")
    public Result<Void> assignRoles(@PathVariable Long id, @RequestBody AssignRolesRequest request) {
        userService.assignRoles(id, request.roleIds());
        return Result.ok();
    }

    @PutMapping("/{id}/password")
    @PreAuthorize("hasAuthority('system:user:edit')")
    @Audit(module = "用户管理", value = "重置密码")
    public Result<Void> resetPassword(@PathVariable Long id, @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(id, request.password());
        return Result.ok();
    }

    /** 数据权限范围枚举，前端下拉框用 */
    @GetMapping("/scopes")
    @PreAuthorize("hasAuthority('system:user:list')")
    public Result<List<Map<String, Object>>> scopes() {
        return Result.ok(List.of(
                Map.of("code", 1, "name", "全部数据"),
                Map.of("code", 2, "name", "本部门数据"),
                Map.of("code", 3, "name", "本部门及以下数据"),
                Map.of("code", 4, "name", "仅本人数据")));
    }
}
