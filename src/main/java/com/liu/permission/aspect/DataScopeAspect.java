package com.liu.permission.aspect;

import com.liu.permission.annotation.DataScope;
import com.liu.permission.datapermission.DataScopeContext;
import com.liu.permission.datapermission.DataScopeRule;
import com.liu.permission.security.SecurityUtils;
import com.liu.permission.service.DataScopeService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 在标注 @DataScope 的方法执行期间，把数据权限规则写入 ThreadLocal
 */
@Aspect
@Component
@RequiredArgsConstructor
public class DataScopeAspect {

    private final DataScopeService dataScopeService;

    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint joinPoint, DataScope dataScope) throws Throwable {
        DataScopeRule rule = dataScope.ignore()
                ? DataScopeRule.ignore()
                : dataScopeService.currentRule(SecurityUtils.getLoginUser());
        DataScopeContext.set(rule);
        try {
            return joinPoint.proceed();
        } finally {
            // 必须清理，避免线程复用时把上一个请求的数据权限带给下一个请求
            DataScopeContext.clear();
        }
    }
}
