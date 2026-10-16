package com.liu.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.permission.entity.SysRole;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysRoleMapper extends BaseMapper<SysRole> {

    @Select("select r.* from sys_role r " +
            "join sys_user_role ur on ur.role_id = r.id " +
            "where ur.user_id = #{userId} and r.status = 1 and r.deleted = 0")
    List<SysRole> selectRolesByUserId(Long userId);
}
