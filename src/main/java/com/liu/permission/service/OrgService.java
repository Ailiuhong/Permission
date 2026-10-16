package com.liu.permission.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liu.permission.common.BizException;
import com.liu.permission.dto.OrgRequest;
import com.liu.permission.entity.SysOrg;
import com.liu.permission.entity.SysUser;
import com.liu.permission.mapper.SysOrgMapper;
import com.liu.permission.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrgService {

    private final SysOrgMapper orgMapper;
    private final SysUserMapper userMapper;

    public List<SysOrg> tree() {
        List<SysOrg> all = orgMapper.selectList(new LambdaQueryWrapper<SysOrg>().orderByAsc(SysOrg::getSort));
        Map<Long, SysOrg> idMap = new HashMap<>();
        all.forEach(org -> {
            org.setChildren(new ArrayList<>());
            idMap.put(org.getId(), org);
        });
        List<SysOrg> roots = new ArrayList<>();
        for (SysOrg org : all) {
            SysOrg parent = idMap.get(org.getParentId());
            if (parent != null) {
                parent.getChildren().add(org);
            } else {
                roots.add(org);
            }
        }
        return roots;
    }

    @Transactional(rollbackFor = Exception.class)
    public Long create(OrgRequest request) {
        SysOrg org = new SysOrg();
        org.setName(request.name());
        org.setSort(request.sort() == null ? 0 : request.sort());
        org.setStatus(request.status() == null ? 1 : request.status());
        applyParent(org, request.parentId());
        orgMapper.insert(org);
        return org.getId();
    }

    @Transactional(rollbackFor = Exception.class)
    public void update(OrgRequest request) {
        if (request.id() == null) {
            throw new BizException("组织ID不能为空");
        }
        SysOrg org = orgMapper.selectById(request.id());
        if (org == null) {
            throw new BizException("组织不存在");
        }
        if (request.id().equals(request.parentId())) {
            throw new BizException("上级组织不能是自己");
        }
        org.setName(request.name());
        org.setSort(request.sort());
        org.setStatus(request.status());
        applyParent(org, request.parentId());
        orgMapper.updateById(org);
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        Long childCount = orgMapper.selectCount(new LambdaQueryWrapper<SysOrg>().eq(SysOrg::getParentId, id));
        if (childCount != null && childCount > 0) {
            throw new BizException("存在下级组织，请先删除下级");
        }
        Long userCount = userMapper.selectCount(new LambdaQueryWrapper<SysUser>().eq(SysUser::getOrgId, id));
        if (userCount != null && userCount > 0) {
            throw new BizException("该组织下还有用户，无法删除");
        }
        orgMapper.deleteById(id);
    }

    /** 维护 ancestors 路径，让"本部门及以下"可以用 find_in_set 一次查出来 */
    private void applyParent(SysOrg org, Long parentId) {
        long pid = parentId == null ? 0L : parentId;
        org.setParentId(pid);
        if (pid == 0L) {
            org.setAncestors("0");
            return;
        }
        SysOrg parent = orgMapper.selectById(pid);
        if (parent == null) {
            throw new BizException("上级组织不存在");
        }
        org.setAncestors(parent.getAncestors() + "," + pid);
    }
}
