package com.liu.permission.datapermission;

import com.liu.permission.common.enums.DataScopeEnum;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.schema.Table;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * 数据权限 SQL 改写的单元测试。
 * 这里断言的是最终 SQL 片段字符串，因为括号丢失这类问题只有看 SQL 才能发现。
 */
class DataPermissionHandlerImplTest {

    private final DataPermissionHandlerImpl handler = new DataPermissionHandlerImpl();
    private final Table userTable = new Table("sys_user");

    @AfterEach
    void tearDown() {
        DataScopeContext.clear();
    }

    @Test
    @DisplayName("单个部门：生成等值条件")
    void singleDept() {
        DataScopeContext.set(DataScopeRule.of(DataScopeEnum.DEPT, List.of(104L), null));
        Expression expression = handler.getSqlSegment(userTable, null, "com.liu.permission.mapper.SysUserMapper.selectPage");
        assertEquals("sys_user.org_id = 104", expression.toString());
    }

    @Test
    @DisplayName("多个部门：IN 条件必须带括号")
    void multiDept() {
        DataScopeContext.set(DataScopeRule.of(DataScopeEnum.DEPT_AND_CHILD, List.of(101L, 103L, 104L), null));
        Expression expression = handler.getSqlSegment(userTable, null, "com.liu.permission.mapper.SysUserMapper.selectPage");
        assertEquals("sys_user.org_id IN (101, 103, 104)", expression.toString());
    }

    @Test
    @DisplayName("仅本人：按用户ID过滤")
    void selfOnly() {
        DataScopeContext.set(DataScopeRule.of(DataScopeEnum.SELF, null, 3L));
        Expression expression = handler.getSqlSegment(userTable, null, "any");
        assertEquals("sys_user.id = 3", expression.toString());
    }

    @Test
    @DisplayName("全部数据：不追加任何条件")
    void allData() {
        DataScopeContext.set(DataScopeRule.of(DataScopeEnum.ALL, null, null));
        assertNull(handler.getSqlSegment(userTable, null, "any"));
    }

    @Test
    @DisplayName("没有部门归属：收紧为空集而不是放开")
    void noOrg() {
        DataScopeContext.set(DataScopeRule.of(DataScopeEnum.DEPT, List.of(), null));
        Expression expression = handler.getSqlSegment(userTable, null, "any");
        assertEquals("sys_user.id = -1", expression.toString());
    }

    @Test
    @DisplayName("未开启数据权限的查询（如登录）：不改写 SQL")
    void withoutContext() {
        assertNull(handler.getSqlSegment(userTable, null, "any"));
    }

    @Test
    @DisplayName("不相关的表：不改写 SQL")
    void otherTable() {
        DataScopeContext.set(DataScopeRule.of(DataScopeEnum.SELF, null, 3L));
        assertNull(handler.getSqlSegment(new Table("sys_org"), null, "any"));
    }

    @Test
    @DisplayName("显式忽略：不改写 SQL")
    void ignoreScope() {
        DataScopeContext.set(DataScopeRule.ignore());
        assertNull(handler.getSqlSegment(userTable, null, "any"));
    }
}
