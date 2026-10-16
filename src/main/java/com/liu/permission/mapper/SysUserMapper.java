package com.liu.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.liu.permission.entity.SysUser;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface SysUserMapper extends BaseMapper<SysUser> {

    @Select("select * from sys_user where username = #{username} and deleted = 0 limit 1")
    SysUser selectByUsername(String username);

    /** 本部门及以下：通过 ancestors 路径匹配，避免递归查询 */
    @Select("select id from sys_org where deleted = 0 and (id = #{orgId} or find_in_set(#{orgId}, ancestors))")
    List<Long> selectOrgAndChildren(Long orgId);
}
