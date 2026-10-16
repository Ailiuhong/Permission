package com.liu.permission.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PermissionRequest(Long id,
                                @NotBlank(message = "权限名称不能为空") String name,
                                String code,
                                @NotNull(message = "权限类型不能为空") Integer type,
                                Long parentId,
                                String path,
                                String component,
                                String icon,
                                Integer sort,
                                Integer status) {
}
