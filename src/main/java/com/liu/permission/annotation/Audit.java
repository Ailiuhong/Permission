package com.liu.permission.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 关键操作审计：打在 Controller / Service 方法上，自动记录操作人与结果
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Audit {

    /** 模块名，如"用户管理" */
    String module();

    /** 操作描述，如"删除用户" */
    String value();
}
