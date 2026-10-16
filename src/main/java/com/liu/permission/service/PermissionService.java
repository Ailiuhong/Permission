package com.liu.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liu.permission.common.BizException;
import com.liu.permission.dto.PermissionRequest;
import com.liu.permission.entity.SysPermission;
import com.liu.permission.entity.SysRolePermission;
import com.liu.permission.mapper.SysPermissionMapper;
import com.liu.permission.mapper.SysRolePermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PermissionService {

    private final SysPermissionMapper permissionMapper;
    private final SysRolePermissionMapper rolePermissionMapper;
    private final PermissionCacheService permissionCacheService;

    /** 全量权限树，权限管理页面与"分配权限"弹窗使用 */
    public List<SysPermission> tree() {
        List<SysPermission> all = permissionMapper.selectList(
                new LambdaQueryWrapper<SysPermission>().orderByAsc(SysPermission::getSort));
        return buildTree(all);
    }

    /** 当前用户可见的菜单树，前端动态路由使用 */
    public List<SysPermission> menuTreeOfUser(Long userId) {
        return buildTree(permissionMapper.selectMenusByUserId(userId));
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(PermissionRequest request) {
        SysPermission permission = new SysPermission();
        permission.setName(request.name());
        permission.setCode(request.code());
        permission.setType(request.type());
        permission.setParentId(request.parentId() == null ? 0L : request.parentId());
        permission.setPath(request.path());
        permission.setComponent(request.component());
        permission.setIcon(request.icon());
        permission.setSort(request.sort() == null ? 0 : request.sort());
        permission.setStatus(request.status() == null ? 1 : request.status());
        permissionMapper.insert(permission);
        permissionCacheService.evictAll();
        return permission.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(PermissionRequest request) {
        if (request.id() == null) {
            throw new BizException("权限ID不能为空");
        }
        SysPermission permission = permissionMapper.selectById(request.id());
        if (permission == null) {
            throw new BizException("权限不存在");
        }
        permission.setName(request.name());
        permission.setCode(request.code());
        permission.setType(request.type());
        permission.setParentId(request.parentId());
        permission.setPath(request.path());
        permission.setComponent(request.component());
        permission.setIcon(request.icon());
        permission.setSort(request.sort());
        permission.setStatus(request.status());
        permissionMapper.updateById(permission);
        permissionCacheService.evictAll();
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long childCount = permissionMapper.selectCount(
                new LambdaQueryWrapper<SysPermission>().eq(SysPermission::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BizException("存在子权限，请先删除子节点");
        }
        permissionMapper.deleteById(id);
        rolePermissionMapper.delete(new LambdaQueryWrapper<SysRolePermission>().eq(SysRolePermission::getPermissionId, id));
        permissionCacheService.evictAll();
    }

    /**
     * 扁平列表转树。
     * 注意：父节点未被授权时，子节点会被丢弃，避免前端出现"有按钮权限却看不到菜单"的悬空节点。
     */
    private List<SysPermission> buildTree(List<SysPermission> flat) {
        Map<Long, SysPermission> idMap = new HashMap<>();
        for (SysPermission item : flat) {
            item.setChildren(new ArrayList<>());
            idMap.put(item.getId(), item);
        }
        List<SysPermission> roots = new ArrayList<>();
        for (SysPermission item : flat) {
            SysPermission parent = idMap.get(item.getParentId());
            if (parent != null) {
                parent.getChildren().add(item);
            } else if (item.getParentId() == null || item.getParentId() == 0) {
                roots.add(item);
            }
        }
        return roots;
    }
}
