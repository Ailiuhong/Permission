package com.liu.permission.controller;

import com.liu.permission.common.Result;
import com.liu.permission.dto.LoginRequest;
import com.liu.permission.dto.RefreshRequest;
import com.liu.permission.security.LoginUser;
import com.liu.permission.security.SecurityUtils;
import com.liu.permission.security.TokenService;
import com.liu.permission.service.PermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final TokenService tokenService;
    private final PermissionService permissionService;

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody LoginRequest request) {
        return Result.ok(tokenService.login(request.username(), request.password()));
    }

    @PostMapping("/refresh")
    public Result<Map<String, Object>> refresh(@Valid @RequestBody RefreshRequest request) {
        return Result.ok(tokenService.refresh(request.refreshToken()));
    }

    @PostMapping("/logout")
    public Result<Void> logout(@RequestBody(required = false) RefreshRequest request) {
        tokenService.logout(request == null ? null : request.refreshToken());
        return Result.ok();
    }

    /** 前端登录后拉取：用户信息 + 角色 + 权限编码 + 菜单树 */
    @GetMapping("/me")
    public Result<Map<String, Object>> me() {
        LoginUser loginUser = SecurityUtils.getLoginUser();
        Map<String, Object> data = new HashMap<>();
        data.put("userId", loginUser.getUserId());
        data.put("username", loginUser.getUsername());
        data.put("nickname", loginUser.getNickname());
        data.put("orgId", loginUser.getOrgId());
        data.put("roles", loginUser.getRoleCodes());
        data.put("permissions", loginUser.getPermissions());
        data.put("menus", permissionService.menuTreeOfUser(loginUser.getUserId()));
        return Result.ok(data);
    }
}
