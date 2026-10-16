package com.liu.permission.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record RoleRequest(Long id,
                          @NotBlank(message = "角色名称不能为空") String name,
                          @NotBlank(message = "角色标识不能为空") String code,
                          Integer dataScope,
                          Integer sort,
                          Integer status,
                          String remark,
                          List<Long> permissionIds) {
}
