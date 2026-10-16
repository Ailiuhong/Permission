package com.liu.permission.security;

import com.liu.permission.entity.SysRole;
import com.liu.permission.entity.SysUser;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 登录用户：包含用户基础信息、角色、权限编码，权限编码直接作为 Spring Security 的 authority
 */
@Getter
public class LoginUser implements UserDetails {

    private final Long userId;
    private final String username;
    private final String password;
    private final String nickname;
    private final Long orgId;
    private final Integer status;
    private final List<SysRole> roles;
    private final Set<String> permissions;
    private final List<GrantedAuthority> authorities;

    public LoginUser(SysUser user, List<SysRole> roles, Set<String> permissions) {
        this.userId = user.getId();
        this.username = user.getUsername();
        this.password = user.getPassword();
        this.nickname = user.getNickname();
        this.orgId = user.getOrgId();
        this.status = user.getStatus();
        this.roles = roles;
        this.permissions = permissions;
        this.authorities = permissions == null
                ? new ArrayList<>()
                : permissions.stream().map(SimpleGrantedAuthority::new).collect(Collectors.toList());
    }

    public List<String> getRoleCodes() {
        return roles == null ? List.of() : roles.stream().map(SysRole::getCode).toList();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return status == null || status == 1;
    }
}
