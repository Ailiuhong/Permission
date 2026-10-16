package com.liu.permission.security;

import com.liu.permission.entity.SysRole;
import com.liu.permission.entity.SysUser;
import com.liu.permission.mapper.SysUserMapper;
import com.liu.permission.service.PermissionCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final SysUserMapper userMapper;
    private final PermissionCacheService permissionCacheService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SysUser user = userMapper.selectByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return build(user);
    }

    /** JWT 过滤器用：根据令牌里的 userId 还原登录态 */
    public LoginUser loadUserById(Long userId) {
        SysUser user = userMapper.selectById(userId);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在");
        }
        return build(user);
    }

    private LoginUser build(SysUser user) {
        List<SysRole> roles = permissionCacheService.getRoles(user.getId());
        Set<String> permissions = permissionCacheService.getPermissionCodes(user.getId());
        return new LoginUser(user, roles, permissions);
    }
}
