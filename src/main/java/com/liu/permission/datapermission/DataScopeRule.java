package com.liu.permission.datapermission;

import com.liu.permission.common.enums.DataScopeEnum;
import lombok.Getter;

import java.util.List;

/**
 * 一次请求内的数据权限规则
 */
@Getter
public class DataScopeRule {

    private final DataScopeEnum scope;
    /** 允许访问的部门ID集合，scope 为 ALL / SELF 时无意义 */
    private final List<Long> orgIds;
    /** scope 为 SELF 时限定为这个用户 */
    private final Long userId;
    private final boolean ignore;

    private DataScopeRule(DataScopeEnum scope, List<Long> orgIds, Long userId, boolean ignore) {
        this.scope = scope;
        this.orgIds = orgIds;
        this.userId = userId;
        this.ignore = ignore;
    }

    public static DataScopeRule of(DataScopeEnum scope, List<Long> orgIds, Long userId) {
        return new DataScopeRule(scope, orgIds, userId, false);
    }

    public static DataScopeRule ignore() {
        return new DataScopeRule(DataScopeEnum.ALL, null, null, true);
    }
}
