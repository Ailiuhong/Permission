package com.liu.permission.dto;

import jakarta.validation.constraints.NotBlank;

public record OrgRequest(Long id,
                         @NotBlank(message = "组织名称不能为空") String name,
                         Long parentId,
                         Integer sort,
                         Integer status) {
}
