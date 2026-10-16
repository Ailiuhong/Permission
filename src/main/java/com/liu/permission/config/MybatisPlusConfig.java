package com.liu.permission.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.DataPermissionInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.liu.permission.datapermission.DataPermissionHandlerImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MybatisPlusConfig {

    /**
     * 拦截器顺序有讲究：先做数据权限拼接，再做分页，
     * 保证分页 total 也是过滤后的数据量（否则总数会泄露越权数据的规模）
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor(DataPermissionHandlerImpl dataPermissionHandler) {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new DataPermissionInterceptor(dataPermissionHandler));
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }
}
