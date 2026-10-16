package com.liu.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liu.permission.annotation.DataScope;
import com.liu.permission.common.BizException;
import com.liu.permission.dto.UserRequest;
import com.liu.permission.entity.SysUser;
import com.liu.permission.entity.SysUserRole;
import com.liu.permission.mapper.SysUserMapper;
import com.liu.permission.mapper.SysUserRoleMapper;
import com.liu.permission.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    public static final String DEFAULT_PASSWORD="123456";
    private static final long SUPER_ADMIN_ID=1L;

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PasswordEncoder passwordEncoder;
    private final  PermissionCacheService permissionCacheService;

    /**
     * 用户列表：@DataScope 让拦截器按当前登录人的数据权限自动追加条件，
     * 业务代码里看不到任何权限判断，也不需要手写 org_id 条件。
     */
    @DataScope
    public IPage<SysUser> page(long pageNo, long pageSize, String username, String nickname, Long orgId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .like(StringUtils.hasText(username), SysUser::getUsername, username)
                .like(StringUtils.hasText(nickname), SysUser::getNickname, nickname)
                .eq(orgId != null, SysUser::getOrgId, orgId)
                .orderByAsc(SysUser::getId);
        IPage<SysUser> page = userMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
        // 列表不返回密码字段
        page.getRecords().forEach(user -> user.setPassword(null));
        return page;
    }

    public SysUser getById(Long id) {
        SysUser user = userMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    /** 用户已绑定的角色ID，前端"分配角色"弹窗回显用 */
    public List<Long> listRoleIds(Long userId) {
        return userRoleMapper.selectList(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId))
                .stream().map(SysUserRole::getRoleId).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(UserRequest request) {
        Long count=userMapper.selectCount(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername,request.username()));
        if(count !=null && count>0){
            throw new BizException("账号已经存在"+request.username());
        }
        SysUser user=new SysUser();
        user.setUsername(request.username());
        user.setNickname(request.nickname());
        user.setOrgId(request.orgId());
        user.setStatus(request.status()==null?1:request.status());
        String rawPassword=StringUtils.hasText(request.password()) ?
                request.password() :DEFAULT_PASSWORD;
        user.setPassword(passwordEncoder.encode(rawPassword));
        userMapper.insert(user);
        saveUserRoles(user.getId(),request.roleIds());
        return user.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(UserRequest request) {
        if(request.id()==null){
            throw new BizException("用户id不能为空");
        }
        SysUser user=userMapper.selectById(request.id());
        if(user==null){
            throw new BizException("用户不存在");
        }
        user.setNickname(request.nickname());
        user.setOrgId(request.orgId());
        user.setStatus(request.status());
        if(StringUtils.hasText(request.password())){
            user.setPassword(passwordEncoder.encode(request.password()));

        }
        userMapper.updateById(user);
        saveUserRoles(user.getId(),request.roleIds());
        permissionCacheService.evictUser(user.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (SUPER_ADMIN_ID == id) {
            throw new BizException("超级管理员不允许删除");
        }
        if (id.equals(SecurityUtils.getUserId())) {
            throw new BizException("不能删除当前登录用户");
        }
        userMapper.deleteById(id);
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, id));
        permissionCacheService.evictUser(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignRoles(Long userId, List<Long> roleIds) {
        if(userMapper.selectById(userId)==null){
            throw new BizException("用户不存在");

        }
        saveUserRoles(userId,roleIds);
        permissionCacheService.evictUser(userId);
    }

    public void resetPassword(Long userId, String password) {
        SysUser user=userMapper.selectById(userId);
        if(user==null){
            throw new BizException("用户不存在");

        }
        user.setPassword(passwordEncoder.encode(password));
        userMapper.updateById(user);
    }

    private void saveUserRoles(Long userId, List<Long> roleIds) {
        if (roleIds == null) {
            return;
        }
        userRoleMapper.delete(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getUserId, userId));
        if (CollectionUtils.isEmpty(roleIds)) {
            return;
        }
        for (Long roleId : roleIds) {
            SysUserRole relation = new SysUserRole();
            relation.setUserId(userId);
            relation.setRoleId(roleId);
            userRoleMapper.insert(relation);
        }
    }
}
