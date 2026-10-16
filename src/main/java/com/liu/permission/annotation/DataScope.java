package com.liu.permission.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记需要按当前用户数据权限过滤的方法。
 * ignore = true 时跳过过滤（例如定时任务、系统内部调用）。
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface DataScope {

    boolean ignore() default false;
}
