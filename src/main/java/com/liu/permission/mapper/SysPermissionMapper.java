package com.liu.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.permission.entity.SysPermission;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysPermissionMapper extends BaseMapper<SysPermission> {

    /** 用户拥有的权限编码（菜单 + 按钮 + 接口） */
    @Select("select distinct p.code from sys_permission p " +
            "join sys_role_permission rp on rp.permission_id = p.id " +
            "join sys_user_role ur on ur.role_id = rp.role_id " +
            "where ur.user_id = #{userId} and p.status = 1 and p.deleted = 0 " +
            "and p.code is not null and p.code <> ''")
    List<String> selectCodesByUserId(Long userId);

    /** 用户拥有的菜单（前端动态路由用） */
    @Select("select distinct p.* from sys_permission p " +
            "join sys_role_permission rp on rp.permission_id = p.id " +
            "join sys_user_role ur on ur.role_id = rp.role_id " +
            "where ur.user_id = #{userId} and p.type = 1 and p.status = 1 and p.deleted = 0 " +
            "order by p.sort")
    List<SysPermission> selectMenusByUserId(Long userId);
}
