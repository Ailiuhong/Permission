package com.liu.permission.service;

import com.liu.permission.common.enums.DataScopeEnum;
import com.liu.permission.datapermission.DataScopeRule;
import com.liu.permission.entity.SysRole;
import com.liu.permission.mapper.SysUserMapper;
import com.liu.permission.security.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 计算当前用户最终生效的数据范围。
 * 多角色时取"最宽"的范围（ALL 最宽，SELF 最窄），与主流后台系统一致：角色权限取并集。
 */
@Service
@RequiredArgsConstructor
public class DataScopeService {

    private final SysUserMapper userMapper;

    public DataScopeRule currentRule(LoginUser loginUser) {
        List<SysRole> roles = loginUser.getRoles();
        DataScopeEnum effective = DataScopeEnum.SELF;
        if (roles != null) {
            for (SysRole role : roles) {
                DataScopeEnum scope = DataScopeEnum.fromCode(role.getDataScope());
                if (scope.getCode() < effective.getCode()) {
                    effective = scope;
                }
            }
        }
        Long orgId = loginUser.getOrgId();
        return switch (effective) {
            case ALL -> DataScopeRule.of(DataScopeEnum.ALL, null, null);
            case DEPT -> DataScopeRule.of(DataScopeEnum.DEPT, orgId == null ? List.of() : List.of(orgId), null);
            case DEPT_AND_CHILD -> DataScopeRule.of(DataScopeEnum.DEPT_AND_CHILD,
                    orgId == null ? List.of() : userMapper.selectOrgAndChildren(orgId), null);
            case SELF -> DataScopeRule.of(DataScopeEnum.SELF, null, loginUser.getUserId());
        };
    }
}
