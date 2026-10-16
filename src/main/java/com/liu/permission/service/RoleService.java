package com.liu.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.liu.permission.common.BizException;
import com.liu.permission.dto.RoleRequest;
import com.liu.permission.entity.SysRole;
import com.liu.permission.entity.SysRolePermission;
import com.liu.permission.entity.SysUserRole;
import com.liu.permission.mapper.SysRoleMapper;
import com.liu.permission.mapper.SysRolePermissionMapper;
import com.liu.permission.mapper.SysUserRoleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {

    private static final long SUPER_ADMIN_ROLE_ID = 1L;

    private final SysRoleMapper roleMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final PermissionCacheService permissionCacheService;

    public IPage<SysRole> page(long pageNo, long pageSize, String name) {
        LambdaQueryWrapper<SysRole> wrapper = new LambdaQueryWrapper<SysRole>()
                .like(name != null && !name.isBlank(), SysRole::getName, name)
                .orderByAsc(SysRole::getSort);
        return roleMapper.selectPage(new Page<>(pageNo, pageSize), wrapper);
    }

    public List<SysRole> listAll() {
        return roleMapper.selectList(new LambdaQueryWrapper<SysRole>().orderByAsc(SysRole::getSort));
    }

    public List<Long> listPermissionIds(Long roleId) {
        return rolePermissionMapper.selectList(
                        new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId))
                .stream().map(SysRolePermission::getPermissionId).toList();
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(RoleRequest request) {
        Long count = roleMapper.selectCount(new LambdaQueryWrapper<SysRole>().eq(SysRole::getCode, request.code()));
        if (count != null && count > 0) {
            throw new BizException("角色标识已存在：" + request.code());
        }
        SysRole role = new SysRole();
        role.setName(request.name());
        role.setCode(request.code());
        role.setDataScope(request.dataScope() == null ? 4 : request.dataScope());
        role.setSort(request.sort() == null ? 0 : request.sort());
        role.setStatus(request.status() == null ? 1 : request.status());
        role.setRemark(request.remark());
        roleMapper.insert(role);
        saveRolePermissions(role.getId(), request.permissionIds());
        return role.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(RoleRequest request) {
        if (request.id() == null) {
            throw new BizException("角色ID不能为空");
        }
        SysRole role = roleMapper.selectById(request.id());
        if (role == null) {
            throw new BizException("角色不存在");
        }
        role.setName(request.name());
        role.setDataScope(request.dataScope());
        role.setSort(request.sort());
        role.setStatus(request.status());
        role.setRemark(request.remark());
        roleMapper.updateById(role);
        saveRolePermissions(role.getId(), request.permissionIds());
        permissionCacheService.evictAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        if (SUPER_ADMIN_ROLE_ID == id) {
            throw new BizException("超级管理员角色不允许删除");
        }
        Long userCount = userRoleMapper.selectCount(new LambdaQueryWrapper<SysUserRole>().eq(SysUserRole::getRoleId, id));
        if (userCount != null && userCount > 0) {
            throw new BizException("该角色下还有用户，请先解绑");
        }
        roleMapper.deleteById(id);
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, id));
        permissionCacheService.evictAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public void assignPermissions(Long roleId, List<Long> permissionIds) {
        if (roleMapper.selectById(roleId) == null) {
            throw new BizException("角色不存在");
        }
        saveRolePermissions(roleId, permissionIds);
        // 权限点变更影响所有持有该角色的用户
        permissionCacheService.evictAll();
    }

    private void saveRolePermissions(Long roleId, List<Long> permissionIds) {
        if (permissionIds == null) {
            return;
        }
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getRoleId, roleId));
        if (CollectionUtils.isEmpty(permissionIds)) {
            return;
        }
        for (Long permissionId : permissionIds) {
            SysRolePermission relation = new SysRolePermission();
            relation.setRoleId(roleId);
            relation.setPermissionId(permissionId);
            rolePermissionMapper.insert(relation);
        }
    }
}
