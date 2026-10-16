package com.liu.permission.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.liu.permission.entity.SysUser;
import com.liu.permission.mapper.SysUserMapper;
import com.liu.permission.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 演示数据里的密码是占位符 INIT，启动时统一加密成 123456，
 * 避免把 BCrypt 密文硬编码进 SQL 脚本。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PasswordInitRunner implements ApplicationRunner {

    private final SysUserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        try {
            List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>().eq(SysUser::getPassword, "INIT"));
            if (users.isEmpty()) {
                return;
            }
            for (SysUser user : users) {
                user.setPassword(passwordEncoder.encode(UserService.DEFAULT_PASSWORD));
                userMapper.updateById(user);
            }
            log.info("已初始化 {} 个演示账号的密码为 {}", users.size(), UserService.DEFAULT_PASSWORD);
        } catch (Exception e) {
            log.warn("演示密码初始化失败（通常是数据库还没执行 sql/schema.sql 和 sql/data.sql）：{}", e.getMessage());
        }
    }
}
