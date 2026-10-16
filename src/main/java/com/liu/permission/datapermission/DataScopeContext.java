package com.liu.permission.datapermission;

/**
 * 数据权限上下文：AOP 在方法进入时写入，方法结束时清理，
 * MyBatis-Plus 拦截器在改写 SQL 时读取。用 ThreadLocal 保证线程隔离。
 */
public final class DataScopeContext {

    private static final ThreadLocal<DataScopeRule> HOLDER = new ThreadLocal<>();

    private DataScopeContext() {
    }

    public static void set(DataScopeRule rule) {
        HOLDER.set(rule);
    }

    public static DataScopeRule get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }
}
