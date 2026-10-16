package com.liu.permission.common.enums;

import lombok.Getter;

/**
 * 数据权限范围
 */
@Getter
public enum DataScopeEnum {

    ALL(1, "全部数据"),
    DEPT(2, "本部门数据"),
    DEPT_AND_CHILD(3, "本部门及以下数据"),
    SELF(4, "仅本人数据");

    private final Integer code;
    private final String desc;

    DataScopeEnum(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public static DataScopeEnum fromCode(Integer code) {
        if (code == null) {
            return SELF;
        }
        for (DataScopeEnum item : values()) {
            if (item.code.equals(code)) {
                return item;
            }
        }
        return SELF;
    }
}
