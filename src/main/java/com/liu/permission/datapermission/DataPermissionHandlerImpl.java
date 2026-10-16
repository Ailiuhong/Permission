package com.liu.permission.datapermission;

import com.baomidou.mybatisplus.extension.plugins.handler.MultiDataPermissionHandler;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.operators.relational.EqualsTo;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.expression.operators.relational.ParenthesedExpressionList;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 数据权限 SQL 改写。
 * 关键点：只拼接参数化的结构（LongValue），不拼字符串，天然规避 SQL 注入；
 * 且只在 {@link com.liu.permission.annotation.DataScope} 标注的方法内生效，登录等查询不受影响。
 */
@Slf4j
@Component
public class DataPermissionHandlerImpl implements MultiDataPermissionHandler {

    /** 需要参与数据权限过滤的表 */
    private static final Set<String> FILTER_TABLES = Set.of("sys_user");

    @Override
    public Expression getSqlSegment(Table table, Expression where, String mappedStatementId) {
        DataScopeRule rule = DataScopeContext.get();
        if (rule == null || rule.isIgnore()) {
            return null;
        }
        String tableName = table.getName().replace("`", "").toLowerCase();
        if (!FILTER_TABLES.contains(tableName)) {
            return null;
        }
        switch (rule.getScope()) {
            case ALL:
                return null;
            case SELF:
                // 只能看自己：id = 当前用户
                return rule.getUserId() == null ? alwaysFalse(table) : new EqualsTo(column(table, "id"), new LongValue(rule.getUserId()));
            case DEPT:
            case DEPT_AND_CHILD:
            default:
                List<Long> orgIds = rule.getOrgIds();
                if (orgIds == null || orgIds.isEmpty()) {
                    // 没有部门归属时不允许看到任何数据，而不是放开
                    log.debug("用户无部门归属，数据权限收紧为空集，statement={}", mappedStatementId);
                    return alwaysFalse(table);
                }
                // 只有一个部门时用等值条件，比 IN 更利于走索引
                if (orgIds.size() == 1) {
                    return new EqualsTo(column(table, "org_id"), new LongValue(orgIds.get(0)));
                }
                // 注意必须用 ParenthesedExpressionList：普通 ExpressionList 渲染出来是
                // "org_id IN 101, 103"，缺少括号会导致 MySQL 语法错误
                List<Expression> values = orgIds.stream()
                        .map(id -> (Expression) new LongValue(id))
                        .collect(Collectors.toList());
                InExpression in = new InExpression();
                in.setLeftExpression(column(table, "org_id"));
                in.setRightExpression(new ParenthesedExpressionList<>(values));
                return in;
        }
    }

    private Expression alwaysFalse(Table table) {
        return new EqualsTo(column(table, "id"), new LongValue(-1L));
    }

    /** 带上表别名，支持多表关联时精确定位字段 */
    private Column column(Table table, String name) {
        return new Column(table, name);
    }
}
